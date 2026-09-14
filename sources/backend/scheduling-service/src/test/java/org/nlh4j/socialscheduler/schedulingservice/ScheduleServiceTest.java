/*
 * ScheduleServiceTest
 * Unit tests for ScheduleService scheduling operations.
 * Traceability: @verifies [REQ-001], [EXC-001], [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Test happy path: retrieve a schedule by valid ID.
     * Business requirement: REQ-001 - System shall allow users to retrieve scheduled posts by ID.
     * Expected: Schedule entity returned successfully.
     */
    @Test
    @DisplayName("Find schedule by valid ID should return schedule")
    void findScheduleById_validId_shouldReturnSchedule() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setScheduleId(scheduleId);
        expectedSchedule.setUserId(testUserId);
        expectedSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        expectedSchedule.setContent("Test content");
        expectedSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        expectedSchedule.setStatus(ScheduleService.STATUS_PENDING);
        expectedSchedule.setCreatedAt(LocalDateTime.now());
        expectedSchedule.setUpdatedAt(LocalDateTime.now());

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(expectedSchedule));

        // Act
        Schedule result = scheduleService.findScheduleById(scheduleId);

        // Assert
        assertNotNull(result, "Schedule should be found");
        assertEquals(scheduleId, result.getScheduleId(), "Schedule ID should match");
        assertEquals(testUserId, result.getUserId(), "User ID should match");
        assertEquals(ScheduleService.PLATFORM_FACEBOOK, result.getPlatform(), "Platform should match");
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    /**
     * Test edge case: retrieve a schedule with non-existent ID.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules gracefully.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Find schedule by non-existent ID should throw ScheduleServiceException")
    void findScheduleById_nonExistentId_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.findScheduleById(nonExistentId));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test happy path: retrieve all schedules when schedules exist.
     * Business requirement: REQ-001 - System shall allow users to retrieve all scheduled posts.
     * Expected: List of schedules returned.
     */
    @Test
    @DisplayName("Find all schedules should return list of schedules")
    void findAllSchedules_shouldReturnSchedulesList() {
        // Arrange
        Schedule schedule1 = new Schedule();
        schedule1.setScheduleId(UUID.randomUUID());
        schedule1.setUserId(testUserId);
        schedule1.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        schedule1.setContent("Content 1");
        schedule1.setScheduledTime(LocalDateTime.now().plusDays(1));
        schedule1.setStatus(ScheduleService.STATUS_PENDING);
        schedule1.setCreatedAt(LocalDateTime.now());
        schedule1.setUpdatedAt(LocalDateTime.now());

        Schedule schedule2 = new Schedule();
        schedule2.setScheduleId(UUID.randomUUID());
        schedule2.setUserId(testUserId);
        schedule2.setPlatform(ScheduleService.PLATFORM_INSTAGRAM);
        schedule2.setContent("Content 2");
        schedule2.setScheduledTime(LocalDateTime.now().plusDays(2));
        schedule2.setStatus(ScheduleService.STATUS_PENDING);
        schedule2.setCreatedAt(LocalDateTime.now());
        schedule2.setUpdatedAt(LocalDateTime.now());

        List<Schedule> expectedSchedules = List.of(schedule1, schedule2);
        when(scheduleRepository.findAll()).thenReturn(expectedSchedules);

        // Act
        List<Schedule> result = scheduleService.findAllSchedules();

        // Assert
        assertNotNull(result, "Schedules list should not be null");
        assertEquals(2, result.size(), "Should return 2 schedules");
        assertTrue(result.stream().anyMatch(s -> s.getPlatform().equals(ScheduleService.PLATFORM_FACEBOOK)), 
            "Should contain Facebook schedule");
        assertTrue(result.stream().anyMatch(s -> s.getPlatform().equals(ScheduleService.PLATFORM_INSTAGRAM)), 
            "Should contain Instagram schedule");
        verify(scheduleRepository, times(1)).findAll();
    }

    /**
     * Test edge case: retrieve all schedules when no schedules exist.
     * Business requirement: REQ-001 - System should handle empty schedule list gracefully.
     * Expected: Empty list returned.
     */
    @Test
    @DisplayName("Find all schedules when none exist should return empty list")
    void findAllSchedules_emptyList_shouldReturnEmptyList() {
        // Arrange
        when(scheduleRepository.findAll()).thenReturn(List.of());

        // Act
        List<Schedule> result = scheduleService.findAllSchedules();

        // Assert
        assertNotNull(result, "Schedules list should not be null");
        assertTrue(result.isEmpty(), "Should return empty list");
        verify(scheduleRepository, times(1)).findAll();
    }

    /**
     * Test happy path: update a pending schedule successfully.
     * Business requirement: REQ-001 - System shall allow users to update scheduled posts.
     * Expected: Updated schedule returned with new content and time.
     */
    @Test
    @DisplayName("Update pending schedule should succeed")
    void updateSchedule_pendingSchedule_shouldUpdateSuccessfully() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule existingSchedule = new Schedule();
        existingSchedule.setScheduleId(scheduleId);
        existingSchedule.setUserId(testUserId);
        existingSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        existingSchedule.setContent("Original content");
        existingSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        existingSchedule.setStatus(ScheduleService.STATUS_PENDING);
        existingSchedule.setCreatedAt(LocalDateTime.now());
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        String newContent = "Updated content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(3);

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(existingSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(existingSchedule);

        // Act
        Schedule result = scheduleService.updateSchedule(scheduleId, newContent, newScheduleTime);

        // Assert
        assertNotNull(result, "Updated schedule should not be null");
        assertEquals(scheduleId, result.getScheduleId(), "Schedule ID should match");
        assertEquals(newContent, result.getContent(), "Content should be updated");
        assertEquals(newScheduleTime, result.getScheduledTime(), "Scheduled time should be updated");
        assertEquals(ScheduleService.STATUS_PENDING, result.getStatus(), "Status should remain pending");
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(existingSchedule);
    }

    /**
     * Test edge case: update a non-existent schedule.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules during update.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Update non-existent schedule should throw ScheduleServiceException")
    void updateSchedule_nonExistentSchedule_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        String newContent = "New content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(1);

        when(scheduleRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.updateSchedule(nonExistentId, newContent, newScheduleTime));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).findById(nonExistentId);
        verify(scheduleRepository, never()).save(any());
    }

    /**
     * Test edge case: update a non-pending schedule (e.g., SENT).
     * Business requirement: REQ-001 / EXC-001 - System should prevent updates to non-pending schedules.
     * Expected: ScheduleServiceException with ILLEGAL_STATE error code.
     */
    @Test
    @DisplayName("Update non-pending schedule should throw ScheduleServiceException")
    void updateSchedule_nonPendingSchedule_shouldThrowException() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule existingSchedule = new Schedule();
        existingSchedule.setScheduleId(scheduleId);
        existingSchedule.setUserId(testUserId);
        existingSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        existingSchedule.setContent("Original content");
        existingSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        existingSchedule.setStatus(ScheduleService.STATUS_SENT); // Non-pending status
        existingSchedule.setCreatedAt(LocalDateTime.now());
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        String newContent = "New content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(existingSchedule));

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.updateSchedule(scheduleId, newContent, newScheduleTime));
        
        assertEquals(ErrorCode.ILLEGAL_STATE, ex.getErrorCode(), "Error code should be ILLEGAL_STATE");
        assertTrue(ex.getMessage().contains("Only pending schedules can be updated"), 
            "Message should indicate only pending schedules can be updated");
        assertNull(ex.getCause(), "Cause should be null for illegal state scenario");
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, never()).save(any());
    }

    /**
     * Test happy path: delete a schedule successfully.
     * Business requirement: REQ-001 - System shall allow users to delete scheduled posts.
     * Expected: Schedule deleted without exception.
     */
    @Test
    @DisplayName("Delete schedule should succeed")
    void deleteSchedule_validId_shouldDeleteSuccessfully() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.existsById(scheduleId)).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById(scheduleId);

        // Act
        scheduleService.deleteSchedule(scheduleId);

        // Assert
        verify(scheduleRepository, times(1)).existsById(scheduleId);
        verify(scheduleRepository, times(1)).deleteById(scheduleId);
    }

    /**
     * Test edge case: delete a non-existent schedule.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules during deletion.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Delete non-existent schedule should throw ScheduleServiceException")
    void deleteSchedule_nonExistentSchedule_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.deleteSchedule(nonExistentId));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).existsById(nonExistentId);
        verify(scheduleRepository, never()).deleteById(any());
    }
}