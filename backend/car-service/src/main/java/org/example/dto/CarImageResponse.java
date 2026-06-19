package org.example.dto;

public record CarImageResponse(
        Long id, String fileName, String contentType, Long fileSize, boolean isMain) {}
