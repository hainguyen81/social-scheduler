# .ai/.agents/agent_super.py

import json
import os
import sys

# for abstract class
from abc import ABC, abstractmethod

# agent helper
from _0d_ai._0d_agents.agent_0u_helper import (
    enabledLogDebug,
    exception_stacktrace,
    get_logger,
    json_loads,
    kwargs_by_key,
    merge_master_prompt,
    parseAIResponseData,
    read_json_file,
    render_prompt,
    resolve_absolute_path,
    write_file,
)

# openAI
from openai import OpenAI

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
MODELS_POOL_PATH            = resolve_absolute_path(".ai/.agents/.models/models.json")
AGENT_MASTER_PROMPTS_PATH   = resolve_absolute_path(".ai/.agents/.prompts")
MASTER_RULE_PROMPT_TEMPLATE = "prompt.rule.enterprise.governance.guardrails.md"

class AbstractAgent(ABC):
    def __init__(self, agent_id, **kwargs):
        self.agent_id = agent_id if agent_id else "Super"
        self.kwargs = kwargs or {}
        self.agent_name = self.get_kwargs(key="agent_name") or self.agent_id
        self.logger = get_logger(self.agent_name or self.agent_id)
        self.debug = self.get_kwargs(key="verbose")
        if self.debug:
            self.enabled_log_debug()
        self.secrets_key = self.agent_secrets_key()
        self.secrets = self.load_secrets(self.secrets_key)
        self.initialize()
    
    def initialize(self):
        self.models_secrets_key = self.agent_models_secrets_key()
        self.models_secrets = self.load_secrets(self.models_secrets_key)
        self.initialize_models()
    
    def initialize_models(self):
        self.models_pool = self.load_models_pool()
        self.active_model_index = 0
        self.client = None
        self.current_model_config = None
        if not self.rotate_model():
            self.logger.critical("💀 Not found any available AI models to execute!")
            sys.exit(1)
    
    def enabled_log_debug(self):
        enabledLogDebug(self.logger)
    
    def get_kwargs_by_key(self, key: str, **kwargs):
        return kwargs_by_key(key=key, **kwargs)
    
    def get_kwargs(self, key: str):
        return self.get_kwargs_by_key(key=key, **self.kwargs)
    
    def agent_models_secrets_key(self) -> str:
        return "AI_MODELS_KEYS_JSON"
    
    @abstractmethod
    def agent_secrets_key(self) -> str:
        pass
    
    def agent_secrets(self, key, defVal=None):
        return self.secrets.get(key, defVal) if self.secrets and key and len(key) > 0 else defVal
    
    def load_secrets(self, secrets_key):
        if not secrets_key or len(secrets_key) <= 0:
            self.logger.warning("⚠️ Invalid secrets key to load secrets!")
            return None
        
        # load secrets from environment
        raw_secrets = os.environ.get(secrets_key)
        if not raw_secrets:
            self.logger.critical(f"💀 The environment variable '{secrets_key}' is completely absent.")
            sys.exit(1)
        
        # parse secrets to JSON
        try:
            return json_loads(raw_secrets)
        except Exception as e:
            self.logger.critical(f"💀 Failed to parse environment '{secrets_key}' JSON string: {exception_stacktrace(e)}")
            sys.exit(1)
    
    def load_models_pool(self):
        _, models_json = read_json_file(MODELS_POOL_PATH)
        return models_json
    
    def __close_ai_client__(self):
        if self.client:
            try:
                self.client.close()
            except Exception as e:
                self.logger.error(f"⚠️ Exception while closing AI client: {exception_stacktrace(e)}")
    
    def rotate_model(self):
        if not self.models_secrets or len(self.models_secrets) <= 0:
            self.logger.warning("⚠️ Not found any models secrets to rotate!")
            return False
        
        models_pool_len = len(self.models_pool) if isinstance(self.models_pool, list) else 0
        while 0 <= self.active_model_index < models_pool_len:
            config = self.models_pool[self.active_model_index]
            # target_model_name = config["model_name"]
            # target_model_endpoint = config["api_endpoint"]
            target_model_name = config.get("model_name") if isinstance(config, dict) else None
            target_model_endpoint = config.get("api_endpoint") if isinstance(config, dict) else None
            
            self.logger.debug("==============================================")
            self.logger.debug("🔍 DEBUG: 'config':")
            try:
                self.logger.debug(json.dumps(config, indent=4, ensure_ascii=False))
            except Exception:
                self.logger.error(f"⚠️ Exception while dump 'config' json: {type(config)} - Config: {config}")
            self.logger.debug("==============================================")
            
            # If endpoint is missing, None, empty "", or just whitespaces "   ", skip it cleanly
            if not target_model_name or not target_model_endpoint or not str(target_model_endpoint).strip():
                self.logger.info(f"⚠️ Ignore this config due to invalid 'model_name': {target_model_name} or 'model_endpoint': {target_model_endpoint}")
                self.active_model_index += 1
                continue # 🔄 Immediately jumps to the next iteration of the while loop
            
            api_key = self.models_secrets.get(target_model_endpoint)
            if api_key:
                # close old AI client if existing
                self.__close_ai_client__()
                
                # start new session
                self.current_model_config = config
                self.current_model_config["api_key"]=api_key
                try:
                    self.client = self.__create_ai_client__()
                    self.logger.info(f"[ 💀 FAILOVER ENGAGED ] Successfully authenticated model: {target_model_name} | endpoint: {target_model_endpoint}")
                    return True
                except Exception as e:
                    # just ignoring exception while creating AI client, jump to next model
                    self.logger.error(f"[ ❌ Model {self.config_model_name()} | API Endpoint {self.config_api_endpoint()} ] SKip this tier, due to exception while creating AI Client: {exception_stacktrace(e)}.")
            self.active_model_index += 1
        self.logger.critical(f"💀 Exhausted all registered fallback models: model_interation {self.active_model_index} models number {len(self.models_pool)}")
        return False
    
    def __create_ai_client__(self):
        return OpenAI(api_key=self.__config_api_key__(), base_url=self.config_api_endpoint())
    
    def config_model(self):
        return self.current_model_config
    
    def __config_api_key__(self):
        config = self.config_model()
        return config.get("api_key") if config else None
    
    def config_model_name(self):
        config = self.config_model()
        return config.get("model_name") if config else None
    
    def config_api_endpoint(self):
        config = self.config_model()
        return config.get("api_endpoint") if config else None
    
    @abstractmethod
    def agent_log_file(self) -> str:
        pass
    
    def write_log(self, data, append=False):
        return write_file(
            file=self.agent_log_file(),
            data=data,
            append=append
        )
    
    def master_prompt_file(self) -> str:
            pass
    
    def master_prompt_template(self) -> str:
        return os.path.join(AGENT_MASTER_PROMPTS_PATH, self.master_prompt_file() or MASTER_RULE_PROMPT_TEMPLATE)
    
    def build_master_prompt_context(self, **kwargs):
        return { **kwargs }
    
    def build_master_prompt(self, **kwargs) -> str:
        master_prompt_context = self.build_master_prompt_context(**kwargs) or {}
        return render_prompt(self.master_prompt_template(), master_prompt_context)
    
    @abstractmethod
    def system_prompt_template(self) -> str:
        pass
    
    def build_system_prompt_context(self, **kwargs):
        return { **kwargs }
    
    def build_system_prompt(self, **kwargs) -> str:
        system_prompt_context = self.build_system_prompt_context(**kwargs)
        return render_prompt(self.system_prompt_template(), system_prompt_context)
    
    @abstractmethod
    def user_prompt_template(self) -> str:
        pass
    
    def build_user_prompt_context(self, **kwargs):
        return { **kwargs }
    
    def build_user_prompt(self, **kwargs) -> str:
        user_prompt_context = self.build_user_prompt_context(**kwargs)
        return render_prompt(self.user_prompt_template(), user_prompt_context)
    
    def agent_temperature(self):
        return 0.1
    
    def clean_response(self, raw_response, **kwargs):
        return raw_response
    
    def __communicate_ai__(self, **kwargs):
        # tracing
        system_prompt = kwargs_by_key(key="system_prompt", **kwargs)
        self.logger.debug("- 🤷 System Prompt: %s", system_prompt)
        if not system_prompt:
            self.logger.error("➡️➡️➡️ 💀 Invalid System Prompt. So the AI reponse maybe wrong your expectation!")
        user_prompt = kwargs_by_key(key="user_prompt", **kwargs)
        self.logger.debug("- 🤷 User Prompt: %s", user_prompt)
        if not user_prompt:
            self.logger.error("➡️➡️➡️ 💀 Invalid User Prompt. So the AI reponse maybe wrong your expectation!")
        
        # communicate with AI
        return self.client.chat.completions.create(
            model=self.config_model_name(),
            messages=[{
                "role": "system", "content": system_prompt
            }, {
                "role": "user", "content": user_prompt
            }],
            temperature=self.agent_temperature()
        )
    
    def __parse_ai_response__(self, response):
        return parseAIResponseData(response)
    
    def communicate(self, **kwargs):
        response = None
        raw_response = None
        
        # only rotate on communitating with AI
        success= False
        while not success:
            try:
                response = self.__communicate_ai__(**kwargs)
                # parse AI response, due to AI could return 404, at that moment, should rotate model
                raw_response = self.__parse_ai_response__(response=response) if response else None
                success = True   # success
            except Exception as e:
                self.logger.error(f"💀 Exception caught on model {self.config_model_name()}: {str(e)}")
                # rotate next model
                if not self.__rotate_next_model__():
                    raise # re-throw exception to super
        
        # remove old raw_response if existing
        kwargs.pop("raw_response", None)
        clean_response = self.clean_response(raw_response=raw_response, **kwargs) if raw_response else None
        return {
            **kwargs,
            # adapt new raw response
            "raw_response": raw_response,
            "clean_response": clean_response
        }
    
    @abstractmethod
    def process_communication(self, **kwargs):
        pass
    
    @abstractmethod
    def pre_execute(self, **kwargs):
        pass
    
    def __ai_execute__(self, **kwargs):
        # ask AI
        kwargs = self.communicate(**kwargs) or {}
        
        # process AI response
        kwargs = self.process_communication(**kwargs) or {}
        self.logger.info(f"[ ✅ Model {self.config_model_name()} | API Endpoint {self.config_api_endpoint()} ] Process successfully!")
        
        # return new values kwargs
        return { **kwargs }
    
    def __execute__(self, **kwargs):
        # agent do job
        system_prompt = None
        user_prompt = None
        success = False
        try:
            # build master prompt
            master_prompt = self.build_master_prompt(**kwargs)
            # build system prompt
            system_prompt = self.build_system_prompt(**kwargs)
            system_prompt = merge_master_prompt(master_prompt, system_prompt)
            # build user prompt
            user_prompt = self.build_user_prompt(**kwargs)
            
            # build new values kwargs
            kwargs = {
                **kwargs,
                "system_prompt": system_prompt,
                "user_prompt": user_prompt
            }
            
            # ask AI execution
            kwargs = self.__ai_execute__(**kwargs) or {}
            success = True
        except Exception as e:
            self.logger.error(f"💀 Exception caught on model {self.config_model_name()}: {exception_stacktrace(e)}")
            if not "exception" in kwargs:
                kwargs = { **kwargs, "exception": exception_stacktrace(e) }
        
        # result
        return {
            **kwargs,
            "success": success
        }
    
    def __handle_execute_exception__(self, e, **kwargs):
        self.logger.error(f"💀 Exception caught on model {self.config_model_name()}: {exception_stacktrace(e)}")
        # write log
        self.write_log(
            data=f"# Exception:\n\n{exception_stacktrace(e)}\n\n---\n\n",
            append=True
        )
    
    def __rotate_next_model__(self):
        self.active_model_index += 1
        return self.rotate_model()
    
    def __do_execute__(self, **kwargs):
        # internal execution
        kwargs = self.__execute__(**kwargs) or {}
        if not kwargs or not kwargs.get("success"):
            exception = kwargs_by_key(key="exception", **kwargs) or "!!!Unknown Exception!!!"
            raise RuntimeError(exception) # response is exception stack-trace from `__execute__`
        
        # done tasks
        return { **kwargs }
    
    def execute(self, **kwargs):
        # pre-execute
        safe_kwargs = kwargs or {}
        safe_kwargs = self.pre_execute(**safe_kwargs) or {}
        
        # execute
        try:
            # internal execution
            return self.__do_execute__(**safe_kwargs) or {}
        except Exception as e:
            self.__handle_execute_exception__(e, **safe_kwargs)
            sys.exit(1)
        finally:
            # close AI client if existing
            self.__close_ai_client__()

