# Content Recommendation Service Documentation
## PerformanceMetricsRepository Technical Specification
Package: `org.nlh4j.socialscheduler.contentrecommendationservice`
Interface: `PerformanceMetricsRepository` extending `JpaRepository<PerformanceMetrics, UUID>`

### Traceability Matrix Reference
| Component | Mapped Tag IDs | Description |
| :--- | :--- | :--- |
| `PerformanceMetricsRepository` | `[REQ-002]`, `[EXC-003]`, `[EXC-004]` | Data access layer for post-performance metrics; enables CRUD and query methods powering the AI recommendation engine. |
| `findById(UUID)` | `[REQ-002]`, `[EXC-003]` | Retrieves a single metrics record by ID; core for lookup operations. |
| `save(PerformanceMetrics)` | `[REQ-002]`, `[EXC-004]` | Persists new or updates existing metrics records. |
| `findByPostId(UUID)` | `[REQ-002]` | Filters metrics by associated scheduled post ID. |
| `countByPostId(UUID)` | `[REQ-002]` | Aggregation method for relevance scoring. |
| `findTop5ByOrderByCollectedAtDesc()` | `[EXC-003]`, `[EXC-004]` | Recent engagement retrieval for trend analysis; exception boundary methods. |

### Method Signatures with Javadoc Descriptions
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.nlh4j.socialscheduler.core.entity.PerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link PerformanceMetrics} entity management.
 * All operations are traceable to requirement [REQ-002] and exception protocols [EXC-003], [EXC-004].
 */
@Repository
public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetrics, UUID> {

    /**
     * Retrieves a performance metrics record by its unique identifier.
     * @param performanceId the UUID of the target record
     * @return {@link Optional} containing the entity, or empty if not found
     */
    Optional<PerformanceMetrics> findById(UUID performanceId);

    /**
     * Persists a new performance metrics record or updates an existing one.
     * @param performanceMetrics the entity to save
     * @return the saved {@link PerformanceMetrics} instance
     */
    PerformanceMetrics save(PerformanceMetrics performanceMetrics);

    /**
     * Removes the metrics record associated with the given ID.
     * @param performanceId the UUID of the record to delete
     */
    void deleteById(UUID performanceId);

    /**
     * Retrieves all performance metrics records from the database.
     * @return {@link List} of all {@link PerformanceMetrics} instances
     */
    List<PerformanceMetrics> findAll();

    /**
     * Retrieves performance metrics records filtered by scheduled post ID.
     * @param postId the UUID of the associated scheduled post
     * @return {@link List} of matching {@link PerformanceMetrics} entities
     */
    List<PerformanceMetrics> findByPostId(UUID postId);

    /**
     * Returns the total count of metrics records for a given post.
     * @param postId the UUID of the associated scheduled post
     * @return the record count as {@link Long}
     */
    Long countByPostId(UUID postId);

    /**
     * Retrieves the top 5 most recent performance metrics entries ordered by collection timestamp descending.
     * Used by the recommendation engine for trend-based scoring.
     */
    List<PerformanceMetrics> findTop5ByOrderByCollectedAtDesc();
}

### Database Schema Alignment with DDL