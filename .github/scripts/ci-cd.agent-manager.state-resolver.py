#!/usr/bin/env python3
import json
import os
import sys

from _0d_ai._0d_agents.agent_0u_helper import (
    get_logger,
    read_json_file,
    resolve_absolute_path,
    write_file,
)


def main():
    logger = get_logger("⚛️ EnterpriseProjectStateManagement")
    logger.info("👉 ⚛️ START: Resolving Project State...")
    plan_path = resolve_absolute_path(os.environ.get("PLAN_FILE", ".ai/.plan/plan.spec.json"))
    state_path = resolve_absolute_path(os.environ.get("STATE_FILE", ".ai/.agents/.states/pipeline_state.json"))
    
    if not os.path.exists(plan_path):
        logger.error(
            f"     └── ❌ [ ERROR ] Plan specification file not found at: {plan_path}"
        )
        sys.exit(1)
    
    # read state
    _, state = read_json_file(state_path)
    
    # read plan json file
    _, plan = read_json_file(plan_path)
    
    # parse plan information
    phases_config = plan.get("phases", [])
    total_phases = plan.get("num_phases", len(phases_config))
    total_days_allowed = plan.get("total_days", sum(p["days"] for p in phases_config))
    phases_str = json.dumps(phases_config, indent=4, ensure_ascii=False)
    logger.info(
        f"     └── 🕒 [ READ ] Total Phases: {total_phases}. Phases: {phases_str}"
    )
    
    # Ingest enviroment for GitHub Actions
    trigger_event = os.environ.get("TRIGGER_EVENT", "workflow_dispatch")
    is_schedule_event = trigger_event.lower() == "schedule"
    state_exec_mode = state.get("exec_mode", "auto_cron") if state else None
    exec_mode = (state_exec_mode or "auto_cron") if is_schedule_event else os.environ.get("INPUT_EXEC_MODE", state_exec_mode) or "auto_cron"
    scope = os.environ.get("INPUT_TARGET_SCOPE", "")
    val_str = os.environ.get("INPUT_VALUE", "")
    logger.info(f"     └── 🕒 [ PARAMETERS ] State Execution: BY_SCHEDULE {is_schedule_event}; EXEC_MODE {exec_mode}; SCOPE {scope}")
    
    final_phase = 1
    final_day = 1
    should_save_state = False
    phase_ended = False
    project_ended = False

    # -------------------------------------------------------------
    # 🕒 CASE 1: AUTO_CRON (Auto cron based on STATE_FILE)
    # -------------------------------------------------------------
    if exec_mode == "auto_cron":
        should_save_state = True
        logger.info(
            "               └── 🕒 [ MODE ] Detected Auto Cron Execution. Resolving historical state matrix..."
        )
        
        # load previous state
        if state:
            curr_day = state.get("current_day", 0)
            curr_phase = state.get("current_phase", 1)
            logger.info(
                f"                      └── 📖 [ READ ] Prior stored baseline matrix: Phase {curr_phase} / Day {curr_day}"
            )
        
        # Initial state for first running time
        else:
            curr_day = 0
            curr_phase = 1
            logger.info(
                "                      └── 🆕 [ INIT ] Stored baseline not found. Instantiating standard origin baseline (Phase 1 / Day 0)..."
            )
        
        # parse phase config
        phase_meta = next((p for p in phases_config if p["phase"] == curr_phase), None)
        phase_meta_str = json.dumps(phase_meta, indent=4, ensure_ascii=False)
        logger.info(f"                      └── 🆕 [ CONFIG ] Phase Meta: {phase_meta_str}")
        
        # calculate running day/phase
        if phase_meta and curr_day < phase_meta["days"]:
            final_day = curr_day + 1
            final_phase = curr_phase
        else:
            final_phase = curr_phase + 1
            final_day = 1
        phase_ended = final_phase <= total_phases and final_day == phase_meta["days"]
        # exceed phase number, it means project already finished
        project_ended = final_phase > total_phases

    # -------------------------------------------------------------
    # 🎛️ CASE 2: MANUAL (Trigger Manually)
    # -------------------------------------------------------------
    else:
        should_save_state = False
        exec_mode = 'manual'
        logger.info(
            "               └── 🎛️ [ MODE ] Detected Manual Override Target Mode. Evaluating dynamic constraints..."
        )
        if not val_str:
            logger.error(
                "                      └── ❌ [ ERROR ] Manual execution mode requires a target INPUT_VALUE!"
            )
            sys.exit(1)
        
        # based on input value to run manual
        val = int(val_str)
        
        # run by day
        if scope == "by_day":
            # check whether exceed total_days of phases
            if val > total_days_allowed or val <= 0:
                logger.error(
                    f"                      └── ❌ [ ERROR ] Targeted absolute day ({val}) exceeds project maximum ({total_days_allowed})!"
                )
                sys.exit(1)
            
            # calculate phase that need to run
            accumulated_days = 0
            found = False
            for p in phases_config:
                if accumulated_days < val <= accumulated_days + p["days"]:
                    final_phase = p["phase"]
                    final_day = val - accumulated_days
                    found = True
                    break
                accumulated_days += p["days"]
            
            # not found any phase match calculated running day
            if not found:
                logger.error(
                    "                      └── ❌ [ ERROR ] Failed to map absolute day metrics to localized Phase structures."
                )
                sys.exit(1)
        
        # run by phase
        else:
            # exceed total_phases
            if val > total_phases or val <= 0:
                logger.error(
                    f"                      └── ❌ [ ERROR ] Targeted Phase ID ({val}) exceeds project schema bounds ({total_phases})!"
                )
                sys.exit(1)
            
            # start inputted phase from day 1
            final_phase = val
            final_day = 1
    
    # Collect agents state
    agents_state = [
        f"RESOLVED_DAY={final_day}",
        f"RESOLVED_PHASE={final_phase}",
        f"SHOULD_SAVE_STATE={'true' if should_save_state else 'false'}",
        f"PHASE_ENDED={'true' if phase_ended else 'false'}",
        f"PROJECT_ENDED={'true' if project_ended else 'false'}",
        f"TOTAL_PHASES={total_phases}",
        f"TOTAL_DAYS={total_days_allowed}",
        f"EXEC_MODE={exec_mode}"
    ]
    print(f"     └── 🆕 [ FINAL STATE ] Phase Meta: {agents_state}")

    # Export calculated values to temporary enviroment file for GitHub Actions
    state_file = resolve_absolute_path(".agent_resolved_state")
    write_file(file=state_file, data="\n".join(agents_state))
    logger.info("👉 ⚛️ END: Resolving Project State...")

if __name__ == "__main__":
    main()
