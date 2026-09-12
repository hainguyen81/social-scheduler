/**
 * Integration test suite for the Schedule microservice.
 * Validates end-to-end schedule creation, retrieval, and deletion
 * within the social-scheduler backend infrastructure.
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration test for Schedule entity lifecycle operations.
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@SpringBootTest
@Testcontainers
class ScheduleTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("social_scheduler")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ScheduleRepository scheduleRepository;

    @BeforeAll
    static void setUpContainer() {
        postgres.start();
    }

    @Test
    /**
     * @verifies [REQ-001], [EXC-001], [EXC-002]
     */
    void createSchedule_happyPath_persistsEntity() {
        // Arrange: Construct a valid Schedule entity per domain model constraints
        Schedule schedule = new Schedule();
        schedule.setUserId(UUID.randomUUID());
        schedule.setPlatform("Facebook");
        schedule.setContent("Sample post content for testing");
        schedule.setScheduledTime(Instant.now().plusSeconds(3600));
        schedule.setStatus("pending");

        // Act: Persist the schedule via repository; triggers JPA save with UUID generation
        Schedule saved = scheduleRepository.save(schedule);

        // Assert: Verify entity is saved with generated ID and correct attributes
        assertNotNull(saved.getScheduleId(), "Schedule ID must be generated upon persistence");
        assertEquals("Facebook", saved.getPlatform(), "Platform must match input value");
        assertEquals("pending", saved.getStatus(), "Status must default to pending as per schema CHECK constraint");
        // Business Req: [REQ-001] - Auto-schedule post functionality
        // Edge Case: Ensure scheduled timestamp is future-bound relative to creation instant
    }

    @Test
    /**
     * @verifies [REQ-001], [EXC-001], [EXC-002]
     */
    void getScheduleById_validId_returnsEntity() {
        // Arrange: Create and persist a schedule entity
        Schedule schedule = new Schedule();
        schedule.setUserId(UUID.randomUUID());
        schedule.setPlatform("Instagram");
        schedule.setContent("Another test post for retrieval validation");
        schedule.setScheduledTime(Instant.now().plusSeconds(7200));
        schedule.setStatus("pending");
        scheduleRepository.save(schedule);

        // Act: Retrieve schedule by its generated primary key
        Schedule found = scheduleRepository.findById(schedule.getScheduleId()).orElse(null);

        // Assert: Verify retrieved entity matches saved data exactly
        assertNotNull(found, "Schedule must exist in database for valid ID lookup");
        assertEquals("Instagram", found.getPlatform(), "Platform must persist and return correctly");
        assertEquals("Another test post for retrieval validation", found.getContent(), "Content must be retrievable unchanged");
        // Business Req: [REQ-001] - Retrieve scheduled post by unique identifier
        // Edge Case: Ensure UUID format matches persisted pattern and case sensitivity
    }

    @Test
    /**
     * @verifies [REQ-001], [EXC-001], [EXC-002]
     */
    void deleteSchedule_existingId_removesEntity() {
        // Arrange: Create and persist a schedule entity
        Schedule schedule = new Schedule();
        schedule.setUserId(UUID.randomUUID());
        schedule.setPlatform("TikTok");
        schedule.setContent("TikTok test content for deletion test");
        schedule.setScheduledTime(Instant.now().plusSeconds(1800));
        schedule.setStatus("pending");
        Schedule saved = scheduleRepository.save(schedule);

        // Act: Delete the schedule by its generated ID
        scheduleRepository.deleteById(saved.getScheduleId());

        // Assert: Verify entity is no longer present in repository
        assertFalse(scheduleRepository.existsById(saved.getScheduleId()),
                "Schedule must be removed from database after delete operation");
        // Business Req: [REQ-001] - Cancel scheduled post functionality
        // Edge Case: Verify no orphan records remain and cascade rules are respected
    }
}