package com.neuramatch.resume.controller;

import com.neuramatch.resume.dto.ResumeUploadResponse;
import com.neuramatch.resume.entity.Resume;
import com.neuramatch.resume.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeService resumeService;

    @Test
    void testUploadResume_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "Sample resume content".getBytes()
        );

        ResumeUploadResponse response = ResumeUploadResponse.builder()
                .resumeId(1L)
                .message("Resume uploaded successfully")
                .fileName("resume.pdf")
                .fileSize(file.getSize())
                .fileType("PDF")
                .qualityScore(85)
                .status("DRAFT")
                .uploadedAt(LocalDateTime.now())
                .hasErrors(false)
                .build();

        when(resumeService.uploadResume(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumeId").value(1))
                .andExpect(jsonPath("$.message").value("Resume uploaded successfully"))
                .andExpect(jsonPath("$.qualityScore").value(85))
                .andExpect(jsonPath("$.hasErrors").value(false));
    }

    @Test
    void testUploadResume_WithError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.txt",
                "text/plain",
                "".getBytes()
        );

        ResumeUploadResponse response = ResumeUploadResponse.builder()
                .message("Failed to upload resume")
                .hasErrors(true)
                .errorMessage("Cannot upload empty file")
                .build();

        when(resumeService.uploadResume(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hasErrors").value(true))
                .andExpect(jsonPath("$.errorMessage").value("Cannot upload empty file"));
    }

    @Test
    void testGetAllResumes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/resumes")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetActiveResumes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/resumes/active")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchResumes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/resumes/search")
                        .param("query", "john")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteResume() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/resumes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
