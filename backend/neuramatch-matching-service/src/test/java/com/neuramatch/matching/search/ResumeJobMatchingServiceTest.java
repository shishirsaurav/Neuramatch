package com.neuramatch.matching.search;

import com.neuramatch.matching.embedding.EmbeddingService;
import com.neuramatch.matching.service.SkillEnrichmentService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.JobVectorRepository;
import com.neuramatch.matching.vector.ResumeVector;
import com.neuramatch.matching.vector.ResumeVectorRepository;
import com.pgvector.PGvector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResumeJobMatchingService
 */
@ExtendWith(MockitoExtension.class)
class ResumeJobMatchingServiceTest {

    @Mock
    private ResumeVectorRepository resumeVectorRepository;

    @Mock
    private JobVectorRepository jobVectorRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private SkillEnrichmentService skillEnrichmentService;

    @InjectMocks
    private ResumeJobMatchingService matchingService;

    private ResumeVector mockResume;
    private JobVector mockJob;
    private List<Double> mockEmbedding;

    @BeforeEach
    void setUp() {
        mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        mockResume = ResumeVector.builder()
            .id(1L)
            .resumeId(100L)
            .fullName("John Doe")
            .yearsOfExperience(5)
            .location("San Francisco")
            .remotePreference("REMOTE")
            .topSkills(new String[]{"Java", "Spring Boot", "PostgreSQL"})
            .qualityScore(85)
            .isActive(true)
            .build();
        mockResume.setEmbeddingFromList(mockEmbedding);

        mockJob = JobVector.builder()
            .id(1L)
            .jobId(200L)
            .title("Senior Java Developer")
            .companyName("Tech Corp")
            .location("San Francisco")
            .remoteType("REMOTE")
            .minYearsExperience(3)
            .maxYearsExperience(7)
            .minSalary(120000)
            .maxSalary(160000)
            .requiredSkills(new String[]{"Java", "Spring Boot"})
            .isActive(true)
            .build();
        mockJob.setEmbeddingFromList(mockEmbedding);
    }

    @Test
    void findMatchingJobsForResume_ShouldReturnMatchedJobs() {
        // Given
        when(resumeVectorRepository.findByResumeId(100L)).thenReturn(Optional.of(mockResume));
        when(jobVectorRepository.findMatchingJobsByExperience(anyString(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(mockJob));
        when(embeddingService.cosineSimilarity(anyList(), anyList())).thenReturn(0.85);
        when(skillEnrichmentService.calculateSkillCoverage(anyList(), anyList(), anyBoolean()))
            .thenReturn(0.90);

        // When
        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(100L, null);

        // Then
        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).getJobId()).isEqualTo(200L);
        assertThat(matches.get(0).getJobTitle()).isEqualTo("Senior Java Developer");
        assertThat(matches.get(0).getOverallScore()).isGreaterThan(0.0);
        assertThat(matches.get(0).getSemanticSimilarity()).isEqualTo(0.85);

        verify(resumeVectorRepository).findByResumeId(100L);
        verify(jobVectorRepository).findMatchingJobsByExperience(anyString(), eq(5), anyInt());
    }

    @Test
    void findMatchingCandidatesForJob_ShouldReturnMatchedCandidates() {
        // Given
        when(jobVectorRepository.findByJobId(200L)).thenReturn(Optional.of(mockJob));
        when(resumeVectorRepository.findSimilarResumesWithExperience(anyString(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(mockResume));
        when(embeddingService.cosineSimilarity(anyList(), anyList())).thenReturn(0.82);
        when(skillEnrichmentService.calculateSkillCoverage(anyList(), anyList(), anyBoolean()))
            .thenReturn(0.85);

        // When
        List<ResumeJobMatchingService.CandidateMatch> matches =
            matchingService.findMatchingCandidatesForJob(200L, null);

        // Then
        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).getResumeId()).isEqualTo(100L);
        assertThat(matches.get(0).getFullName()).isEqualTo("John Doe");
        assertThat(matches.get(0).getOverallScore()).isGreaterThan(0.0);
        assertThat(matches.get(0).getQualityScore()).isEqualTo(85);

        verify(jobVectorRepository).findByJobId(200L);
        verify(resumeVectorRepository).findSimilarResumesWithExperience(anyString(), eq(3), anyInt());
    }

