package com.neuramatch.matching.embedding;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private GenerativeModel generativeModel;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private Retry retry;

    @Mock
    private EmbeddingCostTracker costTracker;

    @InjectMocks
    private EmbeddingService embeddingService;

    private List<Float> mockEmbeddingFloats;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "models/embedding-001");
        ReflectionTestUtils.setField(embeddingService, "embeddingDimensions", 768);
        ReflectionTestUtils.setField(embeddingService, "batchSize", 100);

        mockEmbeddingFloats = Arrays.asList(0.1f, 0.2f, 0.3f);
    }

    @Test
    void generateEmbedding_WithValidText_ShouldReturnEmbedding() throws IOException {
        String text = "Software Engineer with 5 years of Java experience";

        com.google.cloud.vertexai.api.Embedding mockEmbedding = com.google.cloud.vertexai.api.Embedding.newBuilder().addAllValues(mockEmbeddingFloats).build();
        Part mockPart = Part.newBuilder().setEmbedding(mockEmbedding).build();
        Content mockContent = Content.newBuilder().addParts(mockPart).build();
        GenerateContentResponse mockResponse = GenerateContentResponse.newBuilder().addCandidates(mockContent).build();

        when(rateLimiter.acquirePermission()).thenReturn(true);
        when(generativeModel.generateContent(any(List.class))).thenReturn(mockResponse);

        List<Double> embedding = embeddingService.generateEmbedding(text);

        assertThat(embedding).isNotEmpty();
        assertThat(embedding).hasSize(mockEmbeddingFloats.size());
        assertThat(embedding.get(0)).isEqualTo(0.1);

        verify(generativeModel).generateContent(any(List.class));
        verify(costTracker).recordApiCall(eq(1), eq("models/embedding-001"), anyLong());
    }

    @Test
    void generateEmbedding_WithEmptyText_ShouldReturnZeroVector() {
        List<Double> embedding = embeddingService.generateEmbedding("");

        assertThat(embedding).isNotEmpty();
        assertThat(embedding).allMatch(v -> v == 0.0);
        verify(generativeModel, never()).generateContent(any(List.class));
    }

    @Test
    void generateEmbedding_WithNullService_ShouldReturnZeroVector() {
        ReflectionTestUtils.setField(embeddingService, "generativeModel", null);

        List<Double> embedding = embeddingService.generateEmbedding("Some text");

        assertThat(embedding).isNotEmpty();
        assertThat(embedding).allMatch(v -> v == 0.0);
    }

    @Test
    void generateEmbeddings_WithMultipleTexts_ShouldReturnMultipleEmbeddings() throws IOException {
        List<String> texts = Arrays.asList("Java Developer", "Python Engineer", "DevOps Specialist");

        com.google.cloud.vertexai.api.Embedding mockEmbedding = com.google.cloud.vertexai.api.Embedding.newBuilder().addAllValues(mockEmbeddingFloats).build();
        Part mockPart = Part.newBuilder().setEmbedding(mockEmbedding).build();
        Content mockContent = Content.newBuilder().addParts(mockPart).build();
        GenerateContentResponse mockResponse = GenerateContentResponse.newBuilder().addCandidates(mockContent).build();

        when(rateLimiter.acquirePermission()).thenReturn(true);
        when(generativeModel.generateContent(any(List.class))).thenReturn(mockResponse);

        List<List<Double>> embeddings = embeddingService.generateEmbeddings(texts);

        assertThat(embeddings).hasSize(3);
        verify(generativeModel).generateContent(any(List.class));
        verify(costTracker).recordApiCall(eq(3), eq("models/embedding-001"), anyLong());
    }

    @Test
    void normalizeEmbedding_ShouldCreateUnitVector() {
        List<Double> embedding = Arrays.asList(3.0, 4.0, 0.0);

        List<Double> normalized = embeddingService.normalizeEmbedding(embedding);

        double magnitude = Math.sqrt(normalized.stream().mapToDouble(v -> v * v).sum());
        assertThat(magnitude).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void cosineSimilarity_WithIdenticalVectors_ShouldReturn1() {
        List<Double> embedding1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> embedding2 = Arrays.asList(1.0, 2.0, 3.0);

        double similarity = embeddingService.cosineSimilarity(embedding1, embedding2);

        assertThat(similarity).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void cosineSimilarity_WithOrthogonalVectors_ShouldReturn0() {
        List<Double> embedding1 = Arrays.asList(1.0, 0.0, 0.0);
        List<Double> embedding2 = Arrays.asList(0.0, 1.0, 0.0);

        double similarity = embeddingService.cosineSimilarity(embedding1, embedding2);

        assertThat(similarity).isCloseTo(0.0, within(0.0001));
    }

    @Test
    void cosineSimilarity_WithOppositVectors_ShouldReturnNegative1() {
        List<Double> embedding1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> embedding2 = Arrays.asList(-1.0, -2.0, -3.0);

        double similarity = embeddingService.cosineSimilarity(embedding1, embedding2);

        assertThat(similarity).isCloseTo(-1.0, within(0.0001));
    }

    @Test
    void cosineSimilarity_WithDifferentDimensions_ShouldThrowException() {
        List<Double> embedding1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> embedding2 = Arrays.asList(1.0, 2.0);

        assertThatThrownBy(() -> embeddingService.cosineSimilarity(embedding1, embedding2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("same dimensions");
    }
}
