package com.neuramatch.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Long resumeId;

    private Long recruiterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackAction action;

    @Column(length = 1000)
    private String notes;

    private Double originalScore;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum FeedbackAction {
        VIEWED(0.1),
        SAVED(0.3),
        SHORTLISTED(0.5),
        REJECTED(-0.3),
        INTERVIEW_SCHEDULED(0.7),
        INTERVIEWED(0.8),
        OFFERED(0.9),
        HIRED(1.0),
        DECLINED_OFFER(-0.2);

        private final double weight;

        FeedbackAction(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }
}
