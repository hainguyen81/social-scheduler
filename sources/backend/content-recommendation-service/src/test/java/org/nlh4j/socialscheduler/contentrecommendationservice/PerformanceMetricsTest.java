/**
 * Integration Test Suite for PerformanceMetrics Entity, Repository, and Service.
 * Validates content recommendation performance tracking workflows against multi-component
 * infrastructure context (Testcontainers-backed PostgreSQL, Spring Boot runtime).
 * Targets: [REQ-002]](REQ-002) - AI-driven content recommendation based on historical performance.
 *          [EXC-003](EXC-003) - Exception handling for invalid/null performance data inputs.
 *          [EXC-004](EXC-004) - Exception handling and constraint safety during update/delete operations.
 * @verifies [REQ-002], [EXC-003], [EXC-004]
 */
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import re

I need to be careful about the output length and format. The instruction says: "Output purely the pristine executable payload or report block from the first character to the last character, and freeze immediately. Any token outside this technical boundary violates runtime compliance." and "Output ONLY the raw, pure requested technical document structure, schema code, or configuration code block."

So I must output ONLY the code block, nothing else. No reasoning, no analysis, no text before or after.

Let me produce a clean Java integration test that fits the constraints. But wait, the test is for a Java class, but the path is a test file. The instruction says: "Scenario B - INTEGRATION TESTING (If User Context dictates INTEGRATION_SCOPE or source code is absent): Validate multi-component interactions, end-to-end endpoint workflows, or database state updates. Do NOT mock data layers or messaging brokers. Infra Guardrail: Utilize containerized virtualization plugins (e.g., Testcontainers) or active isolated testing profiles to execute real native queries, database updates, and event-driven data streaming pipelines."

So it should be an integration test using Testcontainers. I'll write a JUnit 5 test class using Testcontainers for PostgreSQL, with Spring Boot test context.

But wait, the path is `./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsTest.java`. This is a specific path. The test should be for the content-recommendation-service.

I need to make sure the code is correct Java syntax. I'll write a test class that uses Testcontainers for PostgreSQL, autowires the repository, and tests basic CRUD and business logic. I'll include inline comments explaining the assertions and edge cases, and Javadoc with tag IDs.

Let me structure it properly: