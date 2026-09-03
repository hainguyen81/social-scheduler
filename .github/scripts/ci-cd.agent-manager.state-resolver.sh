#!/bin/bash
export STATE_FILE=".ai/.agents/.states/pipeline_state.json"
export PLAN_FILE=".ai/.plan/plan.spec.json"

# 🧹 CLEAN WORKSPACE
echo "🧹 [WIPE] Vaporizing all untracked and modified files locally..."
git reset --hard HEAD
git clean -ffdx

# make sure directory existing
mkdir -p "$(dirname "$STATE_FILE")"
mkdir -p "$(dirname "$PLAN_FILE")"

# -------------------------------------------------
# !!!IMPORTANT!!! Execute via launcher for registering workflows scripts python as module
# -------------------------------------------------
# 🐍 call python script to calculate running day/phase
# .github/scripts/ci-cd.agent-manager.state-resolver.py
python3 .libs/agent_launcher.py _0d_github.scripts.ci_0h_cd_0d_agent_0h_manager_0d_state_0h_resolver

# adapt calculated running day/phase to GitHub Actions Enviroment
source .agent_resolved_state && rm -f .agent_resolved_state

# check enviroment
if [ -z "$RESOLVED_DAY" -o -z "$RESOLVED_PHASE" ]; then
	echo "❌ [ ERROR ] Could not resolve the running day/phase: RUN_DAY: {$RESOLVED_DAY} | RUN_PHASE: {$RESOLVED_PHASE}"
	exit 1
fi

# 🏁 if project already finished, remove schedule
if [ "$PROJECT_ENDED" = "true" ]; then
  echo "🎉✨ [PROJECT ENDED] All phases and target development timelines executed successfully!"
  echo "🛑🛑 Triggering automated schedule workflow removal protocols..."
  gh workflow disable "${GITHUB_WORKFLOW}" || true
  echo "✅🏁 Schedule automated execution successfully disabled. Terminating pipeline gracefully."
  exit 0
fi

# 📤 export calculated running day/phase to GITHUB_OUTPUT for later sub-agents steps
echo "run_day=$RESOLVED_DAY" >> "$GITHUB_OUTPUT"
echo "run_phase=$RESOLVED_PHASE" >> "$GITHUB_OUTPUT"
echo "total_days=$TOTAL_DAYS" >> "$GITHUB_OUTPUT"
echo "total_phases=$TOTAL_PHASES" >> "$GITHUB_OUTPUT"
echo "phase_ended=$PHASE_ENDED" >> "$GITHUB_OUTPUT"
echo "exec_mode=$EXEC_MODE" >> "$GITHUB_OUTPUT"

# 🌐 define branch for sub-agents working
PREV_BRANCH="features/development-phase-$PREV_PHASE-day-$PREV_DAY"
BRANCH_NAME="features/development-phase-$RESOLVED_PHASE-day-$RESOLVED_DAY"
echo "🌐 [GIT ROUTER] Target environment workspace branch assigned: $BRANCH_NAME"

rm -f "$STATE_FILE"
git checkout -- "$STATE_FILE" 2>/dev/null || true

# 🌿 Check if the target BRANCH_NAME already exists (to prevent overwriting active progress)
if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME" || git ls-remote --exit-code --heads origin "$BRANCH_NAME" >/dev/null 2>&1; then
  echo "🌿 [GIT MERGE] Existing target branch detected. Synchronizing workspace..."
  git fetch origin "$BRANCH_NAME" 2>/dev/null || true
  git checkout -f "$BRANCH_NAME"
  # Optional: Keep it up to date with the remote version if it exists
  git pull origin "$BRANCH_NAME" --no-rebase 2>/dev/null || true

# 🔄 If target doesn't exist, check if PREV_BRANCH exists to branch off from
elif git show-ref --verify --quiet "refs/heads/$PREV_BRANCH" || git ls-remote --exit-code --heads origin "$PREV_BRANCH" >/dev/null 2>&1; then
  echo "⏮️ [GIT FORK] Previous branch [$PREV_BRANCH] detected. Bootstrapping new workspace..."
  git fetch origin "$PREV_BRANCH" 2>/dev/null || true
  
  # Checkout the previous branch safely (local tracking or remote fallback)
  git checkout -f "$PREV_BRANCH" 2>/dev/null || git checkout -f -b "$PREV_BRANCH" "origin/$PREV_BRANCH"
  
  # Fork the new branch from it
  git checkout -f -b "$BRANCH_NAME"

# 🚀 Default Fallback: Initialize from mainline trunk base
else
  echo "🔄 [GIT FETCH] Previous branch not found. Fetching standard mainline development trunk baseline..."
  git fetch origin features/development 2>/dev/null || true
  
  # Determine base trunk
  if git show-ref --verify --quiet refs/remotes/origin/features/development || git show-ref --verify --quiet refs/heads/features/development; then
    TRUNK_BASE="features/development"
  else
    TRUNK_BASE="master"
  fi
  
  echo "🚀 [GIT FORK] Provisioning pristine branch [$BRANCH_NAME] initialized from stable trunk root ($TRUNK_BASE)"
  git checkout -f "$TRUNK_BASE" 2>/dev/null || git checkout -f master
  git checkout -f -b "$BRANCH_NAME"
fi
 
# push changes to development phase/day branch before sub-agents running
git push origin HEAD:"refs/heads/$BRANCH_NAME" || true

# Save state later, when sub-agents run success fully
# # 🚀 ABSOLUTE WRITE OVERWRITE: Re-write the correct synced JSON payload right inside the active workspace branch
# if [ "$SHOULD_SAVE_STATE" = "true" ]; then
#   echo "💾 [STATE SAVE] Serializing current matrix back to file tracking storage..."
#   mkdir -p "$(dirname "$STATE_FILE")"
#   echo "{\"current_day\": $RESOLVED_DAY, \"current_phase\": $RESOLVED_PHASE}" > "$STATE_FILE"
#   
#   git add "$STATE_FILE"
#   git commit -m "chore(pipeline): record cron incremental state to Phase $RESOLVED_PHASE Day $RESOLVED_DAY [AI Loop]" || true
#   git push origin HEAD:"refs/heads/$BRANCH_NAME" --force || true
#   echo "✨ [SUCCESS] Dynamic matrix pointers synchronized upstream!"
# else
#   echo "⚠️ [MANUAL MODE] Skipping state serialization rules. $STATE_FILE remains unmodified."
# fi
