/**
 * Integration test suite for PerformanceMetricsEventHandler.
 * Validates end-to-end AI-driven content recommendation event processing
 * from Kafka topic into PostgreSQL database, ensuring correct persistence
 * of performance metrics for recommendation pipeline behavior.
 * @verifies [REQ-002]
 */
@Testcontainers
@SpringBootTest(classes = ContentRecommendationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PerformanceMetricsEventHandlerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("social_scheduler_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer("docker.io/bitnami/kafka:3.5.0")

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private PerformanceMetricsRepository metricsRepository;

    @BeforeEach
    void setUp() {
        kafkaContainer.start();
        postgres.start();
        // Reset database state before each test to ensure isolation
        metricsRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        kafkaContainer.stop();
        postgres.stop();
    }

    /**
     * Tests that a valid PerformanceMetrics event published to Kafka
     * is correctly processed and persisted into the database.
     * Validates field mapping, timestamp population, and idempotency.
     * @verifies [REQ-002]
     */
    @Test
    @SneakyThrows
    void givenValidPerformanceMetricsEvent_whenHandlerProcesses_thenDatabaseUpdated() {
        // [REQ-002] Business requirement: AI-powered content recommendation
        // must reliably persist performance metrics when events stream via Kafka.
        // Edge case strategy: Verify minimal valid event creates record;
        // duplicate event send must not increase record count (idempotency).
        // Assertion logic: Check DB row count, exact field values from event payload,
        // and automatic collected-at timestamp population by handler.

        // Arrange: Publish a valid PerformanceMetrics event to the Kafka topic
        String eventJson = "{\"postId\":\"post-001\",\"likes\":5,\"comments\":1,\"shares\":0}";
        kafkaTemplate.send("performance.metrics.events", eventJson);
        // Wait for embedded Kafka to become ready and propagate to consumers
        kafkaContainer.awaitReady(java.time.Duration.ofSeconds(10));

        // Act: Allow async event handler sufficient time to persist entity to DB
        java.util.concurrent.TimeUnit.SECONDS.sleep(3);

        // Assert: Exactly one PerformanceMetrics record should exist in database
        assertEquals(1, metricsRepository.count(),
                "DB must contain exactly one PerformanceMetrics record after single event processing. [REQ-002]");

        // Validate that all payload fields are correctly mapped to entity attributes
        var entity = metricsRepository.findTopByOrderByCollectedAtDesc()
                .orElseThrow(() -> new AssertionError("No entity found in database after event processing. [REQ-002]"));

        // Post ID must match the Kafka event payload exactly
        assertEquals("post-001", entity.getPostId(),
                "Post ID must match the sent event payload; validates event-to-entity mapping. [REQ-002]");

        // Likes count must match the event payload
        assertEquals(5, entity.getLikes(),
                "Likes count must match event payload; validates numeric field persistence. [REQ-002]");

        // Comments count must match the event payload
        assertEquals(1, entity.getComments(),
                "Comments count must match event payload; validates numeric field persistence. [REQ-002]");

        // Shares count must match the event payload
        assertEquals(0, entity.getShares(),
                "Shares count must match event payload; validates numeric field persistence. [REQ-002]");

        // Collected-at timestamp must be automatically populated by handler on ingestion
        assertNotNull(entity.getCollectedAt(),
                "Collected timestamp must be populated by handler; ensures audit trail integrity. [REQ-002]");

        // Edge case: Idempotency - sending the same event twice must not duplicate records
        kafkaTemplate.send("performance.metrics.events", eventJson);
        java.util.concurrent.TimeUnit.SECONDS.sleep(3);

        assertEquals(1, metricsRepository.count(),
                "Duplicate event send must not increase record count; handler must enforce idempotency. [REQ-002]");
    }
}