# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/pom.xml` (Must map to sources/backend/ or sources/frontend/)
*   Context Module Context Reference Path: `INTEGRATION_SCOPE`

### SOURCE CODE UNDER AUDIT (VERIFICATION TARGET)
* **Target Code Component Payload For Comprehensive Review:**
<EXISTING_CODE_UNDER_AUDIT>

</EXISTING_CODE_UNDER_AUDIT>


### ❌ REAL RAW COMPILER ERROR LOGS (CRITICAL FIX TARGET)
The codebase above triggered the following compiler or runtime exceptions. You MUST analyze this stack trace or log error text to pinpoint and auto-patch the root cause:
```text
True
```
*   Operational Modality Activated: COMPILER_FIXER_MODE


### 📋 EXECUTION SUB-TASKS TO ENFORCE
['Thực hiện kiểm tra chất lượng toàn bộ descriptor build cha-con đã được tạo tại các tác vụ con 1.1 đến 1.5. Xác minh thẻ parent của mỗi descriptor con trỏ chính xác về groupId, artifactId, version của descriptor cha, đảm bảo không sai lệch namespace. Rà soát dependencyManagement để đảm bảo không khai báo trùng lặp phiên bản Spring Boot, Spring Cloud, Flyway, PostgreSQL Driver. Kiểm tra plugin spring-boot-maven-plugin được khai báo đầy đủ tại tất cả descriptor con với mainClass trỏ chính xác về lớp ứng dụng tương ứng. Xác nhận plugin flyway-maven-plugin được cấu hình đồng nhất tại tất cả module có di trú schema. Phát hiện xung đột version giữa các module và đề xuất chiến lược khắc phục bằng cách khoá version trong properties của descriptor cha. Đối chiếu tất cả mã định danh truy vết [ARC-000] xuất hiện đúng một lần tại mỗi descriptor. Tạo báo cáo review ghi nhận điểm đạt và điểm cần cải thiện.']

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. 

Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, merge them seamlessly with any pre-existing tags inside the source payload, and permanently inject/preserve those exact Tag IDs within the generated code's Javadoc documentation or file header comments. The final code must be completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<project>
    <model>
        <groupId>org.nlh4j.socialscheduler</groupId>
        <artifactId>user-service</artifactId>
        <version>1.0.0</version>
        <name>user-service</name>
        <packaging>jar</packaging>
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>3.3.5</version>
            <relativePath/>
        </parent>
        <properties>
            <java.version>21</java.version>
            <spring-boot.version>3.3.5</spring-boot.version>
            <spring-cloud.version>2023.0.3</spring-cloud.version>
            <flyway.version>10.20.1</flyway.version>
            <postgresql.version>42.7.5</postgresql.version>
            <kafka.version>3.7.0</kafka.version>
            <redis.version>7.0.0</redis.version>
            <bucket4j.version>8.10.1</bucket4j.version>
            <openai.version>0.18.0</openai.version>
            <testcontainers.version>1.19.8</testcontainers.version>
        </properties>
        <dependencyManagement>
            <dependencies>
                <!-- Spring Boot Starters -->
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-dependencies</artifactId>
                    <version>${spring-boot.version}</version>
                    <type>pom</type>
                </dependency>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>${spring-cloud.version}</version>
                    <type>pom</type>
                </dependency>
                <!-- Database -->
                <dependency>
                    <groupId>org.postgresql</groupId>
                    <artifactId>postgresql</artifactId>
                    <version>${postgresql.version}</version>
                </dependency>
                <dependency>
                    <groupId>org.flyway</groupId>
                    <artifactId>flyway-core</artifactId>
                    <version>${flyway.version}</version>
                </dependency>
                <!-- Kafka -->
                <dependency>
                    <groupId>org.apache.kafka</groupId>
                    <artifactId>kafka-clients</artifactId>
                    <version>${kafka.version}</version>
                </dependency>
                <!-- Redis -->
                <dependency>
                    <groupId>io.lettuce</groupId>
                    <artifactId>lettuce-core</artifactId>
                    <version>${redis.version}</version>
                </dependency>
                <dependency>
                    <groupId>com.bucket4j</groupId>
                    <artifactId>bucket4j-core</artifactId>
                    <version>${bucket4j.version}</version>
                </dependency>
                <dependency>
                    <groupId>com.bucket4j</groupId>
                    <artifactId>bucket4j-redis</artifactId>
                    <version>${bucket4j.version}</version>
                </dependency>
                <!-- OpenAI -->
                <dependency>
                    <groupId>com.theokanning</groupId>
                    <artifactId>openai-java</artifactId>
                    <version>${openai.version}</version>
                </dependency>
                <!-- Testcontainers -->
                <dependency>
                    <groupId>org.testcontainers</groupId>
                    <artifactId>testcontainers-bom</artifactId>
                    <version>${testcontainers.version}</version>
                    <type>pom</type>
                </dependency>
            </dependencies>
        </dependencyManagement>
        <dependencies>
            <!-- Spring Boot Web -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-web</artifactId>
            </dependency>
            <!-- Spring Boot Data JPA -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-data-jpa</artifactId>
            </dependency>
            <!-- Spring Boot Security -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-security</artifactId>
            </dependency>
            <!-- Spring Boot OAuth2 Resource Server -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
            </dependency>
            <!-- Spring Boot Validation -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-validation</artifactId>
            </dependency>
            <!-- Flyway -->
            <dependency>
                <groupId>org.flyway</groupId>
                <artifactId>flyway-core</artifactId>
            </dependency>
            <!-- PostgreSQL Driver -->
            <dependency>
                <groupId>org.postgresql</

