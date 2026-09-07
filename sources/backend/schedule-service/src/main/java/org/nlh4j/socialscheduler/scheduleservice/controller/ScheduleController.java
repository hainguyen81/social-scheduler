package org.nlh4j.socialscheduler.scheduleservice.controller;

import java.util.UUID;

import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleStatus;
import org.nlh4j.socialscheduler.scheduleservice.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Controller handling social media scheduling operations.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 * @author Enterprise System Architect
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Creates a new publishing schedule.
     * Requires ADMIN, USER, or SCHEDULER role.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SCHEDULER')")
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody ScheduleRequestDto request) {
        // Extract authenticated user ID from SecurityContextHolder
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        
        // Delegate to service layer for business logic and persistence
        ScheduleResponseDto response = scheduleService.createSchedule(request, userId);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific schedule by ID.
     * Implements ownership check to prevent IDOR (OWASP A01).
     */
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ScheduleResponseDto> getScheduleById(@PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(scheduleService.getScheduleDtoById(scheduleId, userId));
    }

    /**
     * Updates the status of an existing schedule.
     */
    @PutMapping("/{scheduleId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID scheduleId, @RequestParam ScheduleStatus status) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        scheduleService.updateStatus(scheduleId, status, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancels/Deletes a schedule by marking it as CANCELLED.
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }
}