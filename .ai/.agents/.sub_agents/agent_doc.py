# .ai/.agents/.sub-agents/agent-doc.py
# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
# request agent_helper from `.libs/project_agents_package_loader.py`
# super agent
from _0d_ai._0d_agents._0d_sub_0u_agents.agent_0u_super import AbstractSubAgent
from _0d_ai._0d_agents.agent_0u_helper import parse_args, resolve_absolute_path

# ==============================================================================
# GLOBAL CONFIGURATION PATHS - CONFIG HERE TO CUSTOMIZE DIRECTORY STRUCTURE
# ==============================================================================
AGENT_ID                    = "Doc"
AGENT_NAME                  = "🤖✍️ EnterpriseTechnicalDocumentWriterAgent"
SYSTEM_PROMPT_FILE          = resolve_absolute_path(".ai/.agents/.sub_agents/prompts/agent_doc.prompt.system.md")
USER_PROMPT_FILE            = resolve_absolute_path(".ai/.agents/.sub_agents/prompts/agent_doc.prompt.user.md")

class DocumentationAgent(AbstractSubAgent):
    def __init__(self, phase_str, day_num):
        super().__init__(
            agent_id=AGENT_ID,
            agent_name=AGENT_NAME,
            phase_str=phase_str,
            day_num=day_num
        )
    
    # @override
    def agent_secrets_key(self) -> str:
        pass
    
    # @override
    def agent_log_file(self) -> str:
        return resolve_absolute_path(f".ai/.history/agent-doc-phase-{self.phase_str}-day-{self.day_num}.md")
    
    # @override
    def system_prompt_template(self) -> str:
        return SYSTEM_PROMPT_FILE
    
    # @override
    def user_prompt_template(self) -> str:
        return USER_PROMPT_FILE
    
    # @ override
    def clean_response(self, raw_response, **kwargs):
        # TODO Doc Agent should keep original raw response, due to it's technical document markdown
        return raw_response

if __name__ == "__main__":
    def add_known_arguments(parser):
        parser.add_argument("--phase", required=True)
        parser.add_argument("--day", required=True)
    
    args, unknown_args = parse_args(
        description=AGENT_ID,
        parser_callback=add_known_arguments
    )
    
    print(f"📝 Activating technical documentation parsing and synchronization for Phase { args.phase } Day { args.day }...")
    DocumentationAgent(
        phase_str=args.phase,
        day_num=args.day
    ).execute()
