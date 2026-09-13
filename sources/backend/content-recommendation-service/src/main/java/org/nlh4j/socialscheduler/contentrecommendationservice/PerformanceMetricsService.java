```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for AI-powered content recommendation based on historical performance metrics.
 * <p>
 * This component implements the core recommendation engine that analyzes past post performance
 * (likes, comments, shares) to suggest optimal content for future scheduling.
 * The algorithm ranks posts by an aggregated engagement score and returns the top-N recommendations.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Retrieve performance metrics for a given user's posts.</li>
 *   <li>Apply a deterministic AI-inspired ranking algorithm (engagement score).</li>
 *   <li>Map ranked metrics back to {@link Schedule} entities for downstream processing.</li>
 *   <li>Provide comprehensive logging and exception handling with traceability tags.</li>
 * </ul>
 *
 * @traceability [REQ-002]
 */
@Service
public class PerformanceMetricsService {

    // Enterprise‑grade logger for audit and monitoring
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsService.class);

    // Configuration constants – anti‑magic‑numbers enforcement
    /** Maximum number of recommendations to return per request. */
    public static final int DEFAULT_TOP_N_RECOMMENDATIONS = 5;
    /** Version identifier for the recommendation algorithm – aids A/B testing and rollback. */
    public static final String RECOMMENDATION_ALGORITHM_VERSION = "v1.0";

    /** Repository for accessing performance metrics data – Spring Data JPA layer. */
    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    /** Repository for retrieving schedule details – required to return full post context. */
    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * Generates AI‑driven content recommendations for a specific user.
     * <p>
     * The method follows a strict three‑phase pipeline:
     * <ol>
     *   <li><b>Data Ingestion</b> – Fetch all {@link PerformanceMetrics} belonging to the user.</li>
     *   <li><b>Scoring & Ranking</b> – Compute an engagement score
     *       ({@code score = likes * 3 + comments * 2 + shares * 5}) and sort descending.</li>
     *   <li><b>Result Compilation</b> – Map the top‑N ranked metrics back to {@link Schedule}
     *       entities and return them.</li>
     * </ol>
     *
     * @param userId   Identifier of the user for whom recommendations are required.
     * @param topN     Optional limit on the number of recommendations; defaults to
     *                {@link #DEFAULT_TOP_N_RECOMMENDATIONS} if {@code <= 0}.
     * @return A list of {@link Schedule} objects representing the highest‑performing posts.
     * @throws PerformanceMetricsServiceException If any data access or processing error occurs.
     * @traceability [REQ-002]
     */
    public List<Schedule> recommendContentForUser(Long userId, int topN) {
        // Entry‑point logging – captures the full request context for audit trails
        logger.info("[REQ-002] Initiating AI content recommendation for userId={} (topN={})", userId, topN);

        try {
            // Normalize topN – enforce business rule that topN must be positive
            int limit = (topN > 0) ? topN : DEFAULT_TOP_N_RECOMMENDATIONS;

            // Phase 1: Retrieve raw performance metrics for the user
            List<PerformanceMetrics> metrics = performanceMetricsRepository
                    .findByScheduleUserId(userId);

            if (metrics == null || metrics.isEmpty()) {
                logger.info("[REQ-002] No performance metrics found for userId={}; returning empty recommendation list.", userId);
                return Collections.emptyList();
            }

            // Phase 2: Apply deterministic AI‑inspired ranking algorithm
            // Engagement score formula – weighted sum of interactions to emulate AI ranking
            List<PerformanceMetrics> rankedMetrics = metrics.stream()
                    .sorted((m1, m2) -> {
                        long score1 = m1.getLikes() * 3L + m1.getComments() * 2L + m1.getShares() * 5L;
                        long score2 = m2.getLikes() * 3L + m2.getComments() * 2L + m2.getShares() * 5L;
                        return Long.compare(score2, score1); // descending order
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            // Phase 3: Map ranked metrics back to full Schedule entities for downstream use
            List<Schedule