package com.neuramatch.resume.service;

import com.neuramatch.resume.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeParsingService {

    private final FileStorageService fileStorageService;

    public Map<String, Object> parseResume(String filename) {
        Path filePath = fileStorageService.getFilePath(filename);
        Map<String, Object> parsedData = new HashMap<>();

        try (InputStream stream = new FileInputStream(filePath.toFile())) {
            // Initialize Tika components
            BodyContentHandler handler = new BodyContentHandler(-1); // No limit
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            AutoDetectParser parser = new AutoDetectParser();

            // Parse the file
            parser.parse(stream, handler, metadata, context);

            // Extract text content
            String content = handler.toString();
            parsedData.put("content", content);
            parsedData.put("contentLength", content.length());

            // Extract metadata
            parsedData.put("contentType", metadata.get("Content-Type"));
            parsedData.put("author", metadata.get("Author"));
            parsedData.put("creationDate", metadata.get("Creation-Date"));
            parsedData.put("lastModified", metadata.get("Last-Modified"));
            parsedData.put("pageCount", metadata.get("Page-Count"));

            log.info("Successfully parsed resume: {}", filename);
            return parsedData;

        } catch (IOException | SAXException | TikaException e) {
            log.error("Error parsing resume: {}", filename, e);
            throw new FileStorageException("Failed to parse resume file", e);
        }
    }

    public String extractTextContent(String filename) {
        Map<String, Object> parsedData = parseResume(filename);
        return (String) parsedData.get("content");
    }

    public Map<String, String> extractMetadata(String filename) {
        Map<String, Object> parsedData = parseResume(filename);
        Map<String, String> metadata = new HashMap<>();

        metadata.put("contentType", (String) parsedData.get("contentType"));
        metadata.put("author", (String) parsedData.get("author"));
        metadata.put("creationDate", (String) parsedData.get("creationDate"));
        metadata.put("lastModified", (String) parsedData.get("lastModified"));
        metadata.put("pageCount", (String) parsedData.get("pageCount"));

        return metadata;
    }

    /**
     * Extract structured information from resume text
     * This is a basic implementation - will be enhanced with NLP in later sprints
     */
    public Map<String, Object> extractStructuredInfo(String content) {
        Map<String, Object> structuredInfo = new HashMap<>();

        // Basic extraction - will be replaced with NLP in Sprint 4
        structuredInfo.put("hasEmail", content.matches(".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*"));
        structuredInfo.put("hasPhone", content.matches(".*\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}.*"));
        structuredInfo.put("hasLinkedIn", content.toLowerCase().contains("linkedin"));
        structuredInfo.put("hasGitHub", content.toLowerCase().contains("github"));
        structuredInfo.put("wordCount", content.split("\\s+").length);

        return structuredInfo;
    }
}
