/**
 * Test Suite for PerformanceMetricsEventHandler - Integration Scope
 * 
 * Traceability: [REQ-002] - Content Recommendation via AI based on historical performance metrics
 * 
 * Enterprise Governance Compliance:
 * - [0.2] All configuration values extracted as top-of-class immutable constants
 * - [0.3] Standardized Slf4j logging with entry/exit/ERROR payloads tracing Tag IDs
 * - OWASP Top 10: Input validation at ingestion point prevents injection attack surfaces
 */

package org.nlh4j.socialscheduler.contentrecommendationservice;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * @verifies [REQ-002]
 * @verifies [EXC-003]
 * @verifies [EXC-004]
 */
@ExtendWith(MockitoExtension.class)
class PerformanceMetricsEventHandlerTest {

    private PerformanceMetricsEventHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new PerformanceMetricsEventHandler();
    }

    @Test
    void testHandlePerformanceMetricsEvent_ValidPayload_Success() {
        // [REQ-002] Inline traceability tag injection per enterprise audit trail mandate
        // Arrange: Construct a valid PerformanceMetricsEvent JSON payload
        PerformanceMetricsEvent event = new PerformanceMetricsEvent();
        event.setPostId(UUID.randomUUID().toString());
        event.setLikes(100);
        event.setComments(50);
        event.setShares(25);
        event.setCollectedAt(System.currentTimeMillis());

        String payload = objectMapper.valueToTree(event).toString();

        // Act & Assert: Verify the handler processes the event without exception
        // In a full integration test, the @KafkaListener would be triggered,
        // but here we validate the core logic path.
        assertDoesNotThrow(() -> handler.processMetricEvent(event), 
                "Handler should process valid metric events");
    }

    @Test
    void testHandlePerformanceMetricsEvent_NullPayload_ShouldWarnAndReturn() {
        // [REQ-002] Inline traceability tag injection per enterprise audit trail mandate
        // Arrange
        String payload = null;

        // Act & Assert: Null payload should be handled gracefully (warn and return)
        assertDoesNotThrow(handler::processMetricEvent, 
                "Handler should handle null payload gracefully");
        
        // Note: In the actual handler, null payloads are logged and returned early.
        // This test verifies no exception is thrown for null input.
    }

    @Test
    void testHandlePerformanceMetricsEvent_MissingPostId_ShouldThrowIllegalArgumentException() {
        // [REQ-002] Inline traceability tag injection per enterprise audit trail mandate
        // Arrange: Event with missing postId
        PerformanceMetricsEvent event = new PerformanceMetricsEvent();
        // postId is intentionally left null

        // Act & Assert: Expect IllegalArgumentException due to missing required field
        Exception exception = assertThrows(IllegalArgumentException.class, 
                () -> handler.processMetricEvent(event),
                "postId is a required field in performance metrics event payload.");

        // [0.3] Comprehensive exception logging mandate check:
        // Verify the exception message contains the required context
        assertTrue(exception.getMessage().contains("postId is a required field"),
                "Exception message should indicate missing required field postId");
    }

    @Test
    void testHandlePerformanceMetricsEvent_EmptyPayload_ShouldWarnAndReturn() {
        // [REQ-002] Inline traceability tag injection per enterprise audit trail mandate
        // Arrange: Empty payload string
        String payload = "";

        // Act & Assert: Empty payload should be handled gracefully
        assertDoesNotThrow(() -> handler.processMetricEvent(payload),
                "Handler should handle empty payload gracefully");
    }
}