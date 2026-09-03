# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo descriptor Maven cha tại đường dẫn ./sources/backend/pom.xml với khai báo packaging=pom, liệt kê đầy đủ các module con gồm user-service, schedule-service, ai-service, rate-limit-service và api-gateway. Khai báo thẻ parent tham chiếu tới spring-boot-starter-parent phiên bản 3.3.5 và quản lý dependencyManagement cho spring-cloud-dependencies 2023.0.3. Định nghĩa properties java.version=21, maven.compiler.source=21, maven.compiler.target=21. Khai báo dependencyManagement bao gồm Spring Boot Starter Parent 3.3.5, Spring Cloud Starter, Flyway Core 10.20.x, PostgreSQL Driver 42.7.x, Apache Kafka Client 3.7.x, Lettuce Redis Client 6.4.x, OAuth2 Resource Server 6.3.x, Bucket4j Core 8.10.x, OpenAI Java SDK 0.18.x. Đảm bảo descriptor biên dịch thành công qua lệnh mvn validate từ thư mục ./sources/backend/. Khóa cứng mọi giá trị phiên bản trong properties để đảm bảo tính nhất quán giữa các module con.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<project>
    <name>social-scheduler</name>
    <version>1.0 (Cơ sở)</version>
    <description>Khởi tạo khung dự án Microservices (Social Scheduler) với descriptor build cha-con chuẩn Spring Boot 3, Spring Cloud, Kafka client, JPA, Flyway, OAuth2 Resource Server, Redis. Di trú schema ban đầu cho bốn dịch vụ nghiệp vụ cốt lõi: user-service, schedule-service, ai-service, rate-limit-service.</description>
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <spring-boot.version>3.3.5</spring-boot.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <flyway.version>10.20.x</flyway.version>
        <postgresql.driver.version>42.7.x</postgresql.driver.version>
        <kafka.client.version>3.7.x</kafka.client.version>
        <lettuce.redis.client.version>6.4.x</lettuce.redis.client.version>
        <oauth2.resource.server.version>6.3.x</oauth2.resource.server.version>
        <bucket4j.core.version>8.10.x</bucket4j.core.version>
        <openai.java.sdk.version>0.18.x</openai.java.sdk.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot Parent -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Flyway Core -->
            <dependency>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-core</artifactId>
                <version>${flyway.version}</version>
            </dependency>

            <!-- PostgreSQL Driver -->
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.driver.version}</version>
            </dependency>

            <!-- Apache Kafka Client -->
            <dependency>
                <groupId>org.apache.kafka</groupId>
                <artifactId>kafka-clients</artifactId>
                <version>${kafka.client.version}</version>
            </dependency>

            <!-- Lettuce Redis Client -->
            <dependency>
                <groupId>io.lettuce</groupId>
                <artifactId>lettuce-core</artifactId>
                <version>${lettuce.redis.client.version}</version>
            </dependency>

            <!-- OAuth2 Resource Server -->
            <dependency>
                <groupId>org.springframework.security</groupId>
                <artifactId>spring-security-oauth2-resource-server</artifactId>
                <version>${oauth2.resource.server.version}</version>
            </dependency>

            <!-- Bucket4j Core -->
            <dependency>
                <groupId>com.bucket4j</groupId>
                <artifactId>bucket4j-core</artifactId>
                <version>${bucket4j.core.version}</version>
            </dependency>

            <!-- OpenAI Java SDK -->
            <dependency>
                <groupId>com.openai</groupId>
                <artifactId>openai-java</artifactId>
                <version>${openai.java.sdk.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <modules>
        <module>user-service</module>
        <module>schedule-service</module>
        <module>ai-service</module>
        <module>rate-limit-service</module>
        <module>api-gateway</module>
    </modules>
</project>
```

```xml
<!-- ./sources/backend/user-service/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.nlh4j.socialscheduler</groupId>
        <artifactId>social-scheduler</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>user-service</artifactId>
    <packaging>jar</packaging>

    <

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Sinh descriptor Maven con tại ./sources/backend/user-service/pom.xml với thẻ parent tham chiếu chính xác groupId, artifactId, version của descriptor cha. Khai báo artifactId=user-service, version=1.0.0, packaging=jar. Phần dependencies bao gồm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data JPA 3.3.5, Spring Boot Starter Security 3.3.5, Spring Boot Starter OAuth2 Resource Server 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, Lombok 1.18.34, Springdoc OpenAPI Starter WebMVC UI 2.6.x. Cấu hình build với spring-boot-maven-plugin 3.3.5 có mainClass trỏ về org.nlh4j.socialscheduler.userservice.UserServiceApplication và flyway-maven-plugin 10.20.x cấu hình url, user, password đọc từ biến môi trường. Đảm bảo biên dịch sạch qua mvn -f ./sources/backend/user-service/pom.xml compile và thực thi nguyên tắc OWASP A06 bằng cách khoá cứng tất cả phiên bản dependency.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<!--
   Traceability Tags: [ARC-000]
   Description: Maven descriptor for user-service module of social-scheduler project.
   This file defines parent relationship, dependencies, build plugins, and configuration for Spring Boot application.
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <!-- Model version for Maven POM. -->
    <modelVersion>4.0.0</modelVersion>

    <!-- Parent pom coordinates for social-scheduler root. -->
    <parent>
        <groupId>org.nlh4j.socialscheduler</groupId>
        <artifactId>social-scheduler</artifactId>
        <version>1.0.0</version>
    </parent>

    <!-- This module's artifact identifier and version. -->
    <artifactId>user-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <!-- Project properties: Java version, Spring Boot version, plugin versions. -->
    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.3.5</spring-boot.version>
        <flyway.version>10.20.0</flyway.version>
        <postgresql.driver.version>42.7.3</postgresql.driver.version>
        <lombok.version>1.18.34</lombok.version>
        <springdoc.version>2.6.0</springdoc.version>
        <org.slf4j.version>2.0.12</org.slf4j.version>
    </properties>

    <!-- Dependency management section to ensure consistent versions. -->
    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot core dependencies -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <!-- Flyway Core and PostgreSQL plugin -->
            <dependency>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-core</artifactId>
                <version>${flyway.version}</version>
            </dependency>
            <dependency>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-database-postgresql</artifactId>
                <version>${flyway.version}</version>
            </dependency>
            <!-- PostgreSQL JDBC driver -->
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.driver.version}</version>
            </dependency>
            <!-- Lombok for compile-time annotations -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
                <scope>provided</scope>
            </dependency>
            <!-- Springdoc OpenAPI UI -->
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- Runtime dependencies required for the user-service application. -->
    <dependencies>
        <!-- Spring Boot Web layer -->
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
        <!-- Spring Boot Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- Flyway Core for schema migrations -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <!-- PostgreSQL JDBC driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <!-- Lombok (compile-only) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <!-- Springdoc OpenAPI UI for API documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
        <!-- Test dependencies (optional but recommended) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- Build configuration: plugins for packaging and migration. -->
    <build>
        <plugins>
            <!-- Spring Boot Maven plugin to create an executable jar -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <!-- Fully qualified main class for the application -->
                    <mainClass>org.nlh4j.socialscheduler.userservice.UserServiceApplication</mainClass>
                    <!-- Enable layered jar for faster startup -->
                    <layered-jar>true</layered-jar>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Flyway Maven plugin for database migration -->
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>${flyway.version}</version>
                <configuration>
                    <!-- Database connection parameters sourced from environment variables -->
                    <url>${DB_URL}</url>
                    <user>${DB_USER}</user>
                    <password>${DB_PASSWORD}</password>
                    <!-- Enable clean and validate phases -->
                    <cleanEnabled>true</cleanEnabled>
                    <validateOnMigrate>true</validateOnMigrate>
                </configuration>
                <executions>
                    <execution>
                        <id>migrate</id>
                        <phase>validate</phase>
                        <goals>
                            <goal>migrate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Maven Compiler plugin to enforce Java version -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Sinh descriptor Maven con tại ./sources/backend/schedule-service/pom.xml với parent tham chiếu descriptor cha, artifactId=schedule-service, version=1.0.0, packaging=jar. Phần dependencies thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data JPA 3.3.5, Spring Kafka 3.2.x, Spring Data Redis (Lettuce) 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, RestClient (Spring Framework 6.1.x), Springdoc OpenAPI Starter WebMVC UI 2.6.x, Lombok 1.18.34, Resilience4j Spring Boot 3 Starter 2.2.x. Cấu hình build với spring-boot-maven-plugin có mainClass org.nlh4j.socialscheduler.scheduleservice.ScheduleServiceApplication và flyway-maven-plugin 10.20.x. Tích hợp annotation processor của Lombok trong build và cấu hình HikariCP mặc định 50 kết nối qua biến môi trường. Đảm bảo descriptor biên dịch sạch qua mvn -f ./sources/backend/schedule-service/pom.xml compile.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<properties>
    <name>social-scheduler</name>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <parent>
        <groupId>org.nlh4j.socialscheduler</groupId>
        <artifactId>social-scheduler</artifactId>
        <version>1.0.0</version>
    </parent>
    <artifactId>schedule-service</artifactId>
    <version>1.0.0</version>
    <name>social-scheduler :: schedule-service</name>
    <description>Microservice for managing social media posting schedules</description>
    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.3.5</spring-boot.version>
        <spring-kafka.version>3.2.15</spring-kafka.version>
        <spring-data-redis.version>3.3.5</spring-data-redis.version>
        <flyway.version>10.20.0</flyway.version>
        <postgresql.driver.version>42.7.3</postgresql.driver.version>
        <spring-framework.version>6.1.5</spring-framework.version>
        <springdoc-openapi.version>2.6.0</springdoc-openapi.version>
        <resilience4j.version>2.2.2</resilience4j.version>
        <lombok.version>1.18.34</lombok.version>
        <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>
        <spring-boot-maven-plugin.version>3.3.5</spring-boot-maven-plugin.version>
        <flyway-maven-plugin.version>10.20.0</flyway-maven-plugin.version>
        <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
        <maven-failsafe-plugin.version>3.2.5</maven-failsafe-plugin.version>
        <maven-resources-plugin.version>3.3.2</maven-resources-plugin.version>
        <maven-jar-plugin.version>3.3.0</maven-jar-plugin.version>
        <maven-deploy-plugin.version>3.1.1</maven-deploy-plugin.version>
        <maven-install-plugin.version>3.1.1</maven-install-plugin.version>
        <maven-source-plugin.version>3.2.1</maven-source-plugin.version>
        <maven-javadoc-plugin.version>3.5.0</maven-javadoc-plugin.version>
    </properties>
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Spring Kafka -->
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
            <version>${spring-kafka.version}</version>
        </dependency>
        <!-- Spring Data Redis (Lettuce) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Spring Boot Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!-- Flyway Core -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        <!-- Flyway PostgreSQL -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.driver.version}</version>
        </dependency>
        <!-- Spring Framework RestClient -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${spring-framework.version}</version>
        </dependency>
        <!-- Springdoc

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Sinh descriptor Maven con tại ./sources/backend/ai-service/pom.xml với parent tham chiếu descriptor cha, artifactId=ai-service, version=1.0.0, packaging=jar. Phần dependencies thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter WebFlux 3.3.5 (cho OpenAI WebClient), Spring Boot Starter Data JPA 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, OpenAI Java SDK 0.18.x, Caffeine Cache 3.1.x, Springdoc OpenAPI Starter WebMVC UI 2.6.x, Lombok 1.18.34, Resilience4j Spring Boot 3 Starter 2.2.x. Cấu hình build với spring-boot-maven-plugin có mainClass org.nlh4j.socialscheduler.aiservice.AiServiceApplication và flyway-maven-plugin 10.20.x. Kích hoạt khả năng reactive thông qua việc bao gồm WebFlux starter. Biên dịch sạch qua mvn -f ./sources/backend/ai-service/pom.xml compile.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<!-- Traceability Tags: [ARC-000] -->
<project>
  <!-- Parent pom reference for the social-scheduler enterprise suite -->
  <parent>
    <groupId>org.nlh4j.socialscheduler</groupId>
    <artifactId>social-scheduler</artifactId>
    <version>1.0.0</version>
  </parent>

  <!-- This module's artifact definition -->
  <artifactId>ai-service</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>
  <name>ai-service</name>
  <description>AI/ML microservice for content recommendation in social-scheduler project</description>

  <!-- Project-wide properties for version alignment and Java runtime -->
  <properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.5</spring-boot.version>
    <postgresql.version>42.7.2</postgresql.version>
    <flyway.version>10.20.1</flyway.version>
    <openai.version>0.18.0</openai.version>
    <caffeine.version>3.1.0</caffeine.version>
    <springdoc.version>2.6.0</springdoc.version>
    <resilience4j.version>2.2.0</resilience4j.version>
    <lombok.version>1.18.34</lombok.version>
  </properties>

  <!-- Dependency management for AI service, observability, and data access -->
  <dependencies>
    <!-- Spring Boot Web & Reactive (WebClient for OpenAI) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>

    <!-- JPA & Validation -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>

    <!-- Actuator for observability -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
      <version>${spring-boot.version}</version>
    </dependency>

    <!-- PostgreSQL JDBC driver -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>${postgresql.version}</version>
    </dependency>

    <!-- Flyway database migration -->
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
      <version>${flyway.version}</version>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
      <version>${flyway.version}</version>
    </dependency>

    <!-- OpenAI Java SDK for AI content generation -->
    <dependency>
      <groupId>com.openai</groupId>
      <artifactId>openai-java</artifactId>
      <version>${openai.version}</version>
    </dependency>

    <!-- Caffeine caching for performance -->
    <dependency>
      <groupId>com.github.ben-manes.caffeine</groupId>
      <artifactId>caffeine</artifactId>
      <version>${caffeine.version}</version>
    </dependency>

    <!-- Springdoc OpenAPI UI for API documentation -->
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>${springdoc.version}</version>
    </dependency>

    <!-- Resilience4j for circuit breaker, retry, bulkhead -->
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3-starter</artifactId>
      <version>${resilience4j.version}</version>
    </dependency>

    <!-- Lombok for boilerplate reduction -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>${lombok.version}</version>
      <scope>provided</scope>
    </dependency>

    <!-- Test utilities -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <version>${spring-boot.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <!-- Build configuration: packaging, plugins, and Flyway migration -->
  <build>
    <plugins>
      <!-- Spring Boot Maven plugin for packaging and execution -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>${spring-boot.version}</version>
        <configuration>
          <!-- Fully qualified main class for the service -->
          <mainClass>org.nlh4j.socialscheduler.aiservice.AiServiceApplication</mainClass>
          <layout>JAR</layout>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>repackage</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

      <!-- Flyway Maven plugin for schema migrations -->
      <plugin>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-maven-plugin</artifactId>
        <version>${flyway.version}</version>
        <configuration>
          <!-- Adjust connection details as per environment (Docker/K8s) -->
          <url>jdbc:postgresql://localhost:5432/socialscheduler</url>
          <user>socialscheduler</user>
          <password>secret</password>
        </configuration>
        <executions>
          <execution>
            <id>migrate</id>
            <phase>validate</phase>
            <goals>
              <goal>migrate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/pom.xml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/pom.xml`
*   Traceability Audit Tags For This Task: ['[ARC-000]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Sinh descriptor Maven con tại ./sources/backend/rate-limit-service/pom.xml với parent tham chiếu descriptor cha, artifactId=rate-limit-service, version=1.0.0, packaging=jar. Phần dependencies thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data Redis (Lettuce) 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, Bucket4j Core 8.10.x, Bucket4j Redis 8.10.x (Lettuce integration), Lombok 1.18.34, Springdoc OpenAPI Starter WebMVC UI 2.6.x. Cấu hình build với spring-boot-maven-plugin có mainClass org.nlh4j.socialscheduler.ratelimitservice.RateLimitServiceApplication và flyway-maven-plugin 10.20.x. Đảm bảo dependency Bucket4j Redis tích hợp chính xác để hỗ trợ giải thuật Token Bucket phân tán. Biên dịch sạch qua mvn -f ./sources/backend/rate-limit-service/pom.xml compile.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-000]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Traceability Tag: [ARC-000] -->
    <parent>
        <groupId>org.nlh4j.socialscheduler</groupId>
        <artifactId>social-scheduler</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>rate-limit-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>rate-limit-service</name>
    <description>Rate limiting service for social-scheduler using Redis token bucket</description>

    <properties>
        <!-- Core Spring Boot version aligned with project standards -->
        <java.version>21</java.version>
        <spring-boot.version>3.3.5</spring-boot.version>
        <!-- Bucket4j version for distributed token bucket algorithm -->
        <bucket4j.version>8.10.0</bucket4j.version>
        <!-- Flyway version for database migration -->
        <flyway.version>10.20.0</flyway.version>
        <!-- PostgreSQL JDBC driver version -->
        <postgresql.version>42.7.3</postgresql.version>
        <!-- Lombok version for boilerplate reduction -->
        <lombok.version>1.18.34</lombok.version>
        <!-- Springdoc OpenAPI UI version for API documentation -->
        <springdoc.version>2.6.0</springdoc.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web layer – provides RESTful endpoints and HTTP handling -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>

        <!-- Spring Boot Data Redis (Lettuce) – integrates Redis as a caching and rate‑limit store -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>

        <!-- Spring Boot Validation – enforces request payload contracts using Jakarta Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>

        <!-- Spring Boot Actuator – exposes health, metrics, and info endpoints for monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>

        <!-- Flyway Core – database migration framework -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>${flyway.version}</version>
        </dependency>

        <!-- Flyway PostgreSQL – specific support for PostgreSQL dialect -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>${flyway.version}</version>
        </dependency>

        <!-- PostgreSQL JDBC Driver – required for Flyway and runtime JDBC access -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.version}</version>
        </dependency>

        <!-- Bucket4j Core – token‑bucket rate‑limit algorithm implementation -->
        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-core</artifactId>
            <version>${bucket4j.version}</version>
        </dependency>

        <!-- Bucket4j Redis integration – enables distributed token bucket storage via Lettuce -->
        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-redis</artifactId>
            <version>${bucket4j.version}</version>
        </dependency>

        <!-- Lombok – reduces boilerplate code (getters, setters, builders, etc.) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Springdoc OpenAPI UI – generates interactive API documentation (Swagger UI) -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Test dependencies – Spring Boot test utilities and JUnit support -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>${spring-boot.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin – packages the application as an executable JAR -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <!-- Fully qualified main class for the rate‑limit service -->
                    <mainClass>org.nlh4j.socialscheduler.ratelimitservice.RateLimitServiceApplication</mainClass>
                    <layout>JAR</layout>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Flyway Maven Plugin – executes database migrations during the Maven lifecycle -->
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>${flyway.version}</version>
                <configuration>
                    <!-- Connection details – should be overridden via environment variables in production -->
                    <url>jdbc:postgresql://localhost:5432/rate_limit_db</url>
                    <user>postgres</user>
                    <password>postgres</password>
                </configuration>
                <executions>
                    <execution>
                        <phase>validate</phase>
                        <goals>
                            <goal>migrate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

