package org.nlh4j.socialscheduler.scheduleservice.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.nlh4j.socialscheduler.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.dispatcher.SocialPlatformDispatcher;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleStatus;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing social media publishing schedules.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@Service
@Transactional
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SocialPlatformDispatcher platformDispatcher;
    
    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * Creates a new publishing schedule with PENDING status.
     * 
     * @param request The schedule request DTO
     * @param userId The authenticated user ID
     * @return The created schedule entity
     */
    public ScheduleResponseDto createSchedule(ScheduleRequestDto request, UUID userId) {
        logger.info("[PROCESS] Creating new schedule for User ID: {}", userId);
        
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setPlatform(request.platform());
        entity.setContent(request.content());
        entity.setScheduledTime(request.scheduledTime().toLocalDateTime());
        entity.setStatus(ScheduleStatus.PENDING);
        entity.setRetryCount(0);
        
        return scheduleMapper.toResponseDto(scheduleRepository.save(entity));
    }

    /**
     * Retrieves a schedule by ID, enforcing ownership check to prevent IDOR (OWASP A01).
     */
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleDtoById(UUID scheduleId, UUID currentUserId) {
        return scheduleMapper.toResponseDto(getScheduleById(scheduleId, currentUserId));
    }

    /**
     * Retrieves a schedule by ID, enforcing ownership check to prevent IDOR (OWASP A01).
     */
    @Transactional(readOnly = true)
    public ScheduleEntity getScheduleById(UUID scheduleId, UUID currentUserId) {
        ScheduleEntity entity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        // Enforce ownership check
        if (!entity.getUserId().equals(currentUserId) && !isAdmin()) {
            logger.warn("[SECURITY] Unauthorized access attempt by User: {} on Schedule: {}", currentUserId, scheduleId);
            throw new AccessDeniedException("Access denied to this resource");
        }
        return entity;
    }

    /**
     * Updates schedule status to SENT and records actual sent time.
     * Implements retry logic for external platform integration failures.
     */
    @Retryable(retryFor = { SocialPlatformException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void updateStatusToSent(UUID scheduleId) {
    	updateStatus(scheduleId, ScheduleStatus.SENT, null);
    }

    /**
     * Updates schedule status to SENT and records actual sent time.
     * Implements retry logic for external platform integration failures.
     */
    @Retryable(retryFor = { SocialPlatformException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void updateStatus(UUID scheduleId, ScheduleStatus status, UUID userId) {
        ScheduleEntity entity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        try {
            platformDispatcher.dispatchPublish(entity);
            entity.setStatus(status);
            entity.setActualSentTime(LocalDateTime.now());
            entity.setUserId(Objects.requireNonNullElse(userId, UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName())));
            scheduleRepository.save(entity);
            logger.info("[PROCESS] Schedule {} successfully published.", scheduleId);
        } catch (SocialPlatformException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Failed to publish schedule {}. Error: {}", scheduleId, e.getMessage());
            throw e; // Trigger retry
        }
    }

    /**
     * Cancels a schedule by setting status to CANCELLED.
     */
    public void deleteSchedule(UUID scheduleId, UUID currentUserId) {
        ScheduleEntity entity = getScheduleById(scheduleId, currentUserId);
        entity.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(entity);
        logger.info("[PROCESS] Schedule {} cancelled by User: {}", scheduleId, currentUserId);
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}