package com.neuramatch.resume.service;

import com.neuramatch.resume.exception.FileStorageException;
import com.neuramatch.resume.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public FileStorageService(@Value("${file.upload-dir:uploads/resumes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Copy file to storage location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", uniqueFilename);
            return uniqueFilename;

        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + originalFilename, e);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filename);
        } catch (IOException e) {
            log.error("Could not delete file: {}", filename, e);
        }
    }

    public Path getFilePath(String filename) {
        return this.fileStorageLocation.resolve(filename).normalize();
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum limit of 5MB");
        }

        // Check file type
        String contentType = file.getContentType();
        if (!isValidContentType(contentType)) {
            throw new InvalidFileException("Invalid file type. Only PDF, DOC, DOCX, and TXT files are allowed");
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new InvalidFileException("Invalid filename");
        }
    }

    private boolean isValidContentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("text/plain")
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return switch (extension) {
            case ".pdf" -> "PDF";
            case ".doc" -> "DOC";
            case ".docx" -> "DOCX";
            case ".txt" -> "TXT";
            default -> "UNKNOWN";
        };
    }
}
