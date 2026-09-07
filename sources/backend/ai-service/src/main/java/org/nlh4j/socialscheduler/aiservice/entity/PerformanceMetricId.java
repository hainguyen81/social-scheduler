package org.nlh4j.socialscheduler.aiservice.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite primary key class for {@link PerformanceMetricEntity}.
 * Maps to {@code (performance_id, post_id, collected_at)}.
 *
 * @traceability [DAT-002]
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerformanceMetricId implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;

	@Column(name = "performance_id")
    private UUID performanceId;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "collected_at")
    private OffsetDateTime collectedAt;
}