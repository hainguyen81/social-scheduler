### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: {{ project_name }}
*   Enforced Java Package Prefix Base: org.nlh4j.{{ project_name | lower | replace(" ", "") | replace("_", "") | replace("-", "") }}
*   Target Documentation Destination Path: `{{ target_component }}`

{% if source_payload and source_payload.strip() != "" %}
### 📁 COMPONENT CODE & ARCHITECTURE SOURCE CONTEXT
Analyze the following live source codes, configuration parameters, or database schemas to synthesize your documentation:
{{ source_payload }}
{% else %}
*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)
{% endif %}

### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
{{ sub_tasks }}

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above. 

Ensure that you read the exact Tag IDs from the `{{ targeted_tags }}` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