    @Test
    void findMatchingJobsForResume_WithFilters_ShouldApplyFilters() {
        // Given
        ResumeJobMatchingService.MatchingCriteria criteria =
            ResumeJobMatchingService.MatchingCriteria.builder()
                .location("San Francisco")
                .remoteType("REMOTE")
                .limit(20)
                .build();

        when(resumeVectorRepository.findByResumeId(100L)).thenReturn(Optional.of(mockResume));
        when(jobVectorRepository.findMatchingJobsWithFilters(
            anyString(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(Arrays.asList(mockJob));
        when(embeddingService.cosineSimilarity(anyList(), anyList())).thenReturn(0.90);
        when(skillEnrichmentService.calculateSkillCoverage(anyList(), anyList(), anyBoolean()))
            .thenReturn(0.95);

        // When
        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(100L, criteria);

        // Then
        assertThat(matches).isNotEmpty();

        verify(jobVectorRepository).findMatchingJobsWithFilters(
            anyString(),
            eq(5), // yearsOfExperience
            eq(5),
            eq("San Francisco"),
            eq("REMOTE"),
            isNull(),
            eq(20)
        );
    }

    @Test
    void calculateJobMatch_ShouldCalculateWeightedScore() {
        // Given
        when(embeddingService.cosineSimilarity(anyList(), anyList())).thenReturn(0.85);
        when(skillEnrichmentService.calculateSkillCoverage(anyList(), anyList(), anyBoolean()))
            .thenReturn(0.90);

        // When - Use reflection or make the method public for testing
        // For now, we'll test through the main method
        when(resumeVectorRepository.findByResumeId(100L)).thenReturn(Optional.of(mockResume));
        when(jobVectorRepository.findMatchingJobsByExperience(anyString(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(mockJob));

        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(100L, null);

        // Then
        ResumeJobMatchingService.JobMatch match = matches.get(0);

        // Verify weighted scoring: 40% semantic + 30% skill + 20% experience + 10% location
        assertThat(match.getOverallScore()).isGreaterThan(0.0);
        assertThat(match.getOverallScore()).isLessThanOrEqualTo(100.0);

        // Perfect experience match (5 years within 3-7 range)
        assertThat(match.getExperienceMatchScore()).isEqualTo(1.0);

        // Perfect location match (both REMOTE)
        assertThat(match.getLocationMatchScore()).isEqualTo(1.0);
    }

    @Test
    void findMatchingJobsForResume_WhenResumeNotFound_ShouldThrowException() {
        // Given
        when(resumeVectorRepository.findByResumeId(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
            matchingService.findMatchingJobsForResume(999L, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Resume not found");
    }

    @Test
    void findMatchingCandidatesForJob_WhenJobNotFound_ShouldThrowException() {
        // Given
        when(jobVectorRepository.findByJobId(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
            matchingService.findMatchingCandidatesForJob(999L, null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Job not found");
    }

    @Test
    void findMatchingJobsForResume_ShouldSortByScore() {
        // Given
        JobVector lowScoreJob = JobVector.builder()
            .jobId(201L)
            .title("Junior Developer")
            .isActive(true)
            .build();
        lowScoreJob.setEmbeddingFromList(Arrays.asList(0.9, 0.8, 0.7)); // Different embedding

        when(resumeVectorRepository.findByResumeId(100L)).thenReturn(Optional.of(mockResume));
        when(jobVectorRepository.findMatchingJobsByExperience(anyString(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList(mockJob, lowScoreJob));

        when(embeddingService.cosineSimilarity(eq(mockEmbedding), eq(mockEmbedding)))
            .thenReturn(0.95); // High similarity
        when(embeddingService.cosineSimilarity(eq(mockEmbedding), anyList()))
            .thenReturn(0.50); // Low similarity

        when(skillEnrichmentService.calculateSkillCoverage(anyList(), anyList(), anyBoolean()))
            .thenReturn(0.80);

        // When
        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(100L, null);

        // Then
        assertThat(matches).hasSize(2);
        // First result should have higher score
        assertThat(matches.get(0).getOverallScore())
            .isGreaterThan(matches.get(1).getOverallScore());
    }
}
