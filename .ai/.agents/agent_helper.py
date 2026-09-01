# ==============================================================================
# 🛠️ ENTERPRISE PIPELINE ENVIRONMENT-BASED PATH RESOLVER
# ==============================================================================
# Programmatically retrieves the absolute root directory of the active project 
# using GitHub Actions infrastructure environment tokens instead of brittle backtracking.
# ==============================================================================

import argparse
import json
import logging
import os
import re
import sys
import traceback
from collections.abc import Generator
from pathlib import Path

from jinja2 import Environment, FileSystemLoader, meta

# to load prompt template
from jinja2 import Template as JinjaTemplate

# ==============================================================================
# 🏢 ENTERPRISE INTER-PACKAGE ROUTING LAYER
# ==============================================================================
# Programmatically appends the parent directory (.ai/.agents/) into Python's runtime
# search path array. This completely unlocks importing 'agent_helper.py'.
# ==============================================================================
CURRENT_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__)) # .ai/.agents/.sub-agents/
PARENT_AGENTS_DIR  = os.path.abspath(os.path.join(CURRENT_SCRIPT_DIR, "../")) # .ai/.agents/

# jump to `agent_helper.py` folder path
if PARENT_AGENTS_DIR not in sys.path:
    sys.path.insert(0, PARENT_AGENTS_DIR)


def merge_master_prompt(master_prompt: str, system_prompt: str) -> str:
    return (
        "<GLOBAL_GOVERNANCE_MATRIX>\n"
        f"{master_prompt}\n"
        "</GLOBAL_GOVERNANCE_MATRIX>\n\n"
        "<ACTIVE_TASK_SYSTEM_INSTRUCTION>\n"
        f"{system_prompt}\n"
        "</ACTIVE_TASK_SYSTEM_INSTRUCTION>"
    ) if master_prompt and system_prompt else system_prompt if not master_prompt else None


def parse_unknown_args_to_dict(unknown_args):
    if not unknown_args or not isinstance(unknown_args, list):
        return {}

    result = {}
    iterator = iter(unknown_args)
    for item in iterator:
        # Case 1: `--key=value`
        if '=' in item and item.startswith('-'):
            key, value = item.split('=', 1)
            result[key.lstrip('-')] = value
            
        # Case 2 & 3: starts with `-` (ex: --user or -u)
        elif item.startswith('-'):
            key = item.lstrip('-')
            try:
                # check next item whether is its value
                next_item = next(iterator)
                
                # if next item is another arguments (ex: --debug --verbose)
                if next_item.startswith('-'):
                    result[key] = True  # default this argument is boolean flag as True
                    # jump to next argument by creating new iterator mới
                    unknown_args.insert(unknown_args.index(next_item), next_item)
                    iterator = iter(unknown_args[unknown_args.index(next_item):])
                
                # else if next item is its value
                else:
                    result[key] = next_item
            except StopIteration:
                # if this's end item of list, default its value is boolean flag as True
                result[key] = True
    return result

def parse_args(description=None, parser_callback=None):
    """
    - Init parser
    - Execute `parser_callback` to `add_argument` if necessary
    - Return (known_args, unknown_args_dict)
    """
    parser = argparse.ArgumentParser(description=description)
    
    # 2. callback for `add_argument``
    if parser_callback and callable(parser_callback):
        parser_callback(parser)
        
    # 3. parse known/un-known arguments
    args, unknown_args = parser.parse_known_args()
    
    # 4. convert unknown_args from List to Dict
    unknown_args = parse_unknown_args_to_dict(unknown_args)
    
    # 5. result (known_args, unknown_dict)
    return args, unknown_args

def resolve_absolute_path(relative_target_path):
    """
    Ingests a relative path string and safely interpolates it using the absolute 
    workspace anchor provided natively by the GitHub Actions Runner environment.
    """
    # 🚀 CORE RAIL: Ingest the absolute repository root path straight from GitHub infrastructure
    # Fallback to current working directory (os.getcwd()) if executing on a local machine
    # current_directory_path = os.getcwd()
    # github_workspace = os.environ.get("GITHUB_WORKSPACE", '')
    # project_workspace = os.environ.get("PROJECT_WORKSPACE", '')
    # print(f"CURRENT WORKING DIR: { current_directory_path } | GITHUB_WORKSPACE: { github_workspace } | PROJECT_WORKSPACE: { project_workspace }")
    repo_root_path = os.environ.get("PROJECT_WORKSPACE", os.environ.get("GITHUB_WORKSPACE", os.getcwd()))
    
    # Clean up the incoming string parameters by removing leading path descriptors
    cleaned_relative_path = relative_target_path.removeprefix("./")
    
    # Synthesize the non-negotiable absolute hardware computing path destinations
    absolute_hardware_path = os.path.join(repo_root_path, cleaned_relative_path)
    
    # full path from root workspace
    return absolute_hardware_path

def json_tostring(json_data) -> str:
    return json.dumps(json_data, indent=4, ensure_ascii=False) if json_data else "- No data (None)"

def __fix_json__(data):
    return re.sub(r'("(?:[^"\\]|\\.)*")', lambda m: m.group(1).replace('\n', '\\n'), str(data).strip())

def __load_jsons__(data, silent=True):
    try:
        return json.loads(str(data))
    except Exception as e:
        if not silent:
            raise e
        else:
            # print(f"Exception while loading JSON: {str(e)}")
            return {}

def json_loads(data, silent=False):
    # try to parse json
    if not data:
        return None
    
    json_data = __load_jsons__(data=data, silent=True)
    if not json_data:
        json_data = __load_jsons__(data=__fix_json__(data), silent=silent)
    return json_data

def json_raw_content(raw_content):
    """Securely serialize input telemetry payloads into structural double-quoted strings."""
    # If the payload is already a memory object list or dictionary
    cleaned_str = str(raw_content).strip() if raw_content else None
    if isinstance(raw_content, (dict, list)):
        try:
            return json.dumps(raw_content, indent=4, ensure_ascii=False)
        except Exception:
            pass
    
    # try to parse json
    cleaned_json = json_loads(cleaned_str, silent=True)
    if cleaned_json:
        try:
            cleaned_str = json.dumps(cleaned_json, indent=4, ensure_ascii=False)
        except Exception:
            pass
    return cleaned_str

def exception_stacktrace(e) -> str:
    stacktrace = traceback.format_exception(type(e), e, e.__traceback__) if isinstance(e, (BaseException, Exception)) else None
    return None if not e else f"{e!s}: {stacktrace}" if stacktrace else str(e)

def makedirs(path):
    """
    Safely resolves the absolute directory path from any given file or folder path
    and creates the underlying directory tree structure on disk memory if it does not exist.
    Fixed the latent bug where non-existent file paths were evaluated as directories.
    """
    # Convert the raw string path into a structured Path object boundary
    target_path = Path(path)
    
    # CRITICAL FIX: If the path target explicitly contains a file extension suffix (e.g., .png, .svg)
    # or if you explicitly know it represents a target file destination, safely extract its parent directory
    if target_path.suffix or os.path.isfile(path):
        resolved_dir = target_path.parent
    else:
        resolved_dir = target_path

    # Execute atomic file system creation with native concurrency protections
    resolved_dir.mkdir(parents=True, exist_ok=True)

def write_file(file, data, dir=None, append=False):
    checked_dir = dir if dir else os.path.dirname(file)
    checked_file = os.path.basename(file) if not dir else file
    opts = "a" if append else "w"
    os.makedirs(checked_dir, exist_ok=True)
    out_path = os.path.join(checked_dir, checked_file)
    with open(out_path, opts, encoding="utf-8") as f:
        f.write(str(data).replace('\\n', '\n'))
    return out_path # full path of file

def write_json_file(file, json_data, dir=None, append=False):
    checked_dir = dir if dir else os.path.dirname(file)
    checked_file = os.path.basename(file) if not dir else file
    opts = "a" if append else "w"
    os.makedirs(checked_dir, exist_ok=True)
    out_path = os.path.join(checked_dir, checked_file)
    with open(out_path, opts, encoding="utf-8") as f:
        json.dump(json_data, f, ensure_ascii=False, indent=4)
    return out_path # full path of file

def read_json_file(file_path):
    if not os.path.exists(file_path):
        return (None, None)
    
    # read json file
    with open(file_path, "r", encoding="utf-8") as f:
        return (file_path, json.load(f))

def read_file_raw(file_path):
    if not os.path.exists(file_path):
        return (None, None)
    
    # read file
    with open(file_path, "r", encoding="utf-8") as f:
        return (file_path, f.read())

def delete_file(file):
    if os.path.exists(file):
        os.remove(file)

def jinja2_required_variables(template: str) -> set[str]:
    if not os.path.exists(template):
        return None
    
    # detect template directory
    template_dir = Path(template)
    template_file = None
    if template_dir.suffix or os.path.isfile(template):
        template_file = template_dir.name
        template_dir = template_dir.parent
    
    # due to template is directory
    else:
        return None
    env = Environment(loader=FileSystemLoader(template_dir))

    # 1. parse template to get required variables
    template_source = env.loader.get_source(env, template_file)[0]
    parsed_content = env.parse(template_source)

    # 2. get all variables in template
    return meta.find_undeclared_variables(parsed_content)

def render_prompt(template: str, context: dict) -> str:
    logger = get_logger()
    if not os.path.exists(template):
        logger.warning(f"[WARING] - Template {template} is MISSING/NOT_FOUND")
        return None
    
    # for tracing
    required_variables = jinja2_required_variables(template=template)
    context_variables = set(context.keys())
    missing_vars = [ var for var in required_variables if var not in context_variables ]
    if missing_vars and len(missing_vars) > 0:
        logger.warning(f"[WARING] - Render Template {template} maybe wrong, due to missing required variables: {missing_vars}")
    
    # read prompt template
    _, template_content = read_file_raw(template)
    
    # use jinja2 Template
    tmpl = JinjaTemplate(template_content)
    
    # substitute will throw error if missing variables, safely for production
    return tmpl.render(**context).strip()

def render_kwargs_prompt(template: str, **kwargs) -> str:
    return render_prompt(template=template, context={ **kwargs })

def regex_extract(pattern, data):
    if not pattern or not data:
        return (0, [])

    reg_pattern = re.compile(
        pattern,
        re.DOTALL,
    )
    extracted_data = reg_pattern.findall(data)
    return (len(extracted_data) if extracted_data else 0, extracted_data)

def regex_extract_by_pair_tags(tag_start: str, tag_end: str, data):
    if not tag_start and tag_end:
        return regex_extract(pattern=rf"<!--\s*{tag_end}\s*-->", data=data)
    elif tag_start:
        return regex_extract(pattern=rf"<!--\s*{tag_start}\s*-->", data=data)
    elif tag_start and tag_end:
        return regex_extract(
            pattern=rf"<!--\s*{tag_start}\s*-->(.*?)<!--\s*{tag_end}\s*-->", data=data
        )
    return (0, [])

def regex_extract_by_tag(tag: str, data):
    return regex_extract_by_pair_tags(tag_start=tag, tag_end=None, data=data)

def validateAIResponse(response):
    if not response or not hasattr(response, 'choices') or not response.choices:
        raise RuntimeError("[API Upstream Error 404]: No Response Found")
    
    # 1. Check response choices
    choices_data = response.choices
    if not isinstance(choices_data, list) or len(choices_data) <= 0:
        raise RuntimeError("[API Upstream Error 404]: Response Choices is empty/None")
    
    # parse first choice
    first_choice = choices_data[0]
    has_choice_error = hasattr(first_choice, 'error') and getattr(first_choice, 'error', None)
    has_choice_error = has_choice_error or getattr(first_choice, 'finish_reason', None) == 'error'
    has_choice_error = has_choice_error or hasattr(response, 'error')
        
    # 2. Check finish_reason or error response
    if has_choice_error:
        # parse error
        err_detail = getattr(response, 'error', None) or getattr(first_choice, 'error', {}) or getattr(response, 'error', None) or { 'code': 500, 'message': 'Unknown upstream error' }
        if isinstance(err_detail, dict):
            err_msg = err_detail.get('message', 'Unknown upstream error')
            err_code = err_detail.get('code', 500)
        else:
            err_msg = getattr(err_detail, 'message', 'Unknown upstream error')
            err_code = getattr(err_detail, 'code', 500)
        raise RuntimeError(f"[API Upstream Error {err_code}]: {err_msg}")
        
    # 3. check content whether is None (although finish_reason is `stop`)
    if not hasattr(first_choice, 'message') or not first_choice.message or getattr(first_choice.message, 'content', None) is None:
        raise ValueError("[API Upstream Error 404]: AI response content is empty/None.")
    
    # Guard against malformed message blocks or unexpected payload closures
    return first_choice

