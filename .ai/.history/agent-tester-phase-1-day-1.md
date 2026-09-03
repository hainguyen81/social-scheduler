# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/infra/test/maven-build-integration.sh
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/infra/test/maven-build-integration.sh` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/infra/test/maven-build-integration.sh. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Sinh script shell POSIX-compliant tại ./sources/infra/test/maven-build-integration.sh với shebang #!/usr/bin/env bash và cờ set -euo pipefail để dừng ngay khi phát hiện lỗi. Trình tự thực thi gồm: (1) mvn -f ./sources/backend/pom.xml clean validate xác minh cấu trúc POM cha, (2) mvn -f ./sources/backend/pom.xml dependency:resolve kiểm tra khả năng tải dependency từ Maven Central, (3) mvn -f ./sources/backend/user-service/pom.xml compile cho dịch vụ người dùng, (4) mvn -f ./sources/backend/schedule-service/pom.xml compile cho dịch vụ lịch đăng bài, (5) mvn -f ./sources/backend/ai-service/pom.xml compile cho dịch vụ AI/ML, (6) mvn -f ./sources/backend/rate-limit-service/pom.xml compile cho dịch vụ giới hạn tỷ lệ. Mỗi bước ghi log dòng lệnh, phiên bản Maven mvn --version và trạng thái thoát. Trả về mã thoát 0 khi tất cả descriptor biên dịch sạch, mã thoát 1 khi có lỗi. Thực thi chmod +x sau khi tạo tệp và tích hợp báo cáo thời gian bằng time cho mỗi module.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```bash
#!/usr/bin/env bash
# [ARC-000] Integration test for Maven build validation of social-scheduler backend modules
set -euo pipefail
IFS=$'
\t'

# Function to log commands and execute them
run_cmd() {
    local cmd="$*"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] Running: $cmd"
    # Capture version if mvn
    if [[ "$cmd" == mvn* ]]; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] Maven version:"
        mvn --version
    fi
    # Execute command
    if ! $cmd; then
        local exit_code=$?
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] Command failed with exit code $exit_code"
        return $exit_code
    fi
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] Command completed with exit code $?"
}

# Step 1: Validate parent pom.xml structure
run_cmd mvn -f ./sources/backend/pom.xml clean validate

# Step 2: Resolve dependencies for the whole project
run_cmd mvn -f ./sources/backend/pom.xml dependency:resolve

# Step 3: Compile user-service
run_cmd mvn -f ./sources/backend/user-service/pom.xml compile

# Step 4: Compile schedule-service
run_cmd mvn -f ./sources/backend/schedule-service/pom.xml compile

# Step 5: Compile ai-service
run_cmd mvn -f ./sources/backend/ai-service/pom.xml compile

# Step 6: Compile rate-limit-service
run_cmd mvn -f ./sources/backend/rate-limit-service/pom.xml compile

echo "[$(date +'%Y-%m-%d %H:%M:%S')] All Maven build steps completed successfully."
```

