# .ai/.agents/.sub-agents/agent-fixer.py
import os
import subprocess
import xml.etree.ElementTree as ET

import yaml

# super agent
from _0d_ai._0d_agents._0d_sub_0u_agents.agent_0u_super import AbstractSubAgent

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
    parse_args,
    resolve_absolute_path,
)
from jproperties import Properties

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
AGENT_ID                    = "Reviewer"
AGENT_NAME                  = "🤖🛠️ EnterpriseCodeReviewerAgent"
SYSTEM_PROMPT_FILE          = resolve_absolute_path(".ai/.agents/.sub_agents/prompts/agent_reviewer.prompt.system.md")
USER_PROMPT_FILE            = resolve_absolute_path(".ai/.agents/.sub_agents/prompts/agent_reviewer.prompt.user.md")
BACKEND_WORKSPACE           = resolve_absolute_path("sources/backend")
FRONTEND_WORKSPACE          = resolve_absolute_path("sources/frontend")

class BugFixerAgent(AbstractSubAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            agent_name=AGENT_NAME,
            phase_str=phase_str,
            day_num=day_num
        )
    
    def run_compile_check(self, target_path, check_by_compile):
        # parse file extension (ex: '.sql', '.json')
        file_name = os.path.basename(target_path).lower()
        _, file_extension = os.path.splitext(target_path.lower())
        if not check_by_compile:
            check_by_compile = (file_name == 'pom.xml' or file_name == 'package.json')
        
        # if SQL
        if file_extension == '.sql':
            # use sqlfluff (linter to check SQL, need `pip install sqlfluff`)
            # --dialect ansi to check syntax SQL following global standards
            result = subprocess.run([ "sqlfluff", "lint", target_path, "--dialect", "ansi" ], capture_output=True, text=True, timeout=120, check=False)
            if not check_by_compile:
                return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        # if YAML, YML file
        elif file_extension in ['.yaml', '.yml']:
            # need `pip install PyYAML`
            try:
                with open(target_path, 'r', encoding='utf-8') as f:
                    yaml.safe_load(f) # read YAML, YML file
                if not check_by_compile:
                    return (True, "YAML: Cú pháp hoàn toàn hợp lệ.")
            except yaml.YAMLError as e:
                return (False, f"YAML Syntax Error:\n{exception_stacktrace(e)}")
        
        # if XML file
        elif file_extension == '.xml' and file_name != 'pom.xml':
            # Python lib, no need to install
            try:
                ET.parse(target_path) # read XML
                if not check_by_compile:
                    return (True, "XML: XML Syntax correct.")
            except ET.ParseError as e:
                return (False, f"XML Syntax Error: {exception_stacktrace(e)}")
        
        # if properties file
        elif file_extension == '.properties' or file_extension == '.env':
            # need `pip install jproperties`
            try:
                configs = Properties()
                with open(target_path, 'rb') as f: # format properties, read under byte
                    configs.load(f)
                if not check_by_compile:
                    return (True, "Properties: Syntax correct.")
            except Exception as e:
                return (False, f"Properties Syntax Error: {exception_stacktrace(e)}")
        
        # if file JSON
        elif file_extension == '.json':
            # use Python lib to parse, no need to call subprocess
            try:
                with open(target_path, 'r', encoding='utf-8') as f:
                    import json
                    json.load(f)
                if not check_by_compile:
                    return (True, "JSON Validated Successfully")
            except Exception as e:
                return (False, f"JSON Syntax Error: {exception_stacktrace(e)}")
        
        # check by build project
        pom_path = os.path.join(BACKEND_WORKSPACE, "pom.xml")
        package_path = os.path.join(FRONTEND_WORKSPACE, "package.json")
        if "backend" in target_path and os.path.exists(pom_path):
            # build to check error
            result = subprocess.run(["mvn", "clean", "test-compile"], cwd=BACKEND_WORKSPACE, capture_output=True, text=True, timeout=120, check=False)
            # return compile result
            return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        elif "frontend" in target_path and os.path.exists(package_path):
            # build to check error
            result = subprocess.run(
                ["npm", "run", "build"],
                cwd=FRONTEND_WORKSPACE,
                capture_output=True,
                text=True,
                timeout=120,
                check=False,
            )
            # return compile result
            return (result.returncode == 0, result.stdout + "\n" + result.stderr)
        
        elif "backend" in target_path:
            return (True, "Project hasn't been initialized yet. Not found project main component: pom.xml")
        
        else:
            return (True, "Project hasn't been initialized yet. Not found project main component: package.json")
    
    def check_project_initialized(self, target_component):
        if "backend" in target_component:
            pom_path = os.path.join(BACKEND_WORKSPACE, "pom.xml")
            # if not found pom.xml, it means project empty or be initializing
            return (os.path.exists(pom_path), pom_path)
        else:
            package_path = os.path.join(FRONTEND_WORKSPACE, "package.json")
            # if not found package.json, it means project empty or be initializing
            return (os.path.exists(package_path), package_path)
    
    # @override
    def agent_secrets_key(self) -> str:
        pass

    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-reviewer-phase-{self.phase_str}-day-{self.day_num}.md")
    
    # @override
    def system_prompt_template(self) -> str:
        return SYSTEM_PROMPT_FILE
    
    # @override
    def user_prompt_template(self) -> str:
        return USER_PROMPT_FILE
    
    # @ override
    def __do_task_component__(self, **kwargs):
        # parse parameters
        project_name = kwargs_by_key(key="project_name", **kwargs)
        target_component = kwargs_by_key(key="target_component", **kwargs)
        
        # check whether project had been initialized
        project_initialized, project_main_component = self.check_project_initialized(target_component)
        self.logger.info(f"[ ℹ️ F.Y.I ] Project {project_name} had been initialized?. {project_initialized} - Project Main Component: {project_main_component}")
        self.logger.info(f"            - Target Component: {target_component}")
        
        # execute super
        kwargs = {
            **kwargs,
            "project_initialized": project_initialized,
            "project_main_component": project_main_component
        }
        return super().__do_task_component__(**kwargs)
    
    #def execute_task(self, project_name, global_context, day_context, source_component, target_component, sub_tasks):
    def __execute__(self, **kwargs):
        # parse arguments
        project_initialized = kwargs_by_key(key="project_initialized", **kwargs)
        # source_component = kwargs_by_key(key="source_component", **kwargs)
        target_component = kwargs_by_key(key="target_component", **kwargs)
        
        # build system prompt
        system_prompt = self.build_system_prompt(**kwargs)
        
        # test component 3 time(s)
        user_prompt = None
        latest_response = None
        success = False
        max_iterations = 3
        for iteration in range(1, max_iterations + 1):
            # only compile project when it had been initialized
            is_clean, compiler_log = self.run_compile_check(target_component, project_initialized)
            if is_clean:
                self.logger.info(f"✅ Target codebase component compiled cleanly on iteration loop: {iteration}!")
                latest_response = f"✅ Target codebase component {target_component} compiled cleanly on iteration loop {iteration}"
                success = True
                break
            
            # build user prompt
            self.logger.warning(f"⚠️ Build check failed on validation loop: {iteration}. Ingesting raw error logs...")
            self.logger.warning(f"   |__ ⚠️ Compile Error: {compiler_log}")
            user_prompt = self.build_user_prompt(**kwargs)
            
            # build new values kwargs
            kwargs = {
                **kwargs,
                "existing_error_logs": compiler_log and len(compiler_log.strip()) > 0,
                "compiler_error_logs": compiler_log,
                "system_prompt": system_prompt,
                "user_prompt": user_prompt
            }
            
            # execute AI
            try:
                kwargs = self.__ai_execute__(**kwargs)
                latest_response = kwargs_by_key(key="latest_response", **kwargs)
                success = True
            except Exception as e:
                self.logger.error(f"💀 API transaction exception caught. Swapping model: {exception_stacktrace(e)}")
                latest_response = str(e) if not latest_response else latest_response
                # rotate next model
                if not self.__rotate_next_model__():
                    success = False
                    break
        
        if not success:
            self.logger.critical("💀 Structural compiler repairs failed within maximum iteration bounds.")
        return {
            **kwargs,
            "success": success,
            "system_prompt": system_prompt,
            "user_prompt": user_prompt,
            "latest_response": latest_response,
        }

if __name__ == "__main__":
    def add_known_arguments(parser):
        parser.add_argument("--phase", required=True)
        parser.add_argument("--day", required=True)
    
    args, unknown_args = parse_args(
        description=AGENT_ID,
        parser_callback=add_known_arguments
    )
    
    print(f"🛠️ Initiating compiler analysis and automated code healing routines for Phase { args.phase } Day { args.day }...")
    BugFixerAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
