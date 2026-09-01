### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: {{ project_name_safe }}
*   Enforced Java Package Prefix Base: org.nlh4j.{{ project_name | lower | replace(" ", "") | replace("_", "") | replace("-", "") }}
*   Target Documentation Destination Path: `{{ target_component }}`

{% if existing_target_component %}
### ENTERPRISE DOCUMENTATION RECOVERY WORKSPACE
* **Target Document Disk Status:** INCREMENTAL_MAINTENANCE_APPEND
* **Current Living Document Content:**
<EXISTING_DOCUMENT_CONTENT>
{{ target_component_payload }}
</EXISTING_DOCUMENT_CONTENT>
{% endif %}

{% if existing_source_component %}
### 📁 COMPONENT CODE & ARCHITECTURE SOURCE CONTEXT
Analyze the following live source codes, configuration parameters, or database schemas to synthesize your documentation:
{{ source_component_payload }}
{% else %}
*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)
{% endif %}

### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
{{ sub_tasks }}

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:
{% if not existing_target_component %}
Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.
{% else %}
Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above. Analyze the `<EXISTING_DOCUMENT_CONTENT>` layout, you MUST precisely insert the new architectural specifications into their logical structural sections within the current document text. Do NOT overwrite or shrink the pre-existing enterprise content blocks.
{% endif %}

Ensure that you read the exact Tag IDs from the `{{ targeted_tags }}` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
