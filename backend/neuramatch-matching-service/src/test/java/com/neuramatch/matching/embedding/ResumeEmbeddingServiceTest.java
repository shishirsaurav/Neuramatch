package com.neuramatch.matching.embedding;

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
 * Unit tests for ResumeEmbeddingService
 */
@ExtendWith(MockitoExtension.class)
class ResumeEmbeddingServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private EmbeddingCacheService cacheService;

    @InjectMocks
    private ResumeEmbeddingService resumeEmbeddingService;

    private List<Double> mockEmbedding;
    private ResumeEmbeddingService.ResumeEmbeddingRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        mockRequest = ResumeEmbeddingService.ResumeEmbeddingRequest.builder()
            .resumeId(1L)
            .fullName("John Doe")
            .summary("Experienced software engineer")
            .skills(Arrays.asList(
                ResumeEmbeddingService.SkillDTO.builder()
                    .skillName("Java")
                    .proficiency("Expert")
                    .yearsOfExperience(5)
                    .build(),
                ResumeEmbeddingService.SkillDTO.builder()
                    .skillName("Spring Boot")
                    .proficiency("Advanced")
                    .yearsOfExperience(3)
                    .build()
            ))
            .experiences(Arrays.asList(
                ResumeEmbeddingService.ExperienceDTO.builder()
                    .jobTitle("Senior Software Engineer")
                    .companyName("Tech Corp")
                    .durationInMonths(36)
                    .description("Led backend team")
                    .build()
            ))
            .educations(Arrays.asList(
                ResumeEmbeddingService.EducationDTO.builder()
                    .degree("Bachelor of Science")
                    .fieldOfStudy("Computer Science")
                    .institutionName("State University")
                    .build()
            ))
            .build();

        when(embeddingService.getEmbeddingModel()).thenReturn("text-embedding-3-large");
    }

    @Test
    void generateResumeEmbedding_WhenCacheHit_ShouldReturnCachedEmbedding() {
        // Given
        when(cacheService.getCachedEmbedding(anyString(), anyString()))
            .thenReturn(Optional.of(mockEmbedding));

        // When
        List<Double> result = resumeEmbeddingService.generateResumeEmbedding(mockRequest);

        // Then
        assertThat(result).isEqualTo(mockEmbedding);
        verify(cacheService).getCachedEmbedding(anyString(), eq("text-embedding-3-large"));
        verify(embeddingService, never()).generateEmbedding(anyString());
    }

    @Test
    void generateResumeEmbedding_WhenCacheMiss_ShouldGenerateAndCache() {
        // Given
        when(cacheService.getCachedEmbedding(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(embeddingService.generateEmbedding(anyString()))
            .thenReturn(mockEmbedding);

        // When
        List<Double> result = resumeEmbeddingService.generateResumeEmbedding(mockRequest);

        // Then
        assertThat(result).isEqualTo(mockEmbedding);
        verify(embeddingService).generateEmbedding(anyString());
        verify(cacheService).cacheEmbedding(anyString(), eq("text-embedding-3-large"), eq(mockEmbedding));
    }

    @Test
    void generateResumeEmbedding_ShouldIncludeAllResumeData() {
        // Given
        when(cacheService.getCachedEmbedding(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(embeddingService.generateEmbedding(anyString()))
            .thenReturn(mockEmbedding);

        // When
        resumeEmbeddingService.generateResumeEmbedding(mockRequest);

        // Then
        verify(embeddingService).generateEmbedding(argThat(text ->
            text.contains("John Doe") &&
            text.contains("Java") &&
            text.contains("Expert") &&
            text.contains("5 years") &&
            text.contains("Spring Boot") &&
            text.contains("Senior Software Engineer") &&
            text.contains("Tech Corp") &&
            text.contains("Bachelor of Science") &&
            text.contains("Computer Science")
        ));
    }

    @Test
    void generateResumeEmbeddings_ShouldHandleBatchProcessing() {
        // Given
        List<ResumeEmbeddingService.ResumeEmbeddingRequest> requests = Arrays.asList(
            mockRequest,
            ResumeEmbeddingService.ResumeEmbeddingRequest.builder()
                .resumeId(2L)
                .fullName("Jane Smith")
                .build()
        );

        when(cacheService.getCachedEmbeddings(anyList(), anyString()))
            .thenReturn(Arrays.asList(Optional.empty(), Optional.empty()));
        when(embeddingService.generateEmbeddings(anyList()))
            .thenReturn(Arrays.asList(mockEmbedding, mockEmbedding));

        // When
        List<List<Double>> results = resumeEmbeddingService.generateResumeEmbeddings(requests);

        // Then
        assertThat(results).hasSize(2);
        verify(embeddingService).generateEmbeddings(anyList());
        verify(cacheService).cacheEmbeddings(anyList(), anyString(), anyList());
    }

    @Test
    void generateResumeEmbeddings_WithPartialCacheHits_ShouldOnlyGenerateForMisses() {
        // Given
        List<ResumeEmbeddingService.ResumeEmbeddingRequest> requests = Arrays.asList(
            mockRequest,
            ResumeEmbeddingService.ResumeEmbeddingRequest.builder()
                .resumeId(2L)
                .fullName("Jane Smith")
                .build()
        );

        when(cacheService.getCachedEmbeddings(anyList(), anyString()))
            .thenReturn(Arrays.asList(Optional.of(mockEmbedding), Optional.empty()));
        when(embeddingService.generateEmbeddings(anyList()))
            .thenReturn(Arrays.asList(mockEmbedding));

        // When
        List<List<Double>> results = resumeEmbeddingService.generateResumeEmbeddings(requests);

        // Then
        assertThat(results).hasSize(2);
        verify(embeddingService).generateEmbeddings(argThat(list -> list.size() == 1));
    }

    @Test
    void generateResumeEmbedding_WithMinimalData_ShouldStillGenerateEmbedding() {
        // Given
        ResumeEmbeddingService.ResumeEmbeddingRequest minimalRequest =
            ResumeEmbeddingService.ResumeEmbeddingRequest.builder()
                .resumeId(1L)
                .fullName("John Doe")
                .build();

        when(cacheService.getCachedEmbedding(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(embeddingService.generateEmbedding(anyString()))
            .thenReturn(mockEmbedding);

        // When
        List<Double> result = resumeEmbeddingService.generateResumeEmbedding(minimalRequest);

        // Then
        assertThat(result).isEqualTo(mockEmbedding);
        verify(embeddingService).generateEmbedding(argThat(text -> text.contains("John Doe")));
    }
}
