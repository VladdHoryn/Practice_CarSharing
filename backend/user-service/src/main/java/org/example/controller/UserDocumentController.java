package org.example.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.example.application.UserDocumentApplicationService;
import org.example.domain.DocumentType;
import org.example.domain.UserDocument;
import org.example.dto.UserDocumentResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/document/v1")
@RequiredArgsConstructor
public class UserDocumentController {

  private final UserDocumentApplicationService documentService;

  /**
   * Завантаження нового документа. Використовує form-data.
   */
  // Якщо ти ідентифікуєш користувача через Keycloak ID, тут можна змінити Long userId на String keycloakId
  // і шукати внутрішній ID всередині сервісу (як у твоєму UserController).
  @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('USER')") // Налаштуй під свої політики
  public ResponseEntity<UserDocumentResponse> uploadDocument(
    @PathVariable Long userId,
    @RequestParam("documentType") DocumentType documentType,
    @RequestParam("file") MultipartFile file) {

    try {
      UserDocument savedDoc = documentService.uploadDocument(
        userId,
        documentType,
        file.getBytes(),
        file.getContentType(),
        file.getOriginalFilename()
      );
      return ResponseEntity.ok(mapToResponse(savedDoc));
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file bytes", e);
    }
  }

  /**
   * Отримання списку завантажених документів (лише метадані, без самих файлів).
   */
  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('USER')")
  public ResponseEntity<List<UserDocumentResponse>> getUserDocumentsInfo(@PathVariable Long userId) {
    List<UserDocumentResponse> responses = documentService.getUserDocuments(userId)
      .stream()
      .map(this::mapToResponse)
      .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  /**
   * Завантаження самого файлу (Download).
   * Повертає байти файлу з відповідними заголовками для відображення в браузері/завантаження.
   */
  @GetMapping("/{documentId}/download")
  @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('USER')")
  public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
    UserDocument document = documentService.getDocumentById(documentId);

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFileName() + "\"")
      .contentType(MediaType.parseMediaType(document.getContentType()))
      .body(document.getFileData());
  }

  /**
   * Перевірка: чи всі необхідні документи присутні та верифіковані.
   */
  @GetMapping("/user/{userId}/status")
  @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('USER')")
  public ResponseEntity<Boolean> checkAllDocumentsVerified(@PathVariable Long userId) {
    boolean status = documentService.areAllDocumentsVerifiedAndPresent(userId);
    return ResponseEntity.ok(status);
  }

  /**
   * Адміністративна дія: верифікація конкретного документа.
   */
  @PatchMapping("/{documentId}/verify")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ResponseEntity<Void> verifyDocument(@PathVariable Long documentId) {
    documentService.verifyDocument(documentId);
    return ResponseEntity.noContent().build();
  }

  // Допоміжний метод-мапер (у реальному проєкті це часто роблять через MapStruct)
  private UserDocumentResponse mapToResponse(UserDocument document) {
    return UserDocumentResponse.builder()
      .id(document.getId())
      .documentType(document.getDocumentType())
      .originalFileName(document.getOriginalFileName())
      .contentType(document.getContentType())
      .isVerified(document.getIsVerified())
      .uploadedAt(document.getUploadedAt())
      .build();
  }
}
