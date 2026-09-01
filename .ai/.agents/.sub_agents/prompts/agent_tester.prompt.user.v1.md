### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: {{ project_name }}
*   Enforced Java Package Prefix Base: org.nlh4j.saas.{{ project_name }}
*   Target Test Component Destination Path: `{{ target_component }}` (Must map to sources/backend/ or sources/frontend/)

{% if source_component == "INTEGRATION_SCOPE" or not source_payload or source_payload.strip() == "" %}
### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: {{ target_component }}. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]
{% else %}
### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
{{ source_payload }}
{% endif %}

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
{{ sub_tasks }}

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite. 

Verify that any Java file generated strictly begins with the required `org.nlh4j.saas.{{ project_name }}` structure. Ensure that you read the exact Tag IDs from the `{{ targeted_tags }}` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
