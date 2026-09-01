# .ai/.agents/.sub-agents/agent-tester.py
import os
import sys
from datetime import datetime

# Now Python can seamlessly see and import the centralized helper utility cleanly!
from _0d_ai._0d_agents._0d_sub_0u_agents.helper import write_sub_agent_history

# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
# request agent_helper from `.libs/project_agents_package_loader.py`
from _0d_ai._0d_agents.agent_0u_helper import (
    exception_stacktrace,
    kwargs_by_key,
    read_file_raw,
    read_json_file,
    resolve_absolute_path,
    splitAIResponseData,
    write_file,
)
from _0d_ai._0d_agents.agent_0u_super import AbstractAgent

# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
STEPS_PLAN_DIR              = resolve_absolute_path(".ai/.plan/.steps")

class AbstractSubAgent(AbstractAgent):
    def __init__(self, agent_id, phase_str, day_num, **kwargs):
        super().__init__(agent_id=agent_id if agent_id else "SubSuper", **kwargs)
        self.phase_str = phase_str
        self.day_num = int(day_num)
    
    def write_history_log(self, source_component, target_component, user_prompt, data, append=False):
        return write_sub_agent_history(
            history_file=self.agent_log_file(),
            day=self.day_num,
            model_name=self.config_model_name(),
            api_endpoint=self.config_api_endpoint(),
            source_component=source_component,
            target_component=target_component,
            prompt=user_prompt,
            data=data,
            append=append
        )
    
    def collect_agent_tasks(self, target_day):
        tasks = []
        for task in target_day['sub_tasks'] if target_day['sub_tasks'] else []:
            if (task["agent"] and self.agent_id.lower() == task["agent"].lower()) or (task["desc"] and task["desc"].startswith(self.agent_id)):
                tasks.append(task)
        return tasks
    
    def collect_agent_components(self, tasks):
        components = []
        for task in tasks if isinstance(tasks, list) else []:
            for component in task["components"] if isinstance(task["components"], list) else []:
                components.append(component)
        return components
    
    def __component_language_code__(self, component_key: str, **kwargs):
        component_path = self.get_kwargs_by_key(key=component_key, **kwargs)
        if os.path.exists(component_path):
            return (
                "typescript"
                if component_path.endswith((".ts", ".tsx", ".js"))
                else "java"
                if component_path.endswith(".java")
                else "markdown"
                if component_path.endswith(".md")
                else "yaml"
                if component_path.endswith((".yaml", ".yml"))
                else "sql"
                if component_path.endswith(".sql")
                else "json"
                if component_path.endswith(".json")
                else "properties"
                if component_path.endswith(".properties")
                else "bash"
                if component_path.endswith(".sh")
                else "text"
            )
        return "text"

    def __read_component_metadata__(self, component_key: str, **kwargs):
        component_path = self.get_kwargs_by_key(key=component_key, **kwargs)
        raw_component_content = None
        component_payload = None
        lang_code = None
        if component_path and os.path.exists(component_path):
            lang_code = self.__component_language_code__(component_key, **kwargs)
            _, raw_content = read_file_raw(component_path)
            raw_component_content = raw_content.strip()
            component_payload = f"```{lang_code}\n{raw_component_content}\n```" if raw_component_content else None
        return (lang_code, component_payload, raw_component_content)
    
    # @ override
    def clean_response(self, raw_response, **kwargs):
        return splitAIResponseData(raw_response)
    
    # @ override
    def process_communication(self, **kwargs):
        response_data = self.get_kwargs_by_key(key="clean_response", **kwargs)
        target_component = self.get_kwargs_by_key(key="target_component", **kwargs)
        write_file(
            file=target_component,
            data=response_data
        )
        self.logger.info(f"[ ✅ Model {self.config_model_name()} | API Endpoint {self.config_api_endpoint()} | Day {self.day_num} ] Saved to: { target_component }")
        return { **kwargs }
    
    # @ override
    def pre_execute(self, **kwargs):
        # read JSON steps
        phase_step_file = f"phase-{self.phase_str}.steps.json"
        _, steps_data = read_json_file(os.path.join(STEPS_PLAN_DIR, phase_step_file))
        if not steps_data:
            self.logger.critical(f"💀 Not found phase steps JSON file { phase_step_file }")
            sys.exit(1)
        
        # parse project name from phase steps data
        datetimeStr = datetime.now().strftime("%Y%m%d%H%M%S")
        defaultPrjName = f"project-{datetimeStr}"
        project_name = steps_data["project_name"] if steps_data["project_name"] else defaultPrjName
        
        # check agent from JSON steps
        target_day = next((d for d in steps_data["days"] if d["day"] == self.day_num), None)
        agent_tasks = self.collect_agent_tasks(target_day)
        if not agent_tasks or len(agent_tasks) <= 0:
            self.logger.warning(f"⚠️ Step Day { self.day_num }, File { phase_step_file } has no any task!")
            sys.exit(0)
        
        # tracing
        self.logger.info(f"ℹ️ Step Day { self.day_num }, File { phase_step_file }, Execute Agent Project {project_name}...")
        
        # check whether exists any components for this agent
        components = self.collect_agent_components(agent_tasks)
        if not components or len(components) <= 0:
            self.logger.warning(f"⚠️ Step Day { self.day_num }, File { phase_step_file } has no any components!")
            sys.exit(0)
        
        # read global context md
        global_context_file, global_context = read_file_raw(resolve_absolute_path(steps_data["global_context_file"]))
        if not global_context:
            self.logger.critical(f"💀 Not found GLOBAL project context markdown { global_context_file }")
            sys.exit(1)
        
        # request phase context
        phase_context_file, phase_context = read_file_raw(resolve_absolute_path(target_day["context_file"]))
        if not phase_context:
            self.logger.critical(f"💀 Not found PHASE context markdown { phase_context_file }")
            sys.exit(1)
        
        # return merged new values
        return {
            **kwargs,
            "project_name": project_name,
            "phase_step_file": phase_step_file,
            "agent_tasks": agent_tasks,
            "global_context": global_context,
            "phase_context": phase_context,
            "plan_steps": steps_data,
            "day_context": target_day['context_section']
        }
    
    # @override
    def __handle_execute_exception__(self, e, **kwargs):
        model_name = self.config_model_name() if self.current_model_config else None
        self.logger.error(f"❌ Exception caught on model {model_name}: {exception_stacktrace(e)}")
        # write log
        self.write_history_log(
            source_component=kwargs_by_key(key="source_component", **kwargs),
            target_component=kwargs_by_key(key="target_component", **kwargs),
            user_prompt=kwargs_by_key(key="user_prompt", **kwargs),
            data=exception_stacktrace(e),
            append=True
        )

    # @override
    def __execute__(self, **kwargs):
        # adapt existing component source
        source_comp_lang, source_comp_payload, source_comp_raw = (
            self.__read_component_metadata__("source_component", **kwargs)
        )
        target_comp_lang, target_comp_payload, target_comp_raw = (
            self.__read_component_metadata__("target_component", **kwargs)
        )
        execute_kwargs = {
            **kwargs,
            "existing_source_component": source_comp_raw and len(source_comp_raw) > 0,
            "source_component_lang": source_comp_lang,
            "source_component_payload": source_comp_payload,
            "source_component_content": source_comp_raw,
            "existing_target_component": target_comp_raw and len(target_comp_raw) > 0,
            "target_component_lang": target_comp_lang,
            "target_component_payload": target_comp_payload,
            "target_component_content": target_comp_raw,
        }
        
        # execute as super
        return super().__execute__(**execute_kwargs)
    
    # @ override
    def __do_execute__(self, **kwargs):
        # extract arguments
        phase_step_file = kwargs_by_key(key="phase_step_file", **kwargs)
        agent_tasks = kwargs_by_key(key="agent_tasks", **kwargs)
        
        # iterate every task in day
        for sub_task in agent_tasks:
            components = sub_task['components']
            if not components or len(components) <= 0:
                self.logger.warning(f"⚠️ Step Day { self.day_num }, File { phase_step_file } has no any task components!")
                continue
            
            # parse task description
            sub_tasks = [ sub_task.get("desc") ]
            targeted_tags = sub_task.get("targeted_tags") or []
            self.logger.info("=================================================")
            self.logger.info(f"ℹ️ Do Task: {sub_tasks!s}")
            self.logger.info("=================================================")
            task_kwargs = {
                **kwargs,
                "targeted_tags": targeted_tags,
                "sub_tasks": sub_tasks
            }
            
            # iterate every target component
            for component in components:
                self.logger.info(f"➡️ Target Component: {component}")
                self.logger.info("-------------------------------------------------")
                componentParts = component.split(";")
                source_component = componentParts[0] if len(componentParts) > 1 else "INTEGRATION_SCOPE"
                target_component = componentParts[0] if 0 < len(componentParts) < 2 else componentParts[1] if len(componentParts) > 1 else ""
                task_kwargs = {
                    **task_kwargs,
                    "source_component": source_component,
                    "target_component": target_component
                }
                
                # check if invalid target component
                if len(target_component) <= 0:
                    self.logger.warning(f"⚠️ Step Day { self.day_num }, File { phase_step_file }, Target Component not found to do")
                    continue
                
                # do task component
                task_kwargs = super().__do_execute__(**task_kwargs) or {}
                
                # write AI response log
                self.write_history_log(
                    source_component=kwargs_by_key(key="source_component", **task_kwargs),
                    target_component=kwargs_by_key(key="target_component", **task_kwargs),
                    user_prompt=kwargs_by_key(key="user_prompt", **task_kwargs),
                    data=kwargs_by_key(key="raw_response", **task_kwargs),
                    append=True
                )
            
            # end components interation
        
        # end sub-tasks interation
        
        # return new values dict
        return { **kwargs }

