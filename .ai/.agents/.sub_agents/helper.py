# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
# request agent_helper from `.libs/project_agents_package_loader.py`
from _0d_ai._0d_agents.agent_0u_helper import write_file


def write_sub_agent_history(history_file, day, model_name, api_endpoint, source_component, target_component, prompt, data, append=False):
    log_history_content = (
        f"# Day { day }: model { model_name } - API Endpoint { api_endpoint }\n"
        f"* **Production source codebase at SOURCE destination**: {source_component}\n"
        f"* **Production source codebase generated at TARGET destination**: {target_component}\n"
        f"* **📝 Prompt / Tasks / Data**:\n{prompt}\n"
        f"* **📝 Response**:\n{data}\n\n"
    )
    return write_file(
        file=history_file,
        data=log_history_content,
        append=append
    )
