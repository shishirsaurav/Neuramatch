package com.neuramatch.matching.repository;

import com.neuramatch.matching.entity.MatchFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchFeedbackRepository extends JpaRepository<MatchFeedback, Long> {

    List<MatchFeedback> findByJobId(Long jobId);

    List<MatchFeedback> findByResumeId(Long resumeId);

    Optional<MatchFeedback> findByJobIdAndResumeId(Long jobId, Long resumeId);

    List<MatchFeedback> findByRecruiterId(Long recruiterId);

    List<MatchFeedback> findByAction(MatchFeedback.FeedbackAction action);

    @Query("SELECT f FROM MatchFeedback f WHERE f.createdAt >= :since")
    List<MatchFeedback> findRecentFeedback(@Param("since") LocalDateTime since);

    @Query("SELECT f FROM MatchFeedback f WHERE f.jobId = :jobId AND f.action IN :actions")
    List<MatchFeedback> findByJobIdAndActions(
            @Param("jobId") Long jobId,
            @Param("actions") List<MatchFeedback.FeedbackAction> actions);

    @Query("SELECT COUNT(f) FROM MatchFeedback f WHERE f.action = :action AND f.createdAt >= :since")
    long countByActionSince(
            @Param("action") MatchFeedback.FeedbackAction action,
            @Param("since") LocalDateTime since);
}
