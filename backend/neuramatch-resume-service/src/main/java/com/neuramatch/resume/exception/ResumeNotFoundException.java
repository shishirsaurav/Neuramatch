package com.neuramatch.resume.exception;

public class ResumeNotFoundException extends RuntimeException {

    public ResumeNotFoundException(String message) {
        super(message);
    }

    public ResumeNotFoundException(Long id) {
        super("Resume not found with id: " + id);
    }
}
