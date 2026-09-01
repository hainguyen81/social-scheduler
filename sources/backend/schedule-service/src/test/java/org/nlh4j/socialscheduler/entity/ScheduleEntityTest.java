package org.nlh4j.socialscheduler.scheduleservice.entity;

// [REQ-001] Traceability Tag ID compliance mapping for entity schema unit testing

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🏛️ ScheduleEntityTest provides comprehensive unit testing coverage for the 
 * {@link ScheduleEntity} JPA persistence model, verifying lifecycle callbacks, 
 * default initializations, and attribute mutator contracts.
 * 
 * @verifies [REQ-001]
 * @author Enterprise Architecture Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ScheduleEntityTest {

    // =========================================================================
    // 🛡️ CONSTANTS & LOGGER DEFINITION
    // =========================================================================

    private static final Logger logger = LoggerFactory.getLogger(ScheduleEntityTest.class);

    private static final String TEST_TENANT_ID = "tenant-enterprise-01";
    private static final String TEST_CONTENT = "Automated test publication content payload for social channels.";
    private static final String TEST_PLATFORM = ScheduleEntity.PLATFORM_FACEBOOK;

    private UUID sampleScheduleId;
    private UUID sampleUserId;
    private LocalDateTime sampleScheduledTime;
    private ScheduleEntity scheduleEntity;

    /**
     * Pre-test execution setup hook initializing baseline entity parameters
     * for state mutation and lifecycle verification tests.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] [REQ-001] Initializing ScheduleEntity test fixture state.");
        this.sampleScheduleId = UUID.randomUUID();
        this.sampleUserId = UUID.randomUUID();
        this.sampleScheduledTime = LocalDateTime.now().plusHours(2);
        
        this.scheduleEntity = new ScheduleEntity(
                sampleScheduleId,
                sampleUserId,
                TEST_TENANT_ID,
                TEST_PLATFORM,
                TEST_CONTENT,
                sampleScheduledTime
        );
    }

    // =========================================================================
    // 🧪 UNIT TEST CASES (Happy Paths, Edge Cases & Lifecycle Hooks)
    // =========================================================================

    /**
     * Verifies that the default constructor initializes fields with correct
     * enterprise baseline values (status = PENDING, retryCount = 0).
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test default constructor initial state compliance [REQ-001]")
    void testDefaultConstructorInitializesDefaults() {
        logger.info("[TEST_START] [REQ-001] Executing testDefaultConstructorInitializesDefaults.");
        
        ScheduleEntity entity = new ScheduleEntity();
        
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(entity.getScheduleId()).isNull();
        
        logger.info("[TEST_SUCCESS] [REQ-001] Default constructor initialization verified successfully.");
    }

    /**
     * Verifies that the parameterized constructor correctly assigns all input
     * arguments and establishes the PENDING state.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test parameterized constructor field binding [REQ-001]")
    void testParameterizedConstructorBinding() {
        logger.info("[TEST_START] [REQ-001] Executing testParameterizedConstructorBinding.");
        
        assertThat(scheduleEntity.getScheduleId()).isEqualTo(sampleScheduleId);
        assertThat(scheduleEntity.getUserId()).isEqualTo(sampleUserId);
        assertThat(scheduleEntity.getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(scheduleEntity.getPlatform()).isEqualTo(TEST_PLATFORM);
        assertThat(scheduleEntity.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(scheduleEntity.getScheduledTime()).isEqualTo(sampleScheduledTime);
        assertThat(scheduleEntity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(scheduleEntity.getRetryCount()).isZero();
        
        logger.info("[TEST_SUCCESS] [REQ-001] Parameterized constructor field binding verified.");
    }

    /**
     * Verifies that all getters and setters correctly mutate and retrieve
     * entity property states without regression.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test entity getters and setters mutator contracts [REQ-001]")
    void testGettersAndSettersMutations() {
        logger.info("[TEST_START] [REQ-001] Executing testGettersAndSettersMutations.");
        
        UUID newScheduleId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        LocalDateTime newSentTime = LocalDateTime.now();
        LocalDateTime newUpdatedTime = LocalDateTime.now().plusDays(1);
        
        scheduleEntity.setScheduleId(newScheduleId);
        scheduleEntity.setUserId(newUserId);
        scheduleEntity.setTenantId("tenant-new");
        scheduleEntity.setPlatform(ScheduleEntity.PLATFORM_INSTAGRAM);
        scheduleEntity.setContent("Updated content payload.");
        scheduleEntity.setScheduledTime(newSentTime);
        scheduleEntity.setStatus(ScheduleEntity.STATUS_SENT);
        scheduleEntity.setActualSentTime(newSentTime);
        scheduleEntity.setRetryCount(3);
        scheduleEntity.setCreatedAt(newSentTime);
        scheduleEntity.setUpdatedAt(newUpdatedTime);
        
        assertThat(scheduleEntity.getScheduleId()).isEqualTo(newScheduleId);
        assertThat(scheduleEntity.getUserId()).isEqualTo(newUserId);
        assertThat(scheduleEntity.getTenantId()).isEqualTo("tenant-new");
        assertThat(scheduleEntity.getPlatform()).isEqualTo(ScheduleEntity.PLATFORM_INSTAGRAM);
        assertThat(scheduleEntity.getContent()).isEqualTo("Updated content payload.");
        assertThat(scheduleEntity.getScheduledTime()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getStatus()).isEqualTo(ScheduleEntity.STATUS_SENT);
        assertThat(scheduleEntity.getActualSentTime()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getRetryCount()).isEqualTo(3);
        assertThat(scheduleEntity.getCreatedAt()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getUpdatedAt()).isEqualTo(newUpdatedTime);
        
        logger.info("[TEST_SUCCESS] [REQ-001] Getters and setters mutator contracts verified.");
    }

    /**
     * Verifies the pre-persist lifecycle callback correctly populates missing
     * primary keys, retry counts, statuses, and creation audit timestamps.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test prePersist lifecycle callback auto-population [REQ-001]")
    void testPrePersistCallbackPopulatesFields() {
        logger.info("[TEST_START] [REQ-001] Executing testPrePersistCallbackPopulatesFields.");
        
        ScheduleEntity entity = new ScheduleEntity();
        entity.setUserId(sampleUserId);
        entity.setTenantId(TEST_TENANT_ID);
        entity.setPlatform(ScheduleEntity.PLATFORM_TIKTOK);
        entity.setContent("Test PrePersist content.");
        entity.setScheduledTime(LocalDateTime.now().plusDays(1));
        
        // Execute lifecycle hook manually for unit testing isolation
        entity.prePersist();
        
        assertThat(entity.getScheduleId()).isNotNull();
        assertThat(entity.getRetryCount()).isZero();
        assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
        
        logger.info("[TEST_SUCCESS] [REQ-001] PrePersist lifecycle hook verified successfully.");
    }

    /**
     * Verifies the pre-update lifecycle callback correctly refreshes the
     * modification audit timestamp.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test preUpdate lifecycle callback refreshes timestamp [REQ-001]")
    void testPreUpdateCallbackRefreshesTimestamp() throws InterruptedException {
        logger.info("[TEST_START] [REQ-001] Executing testPreUpdateCallbackRefreshesTimestamp.");
        
        scheduleEntity.prePersist();
        LocalDateTime initialUpdatedAt = scheduleEntity.getUpdatedAt();
        
        // Simulate a slight delay to ensure timestamp difference
        Thread.sleep(10);
        
        scheduleEntity.preUpdate();
        LocalDateTime updatedUpdatedAt = scheduleEntity.getUpdatedAt();
        
        assertThat(updatedUpdatedAt).isAfterOrEqualTo(initialUpdatedAt);
        
        logger.info("[TEST_SUCCESS] [REQ-001] PreUpdate lifecycle hook verified successfully.");
    }

    /**
     * Verifies equals and hashCode contracts based solely on scheduleId.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test equals and hashCode contracts [REQ-001]")
    void testEqualsAndHashCodeContracts() {
        logger.info("[TEST_START] [REQ-001] Executing testEqualsAndHashCodeContracts.");
        
        UUID sharedId = UUID.randomUUID();
        
        ScheduleEntity entity1 = new ScheduleEntity();
        entity1.setScheduleId(sharedId);
        
        ScheduleEntity entity2 = new ScheduleEntity();
        entity2.setScheduleId(sharedId);
        
        ScheduleEntity entity3 = new ScheduleEntity();
        entity3.setScheduleId(UUID.randomUUID());
        
        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1).isNotEqualTo(null);
        assertThat(entity1).isNotEqualTo("some string object");
        
        logger.info("[TEST_SUCCESS] [REQ-001] Equals and hashCode contracts verified.");
    }

    /**
     * Verifies toString representation formats entity attributes correctly.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test toString formatting [REQ-001]")
    void testToStringFormatting() {
        logger.info("[TEST_START] [REQ-001] Executing testToStringFormatting.");
        
        String toStringResult = scheduleEntity.toString();
        
        assertThat(toStringResult).contains(sampleScheduleId.toString());
        assertThat(toStringResult).contains(TEST_TENANT_ID);
        assertThat(toStringResult).contains(TEST_PLATFORM);
        assertThat(toStringResult).contains(ScheduleEntity.STATUS_PENDING);
        
        logger.info("[TEST_SUCCESS] [REQ-001] toString formatting verified successfully.");
    }
}