/**
 * Automated integration test suite for RateLimit entity business logic.
 * Validates rate limit checking, exception handling, and window reset operations
 * under direct class instantiation without mocking data layers.
 *
 * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
 */
package org.nlh4j.socialscheduler.ratelimitingservice;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitTest {

    private static final Logger testLogger = LoggerFactory.getLogger(RateLimitTest.class);

    @BeforeEach
    void setUp() {
        // Test isolation setup; logger per test instance
    }

    /**
     * Test isWithinLimit returns true when requestCount within threshold.
     * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Test
    @DisplayName("Test isWithinLimit returns true when requestCount within threshold")
    @org.junit.jupiter.api.Tag("rate-limit")
    void testIsWithinLimit_WithinThreshold() {
        RateLimit rateLimit = new RateLimit(
                UUID.randomUUID(),
                "test-endpoint",
                50,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 60000L
        );
        int maxRequests = 100;

        assertTrue(rateLimit.isWithinLimit(maxRequests),
                "isWithinLimit should return true when requestCount <= maxRequests");
        testLogger.info("[PASS] isWithinLimit passed for endpoint test-endpoint with requestCount=50");
    }

    /**
     * Test isWithinLimit throws RateLimitExceededException when exceeding threshold.
     * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Test
    @DisplayName("Test isWithinLimit throws RateLimitExceededException when exceeding threshold")
    @org.junit.jupiter.api.Tag("rate-limit")
    void testIsWithinLimit_ExceedsThreshold() {
        RateLimit rateLimit = new RateLimit(
                UUID.randomUUID(),
                "test-endpoint",
                150,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 60000L
        );
        int maxRequests = 100;

        Exception exception = assertThrows(RateLimit.RateLimitExceededException.class, () -> {
            rateLimit.isWithinLimit(maxRequests);
        });

        assertTrue(exception.getMessage().contains("Vượt quá giới hạn tỷ lệ"),
                "Exception message should indicate rate limit violation");
        testLogger.info("[PASS] isWithinLimit correctly threw RateLimitExceededException for endpoint test-endpoint");
    }

    /**
     * Test resetWindow resets requestCount and updates window timestamps.
     * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Test
    @DisplayName("Test resetWindow resets requestCount and updates window timestamps")
    @org.junit.jupiter.api.Tag("rate-limit")
    void testResetWindow_ResetsState() {
        long originalStart = System.currentTimeMillis();
        RateLimit rateLimit = new RateLimit(
                UUID.randomUUID(),
                "test-endpoint",
                50,
                originalStart,
                originalStart + 60000L
        );

        rateLimit.resetWindow(120);

        assertEquals(0, rateLimit.getRequestCount(),
                "requestCount should be reset to 0");
        assertTrue(rateLimit.getWindowStart() > originalStart,
                "windowStart should be updated to new current time");
        assertTrue(rateLimit.getWindowEnd() > rateLimit.getWindowStart(),
                "windowEnd should be windowStart + (120 * 1000)");
        testLogger.info("[PASS] resetWindow correctly reset requestCount and updated window timestamps");
    }

    /**
     * Test isWithinLimit with zero maxRequests always throws exception.
     * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Test
    @DisplayName("Test isWithinLimit with zero maxRequests always throws exception")
    @org.junit.jupiter.api.Tag("rate-limit")
    void testIsWithinLimit_ZeroMaxRequests() {
        RateLimit rateLimit = new RateLimit(
                UUID.randomUUID(),
                "test-endpoint",
                1,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 60000L
        );
        int maxRequests = 0;

        Exception exception = assertThrows(RateLimit.RateLimitExceededException.class, () -> {
            rateLimit.isWithinLimit(maxRequests);
        });

        assertTrue(exception.getMessage().contains("Vượt quá giới hạn tỷ lệ"),
                "Exception should be thrown even with zero maxRequests");
        testLogger.info("[PASS] isWithinLimit correctly threw exception for zero maxRequests");
    }
}