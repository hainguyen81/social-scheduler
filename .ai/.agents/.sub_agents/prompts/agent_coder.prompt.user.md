### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: {{ project_name }}
*   Enforced Java Package Prefix Base: org.nlh4j.{{ project_name | lower | replace(" ", "") | replace("_", "") | replace("-", "") }}
*   Target Component Destination Path: `{{ target_component }}`
*   Traceability Audit Tags For This Task: {{ targeted_tags }}

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION
{% if not existing_target_component %}
[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]
{% else %}
<EXISTING_SOURCE_CODE_WORKSPACE>
* **File Existence Status:** PROCOVERY_MAINTENANCE
* **Current Disk Content Base:**
<EXISTING_SOURCE_CODE>
{{ target_component_payload }}
</EXISTING_SOURCE_CODE>
</EXISTING_SOURCE_CODE_WORKSPACE>
{% endif %}

### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
{{ sub_tasks }}

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:
{% if not existing_target_component %}
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.
{% else %}
Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Analyze the `<EXISTING_SOURCE_CODE_WORKSPACE>` layout, you MUST perform an AST-level incremental insertion of the requested sub-tasks into the current file content. You ARE STRICTLY FORBIDDEN from dropping old logic.
{% endif %}

Verify that any Java file generated strictly begins with the required `org.nlh4j.{{ project_name | lower | replace(" ", "") | replace("_", "") | replace("-", "") }}` package layout. Ensure that you read the exact Tag IDs from the `{{ targeted_tags }}` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
