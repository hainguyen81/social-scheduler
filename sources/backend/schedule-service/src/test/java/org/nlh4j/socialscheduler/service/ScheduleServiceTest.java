package org.nlh4j.socialscheduler.scheduleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.socialscheduler.scheduleservice.dispatcher.SocialPlatformDispatcher;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.repository.ScheduleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link ScheduleService}.
 * Validates core business operations, security ownership checks, status transitions,
 * and external platform dispatcher integrations.
 * 
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    // [REQ-001] Top-of-class constants for test fixture generation to avoid magic literals
    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID OTHER_USER_ID = UUID.fromString("765e4321-e89b-12d3-a456-426614174999");
    private static final UUID TEST_SCHEDULE_ID = UUID.fromString("987e6543-e89b-12d3-a456-426614174111");
    private static final String TEST_PLATFORM = "FACEBOOK";
    private static final String TEST_CONTENT = "Automated test publication content for social channels.";

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SocialPlatformDispatcher platformDispatcher;

    @InjectMocks
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that creating a schedule correctly sets PENDING status, 0 retry count,
     * and persists the entity using the repository.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully create schedule with PENDING status and zero retry count")
    void testCreateSchedule_Success() {
        // [REQ-001] Prepare incoming request DTO fixture
        ScheduleRequestDto requestDto = new ScheduleRequestDto();
        requestDto.setPlatform(TEST_PLATFORM);
        requestDto.setContent(TEST_CONTENT);
        requestDto.setScheduledTime(LocalDateTime.now().plusHours(2));

        // Mock repository behavior to echo back the saved entity
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute service method under test
        ScheduleEntity createdEntity = scheduleService.createSchedule(requestDto, TEST_USER_ID);

        // Assertions verifying entity fields initialization and compliance
        assertThat(createdEntity).isNotNull();
        assertThat(createdEntity.getScheduleId()).isNotNull();
        assertThat(createdEntity.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(createdEntity.getPlatform()).isEqualTo(TEST_PLATFORM);
        assertThat(createdEntity.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(createdEntity.getStatus()).isEqualTo("PENDING");
        assertThat(createdEntity.getRetryCount()).isEqualTo(0);

        // Verify repository interaction
        verify(scheduleRepository, times(1)).save(any(ScheduleEntity.class));
    }

    /**
     * Verifies that retrieving a schedule by ID succeeds when the current user owns the schedule.
     * 
     * @verifies [REQ-001], [EXC-002]
     */
    @Test
    @DisplayName("[REQ-001][EXC-002] Should return schedule when requested by the legitimate owner")
    void testGetScheduleById_OwnerSuccess() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Execute retrieval enforcing ownership check
        ScheduleEntity result = scheduleService.getScheduleById(TEST_SCHEDULE_ID, TEST_USER_ID);

        // Verify successful retrieval
        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(TEST_SCHEDULE_ID);
        verify(scheduleRepository, times(1)).findById(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that attempting to access another user's schedule triggers an AccessDeniedException (OWASP A01).
     * 
     * @verifies [EXC-002]
     */
    @Test
    @DisplayName("[EXC-002] Should throw AccessDeniedException when non-owner attempts to access schedule")
    void testGetScheduleById_UnauthorizedAccessDenied() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Setup security context without admin privileges
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("otherUser", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        );

        // Execute and assert security exception containment
        assertThatThrownBy(() -> scheduleService.getScheduleById(TEST_SCHEDULE_ID, OTHER_USER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to this resource");

        verify(scheduleRepository, times(1)).findById(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that an administrator can successfully retrieve any user's schedule.
     * 
     * @verifies [REQ-001], [EXC-002]
     */
    @Test
    @DisplayName("[REQ-001] Should allow admin user to retrieve any schedule regardless of ownership")
    void testGetScheduleById_AdminBypassSuccess() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Setup security context with ROLE_ADMIN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("adminUser", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        // Execute retrieval as admin with different currentUserId parameter
        ScheduleEntity result = scheduleService.getScheduleById(TEST_SCHEDULE_ID, OTHER_USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that updating status to SENT successfully invokes the platform dispatcher,
     * updates the entity status to SENT, and records actual sent timestamp close to now.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully dispatch publication and update status to SENT with timestamp")
    void testUpdateStatusToSent_Success() {
        // Setup pending schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute status update to SENT
        scheduleService.updateStatusToSent(TEST_SCHEDULE_ID);

        // Verify dispatcher invocation
        verify(platformDispatcher, times(1)).dispatchPublish(entity);

        // Capture saved entity to verify state mutation
        ArgumentCaptor<ScheduleEntity> captor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository, times(1)).save(captor.capture());

        ScheduleEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus()).isEqualTo("SENT");
        assertThat(savedEntity.getActualSentTime()).isNotNull();
        assertThat(savedEntity.getActualSentTime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    /**
     * Verifies that when the platform dispatcher throws a SocialPlatformException,
     * the exception is rethrown to trigger Spring Retry mechanisms and logged accordingly [EXC-001].
     * 
     * @verifies [EXC-001]
     */
    @Test
    @DisplayName("[EXC-001] Should rethrow SocialPlatformException when dispatcher fails to trigger retry mechanism")
    void testUpdateStatusToSent_DispatcherFailureThrowsException() {
        // Setup pending schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        
        // Mock dispatcher failure simulating external network drop or API error
        doThrow(new SocialPlatformException("API rate limit exceeded or connection timeout"))
                .when(platformDispatcher).dispatchPublish(entity);

        // Execute and assert exception propagation for retry interception
        assertThatThrownBy(() -> scheduleService.updateStatusToSent(TEST_SCHEDULE_ID))
                .isInstanceOf(SocialPlatformException.class)
                .hasMessageContaining("API rate limit exceeded");

        // Verify repository save was never called due to early failure
        verify(scheduleRepository, never()).save(any(ScheduleEntity.class));
    }

    /**
     * Verifies that canceling a schedule changes its status to CANCELLED and persists the change.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully cancel schedule and persist CANCELLED status")
    void testDeleteSchedule_Success() {
        // Setup existing schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute schedule deletion/cancellation
        scheduleService.deleteSchedule(TEST_SCHEDULE_ID, TEST_USER_ID);

        // Verify status mutation and persistence
        ArgumentCaptor<ScheduleEntity> captor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository, times(1)).save(captor.capture());

        ScheduleEntity cancelledEntity = captor.getValue();
        assertThat(cancelledEntity.getStatus()).isEqualTo("CANCELLED");
    }
}