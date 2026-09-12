/**
 * PerformanceMetrics entity representing post performance metrics.
 * This class captures quantitative engagement data for a scheduled post,
 * including likes, comments, shares, and the timestamp when the metrics were collected.
 * <p>
 * Traceability Tags: [REQ-002], [EXC-003], [EXC-004]
 * </p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @since 2026-09-12
 */
package org.nlh4j.socialscheduler.contentrecommendationservice;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constants for the PerformanceMetrics entity.
 * These constants are hoisted to the class crown to satisfy the Anti-Magic-Numbers policy.
 */
class PerformanceMetricsConstants {
    /** Logical entity name used for logging and auditing. */
    public static final String ENTITY_NAME = "PerformanceMetrics";
    /** Traceability tag identifiers for this component. */
    public static final String TRACEABILITY_TAGS = "[REQ-002], [EXC-003], [EXC-004]";
}

/**
 * JPA Entity mapping for the {@code performance_metrics} table.
 * Stores the quantitative engagement metrics of a post.
 */
@Entity
@Table(name = "performance_metrics", indexes = {
    @Index(name = "idx_performance_post_id", columnList = "post_id")
})
public class PerformanceMetrics {

    /** Logger instance for enterprise-grade audit tracing. */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetrics.class);

    /**
     * Unique identifier for the performance metric record.
     * <p>Generated automatically; never updated after creation.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "performance_id", updatable = false, nullable = false)
    private UUID performanceId;

    /**
     * Identifier of the post this metric belongs to.
     * <p>Non‑nullable to enforce referential integrity.</p>
     */
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    /**
     * Number of likes received on the post.
     * <p>Non‑nullable; defaults to zero in application logic.</p>
     */
    @Column(name = "likes", nullable = false)
    private int likes;

    /**
     * Number of comments received on the post.
     * <p>Non‑nullable; defaults to zero in application logic.</p>
     */
    @Column(name = "comments", nullable = false)
    private int comments;

    /**
     * Number of shares received on the post.
     * <p>Non‑nullable; defaults to zero in application logic.</p>
     */
    @Column(name = "shares", nullable = false)
    private int shares;

    /**
     * Timestamp when the metrics were collected.
     * <p>Non‑nullable; captures the exact moment of metric aggregation.</p>
     */
    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor required by JPA.
     * <p>Logs entry for persistence framework compliance.</p>
     */
    public PerformanceMetrics() {
        logger.debug("[ENTRY] PerformanceMetrics default constructor invoked for JPA.");
    }

    /**
     * Full‑argument constructor for creating a new metric record.
     *
     * @param postId      The UUID of the associated post.
     * @param likes       Number of likes.
     * @param comments    Number of comments.
     * @param shares      Number of shares.
     * @param collectedAt Timestamp of collection.
     */
    public PerformanceMetrics(UUID postId, int likes, int comments, int shares, LocalDateTime collectedAt) {
        this.postId = postId;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.collectedAt = collectedAt;
        logger.info("[ENTRY] PerformanceMetrics created for postId {} with likes={}, comments={}, shares={}",
                postId, likes, comments, shares);
    }

    // -------------------------------------------------------------------------
    // Property Accessors (Getters / Setters)
    // -------------------------------------------------------------------------

    public UUID getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(UUID performanceId) {
        this.performanceId = performanceId;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    // -------------------------------------------------------------------------
    // Standard Object Overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "performanceId=" + performanceId +
                ", postId=" + postId +
                ", likes=" + likes +
                ", comments=" + comments +
                ", shares=" + shares +
                ", collectedAt=" + collectedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceMetrics that = (PerformanceMetrics) o;
        return performanceId != null && performanceId.equals(that.performanceId);
    }

    @Override
    public int hashCode() {
        return performanceId != null ? performanceId.hashCode() : 0;
    }
}