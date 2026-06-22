package org.example.dto;

public record OwnerMainImageResponse(
  Long carId,
  Long imageId,
  String fileName,
  String contentType,
  long fileSize,
  boolean isMain
) {}
