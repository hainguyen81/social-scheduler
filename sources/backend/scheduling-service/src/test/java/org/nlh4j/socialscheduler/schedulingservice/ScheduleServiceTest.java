/*
 * ScheduleServiceTest
 * Unit tests for ScheduleService scheduling operations.
 * Traceability: @verifies [REQ-001], [EXC-001], [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.nlh4j.socialscheduler.schedulingservice.exception.ScheduleServiceException;
import org.nlh4j.socialscheduler.schedulingservice.model.ErrorCode;
import org.nlh4j.socialscheduler.schedulingservice.model.Schedule;
import org.nlh4j.socialscheduler.userservice.model.User;

/**
 * Unit test suite for ScheduleService core scheduling operations.
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private UUID testUserId;
    private String validIdempotencyKey;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        validIdempotencyKey = "idempotency-key-" + System.currentTimeMillis();
        // Initialize any static state if required by the service
        ScheduleService.processedKeys.clear();
    }

    /**
     * Test happy path: schedule a post with valid inputs.
     * Business requirement: REQ-001 - System shall allow users to schedule social media posts
     * with valid platform, content, and future scheduled time.
     * Expected: Repository save called once, schedule returned with pending status.
     */
    @Test
    @DisplayName("Schedule post with valid inputs should succeed and persist entity")
    void schedulePost_validInput_shouldSaveAndReturnSchedule() {
        // Arrange: Prepare valid request parameters matching source logic constraints
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Sample post content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setScheduleId(UUID.randomUUID());
        expectedSchedule.setUserId(testUserId);
        expectedSchedule.setPlatform(platform);
        expectedSchedule.setContent(content);
        expectedSchedule.setScheduledTime(scheduleTime);
        expectedSchedule.setStatus(ScheduleService.STATUS_PENDING);
        expectedSchedule.setCreatedAt(LocalDateTime.now());
        expectedSchedule.setUpdatedAt(LocalDateTime.now());

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(expectedSchedule);

        // Act: Execute scheduling operation
        Schedule result = scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey);

        // Assert: Verify entity was persisted with correct attributes
        assertNotNull(result.getScheduleId(), "Schedule ID must not be null after successful persistence");
        assertEquals(testUserId, result.getUserId(), "User ID must match request");
        assertEquals(platform, result.getPlatform(), "Platform must match requested platform");
        assertEquals(content, result.getContent(), "Content must match requested content");
        assertEquals(ScheduleService.STATUS_PENDING, result.getStatus(), "Initial status must be PENDING");
        verify(scheduleRepository, times(1)).save(argThat(s -> 
            s.getPlatform().equals(platform) && s.getContent().equals(content)));
    }

    /**
     * Test edge case: invalid platform string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Platform must be one of Facebook/Instagram/TikTok;
     * invalid platform triggers business exception with preserved cause chain.
     */
    @Test
    @DisplayName("Invalid platform should throw ScheduleServiceException")
    void schedulePost_invalidPlatform_shouldThrowException() {
        // Arrange
        String invalidPlatform = "Twitter"; // Not in allowed set
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert: Expect IllegalArgumentException wrapped in ScheduleServiceException
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, invalidPlatform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode(), "Error code must be INVALID_INPUT");
        assertTrue(ex.getMessage().contains("Invalid platform"), "Message must indicate invalid platform");
        // Verify cause chain preservation: original IllegalArgumentException should be embedded
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must not be severed");
        assertTrue(cause.getMessage().contains("Invalid platform"), "Root cause message must contain platform validation text");
    }

    /**
     * Test edge case: empty content string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Content field must not be blank; empty content triggers exception.
     */
    @Test
    @DisplayName("Empty content should throw ScheduleServiceException")
    void schedulePost_emptyContent_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = ""; // Blank content
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Content cannot be empty"), "Message must indicate empty content constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve root exception");
        assertTrue(cause.getMessage().contains("Content cannot be empty"), "Root cause must reflect content validation");
    }

    /**
     * Test edge case: past schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Scheduled time must be in the future; past times rejected.
     */
    @Test
    @DisplayName("Past schedule time should throw ScheduleServiceException")
    void schedulePost_pastScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_TIKTOK;
        String content = "Content";
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1); // Explicitly in the past
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, pastTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must enforce future time constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must be preserved");
        assertTrue(cause.getMessage().contains("Schedule time must be in the future"), "Root cause must reflect time validation");
    }

    /**
     * Test negative path: duplicate idempotency key should throw exception.
     * Business requirement: REQ-001 / EXC-002 - System must prevent duplicate schedule submissions
     * within the idempotency window (3600 seconds).
     */
    @Test
    @DisplayName("Duplicate idempotency key should throw ScheduleServiceException")
    void schedulePost_duplicateIdempotencyKey_shouldThrowException() {
        // Arrange: First successful submission
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String duplicateKey = "duplicate-key-1";

        Schedule firstSchedule = new Schedule();
        firstSchedule.setScheduleId(UUID.randomUUID());
        firstSchedule.setUserId(testUserId);
        firstSchedule.setPlatform(platform);
        firstSchedule.setContent(content);
        firstSchedule.setScheduledTime(scheduleTime);
        firstSchedule.setStatus(ScheduleService.STATUS_PENDING);

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(firstSchedule);

        // Act: First call should succeed
        scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey);

        // Act: Second call with same key should fail
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey));

        // Assert: Exception must reference duplicate request error code
        assertEquals(ErrorCode.DUPLICATE_REQUEST, ex.getErrorCode(), "Error code must be DUPLICATE_REQUEST");
        assertTrue(ex.getMessage().contains("Duplicate schedule request detected"), 
            "Message must indicate duplicate request detection");
        // Verify cause chain: null cause is explicitly passed in service, but exception wraps business context
        assertNull(ex.getCause(), "Cause should be null as per service design when throwing duplicate exception");
    }

    /**
     * Test negative path: null schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Null schedule time violates pre-condition and triggers exception.
     */
    @Test
    @DisplayName("Null schedule time should throw ScheduleServiceException")
    void schedulePost_nullScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = "Content";
        LocalDateTime nullTime = null;
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, nullTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must handle null time as invalid");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve the NullPointerException or wrapped validation");
        assertTrue(cause.getMessage() != null && cause.getMessage().contains("Schedule time"), 
            "Root cause must reference schedule time constraint");
    }
}