def parseAIResponseData(response):
    """
    Safely parses text responses from OpenAI completion models.
    Protects the runtime from attribute errors if content fields are blank or null.
    """
    first_choice = validateAIResponse(response)
    
    # Guard against malformed message blocks or unexpected payload closures
    message_obj = first_choice.message
    if hasattr(message_obj, 'content') and message_obj.content:
        return message_obj.content.strip()
    
    # Safe fallback if choice format changes or breaks unexpectedly
    return str(first_choice).strip()

def __normalize_raw_data__(raw_data):
    # normalize raw data by detecting the raw string type dynamically
    if isinstance(raw_data, (str, bytes)):
        raw_data = str(raw_data)
    elif hasattr(raw_data, "raw"):
        # Catches the standard static CrewOutput object
        raw_data = raw_data.raw
    elif isinstance(raw_data, (Generator, list)) or hasattr(raw_data, "__iter__"):
        # Catches CrewStream / Generator loops and aggregates tokens into a flat string
        raw_data = "".join(str(chunk) for chunk in raw_data)
    else:
        # Safe final boundary fallback
        raw_data = str(raw_data)
    return raw_data

def splitAIResponseData(raw_data):
    if not raw_data:
        return None
    
    # normalize raw data
    raw_data = __normalize_raw_data__(raw_data=raw_data)
    
    # extract by regex
    match = re.search(
        r"```(?:text|json|xml|mermaid|markdown|sql|python|code|yaml|properties|bash|java|ts|tsx)?\s*(.*?)\s*```",
        raw_data,
        re.DOTALL,
    )
    return match.group(1).strip() if match else raw_data.strip()

def splitAIResponseJsonData(raw_data):
    if not raw_data:
        return None

    # normalize raw data
    raw_data = __normalize_raw_data__(raw_data=raw_data)
    clean_json_str = raw_data.strip()
    
    # 💡 Use find() to split json block
    lower_raw = clean_json_str.lower()
    start_tag = "```json"
    end_tag = "```"
    
    if start_tag in lower_raw:
        start_idx = lower_raw.find(start_tag) + len(start_tag)
        end_idx = lower_raw.find(end_tag, start_idx)
        if end_idx != -1:
            clean_json_str = clean_json_str[start_idx:end_idx].strip()
    
    elif "```" in lower_raw:
        start_idx = lower_raw.find("```") + 3
        end_idx = lower_raw.find("```", start_idx)
        if end_idx != -1:
            clean_json_str = clean_json_str[start_idx:end_idx].strip()
    
    return clean_json_str

def parseAIResponseJsonData(response):
    """
    Extracts and deserializes raw response texts into fully validated Python dict layouts.
    Leverages non-greedy structural indexing to filter out conversational agent summaries.
    """
    # Ingest text payload through the hardened safety parser above
    raw_data = parseAIResponseData(response)
    
    if not raw_data:
        return (None, None)
        
    # Pattern 1: Targeted scan for standard markdown language JSON codeblocks
    json_match = re.search(r"```json\s*([\s\S]*?)\s*```", raw_data, re.DOTALL)
    if json_match:
        try:
            clean_json_str = json_match.group(1).strip()
            return (raw_data, json_loads(clean_json_str))
        except Exception:
            pass # Continue evaluating alternative pattern structures if parsing breaks
            
    # Pattern 2: Generic codeblock fallback without language tags
    json_match = re.search(r"```\s*([\s\S]*?)\s*```", raw_data, re.DOTALL)
    if json_match:
        try:
            clean_json_str = json_match.group(1).strip()
            return (raw_data, json_loads(clean_json_str))
        except Exception:
            pass

    # Pattern 3: Hardened bracket boundary locator leveraging non-greedy isolation
    # Fixes the broken greedy regex logic to ensure text outside the curly braces is safely ignored
    try:
        return (raw_data, json_loads(splitAIResponseJsonData(raw_data)))
    except Exception as e:
        json_match = re.search(r"(\{[\s\S]*\})", raw_data, re.DOTALL)
        if json_match:
            try:
                clean_json_str = json_match.group(1).strip()
                return (raw_data, json_loads(clean_json_str))
            except Exception:
                pass
        
        else:
            pass
            
    # Final Fallback Layer: Treat the whole string as literal plain text payload
    try:
        return (raw_data, json_loads(raw_data.strip()))
    except Exception as final_error:
        get_logger().warning(f"⚠️  [PARSER WARNING] Local string-to-json mapping failed: {final_error}")
        return (raw_data, None)

def count_files_by_pattern(dir, file_filter_pattern) -> int:
    folder_path = Path(dir).resolve()
    if not folder_path.is_dir():
        return 0
    
    file_pattern = file_filter_pattern.strip() if file_filter_pattern.strip() else "*"
    return sum(1 for item in folder_path.glob(file_pattern) if item.is_file())

def kwargs_by_key(key: str, **kwargs):
    return (kwargs or {}).get(key) if key else None


# ==============================================================================
# GLOBAL CONFIGURATION LOGGER
# ==============================================================================
# Color ANSI table of log levels
LOG_COLORS = {
    'TRACE':    '\033[90m',     # Dark Gray (Highly detailed logs)
    'DEBUG':    '\033[94m',     # Light Blue (Debugging information)
    'INFO':     '\033[92m',     # Green (Normal operational messages)
    'SUCCESS':  '\033[96m',     # Cyan (Successful operations)
    'WARNING':  '\033[93m',     # Yellow (Warnings/non-critical issues)
    'ERROR':    '\033[91m',     # Red (Errors/runtime exceptions)
    'CRITICAL': '\033[95m',     # Magenta (Critical system failures)
    'RESET':    '\033[0m'       # Reset to default terminal text color
}
LOG_EMOJIS = {
    'TRACE':    '🔍',            # Magnifying glass for deep tracing
    'DEBUG':    '⚙️',            # Gear for debugging details
    'INFO':     'ℹ️',            # Information source icon
    'SUCCESS':  '✅',            # Check mark for successful operations
    'WARNING':  '⚠️',            # Warning sign for non-critical alerts
    'ERROR':    '❌',            # Cross mark for runtime errors
    'CRITICAL': '💀'             # Police car light for critical failures
}

# Define `TRACE` level because python doesn't have it
TRACE_LEVEL_NUM = 5
logging.addLevelName(TRACE_LEVEL_NUM, "TRACE")
def trace(self, message, *args, **kws):
    if self.isEnabledFor(TRACE_LEVEL_NUM):
        self._log(TRACE_LEVEL_NUM, message, args, **kws)
logging.Logger.trace = trace

class FullColorFormatter(logging.Formatter):
    def format(self, record):
        color = LOG_COLORS.get(record.levelname, LOG_COLORS['RESET'])
        reset = LOG_COLORS['RESET']
        emoji = LOG_EMOJIS.get(record.levelname, '')
        raw_level = f"{emoji} {record.levelname}" if emoji else record.levelname
        emoji_level = f"{raw_level:<12}"
        
        # Place the color code at the very beginning and the reset code at the very end
        # This forces the entire log line to inherit the level color
        log_format = (
            f"{color}%(asctime)s [ %(name)s | {emoji_level} ] %(message)s{reset}"
        )
        
        formatter = logging.Formatter(log_format, datefmt='%Y-%m-%d %H:%M:%S')
        return formatter.format(record)

# logging configuration
# logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s]: %(message)s")
# logging.basicConfig(
#     level=logging.INFO,
#     format="%(asctime)s [ %(name)s | %(levelname)s ] %(message)s",
#     datefmt="%Y-%m-%d %H:%M:%S"
# )
def get_logger(logger_name="Helper"):
    logger = logging.getLogger(logger_name)
    logger.setLevel(logging.INFO) 
    if not logger.handlers:
        handler = logging.StreamHandler()
        handler.setFormatter(FullColorFormatter())
        logger.addHandler(handler)
    return logger

def enabledLogLevel(logger, level=logging.INFO):
    try:
        logger.setLevel(level)
        return True
    except Exception:
        return False

def enabledLogDebug(logger):
    return enabledLogLevel(logger=logger, level=logging.DEBUG)


