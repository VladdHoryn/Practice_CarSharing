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

    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('RENTER')")
    public ResponseEntity<UserDocumentResponse> uploadDocument(
            @PathVariable Long userId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {

        try {
            UserDocument savedDoc =
                    documentService.uploadDocument(
                            userId,
                            documentType,
                            file.getBytes(),
                            file.getContentType(),
                            file.getOriginalFilename());
            return ResponseEntity.ok(mapToResponse(savedDoc));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file bytes", e);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('RENTER')")
    public ResponseEntity<List<UserDocumentResponse>> getUserDocumentsInfo(
            @PathVariable Long userId) {
        List<UserDocumentResponse> responses =
                documentService.getUserDocuments(userId).stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/unverified")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserDocumentResponse>> getAllSystemUnverifiedDocuments() {

      List<UserDocumentResponse> responses =
        documentService.getAllSystemUnverifiedDocuments().stream()
          .map(this::mapToResponse)
          .collect(Collectors.toList());

      return ResponseEntity.ok(responses);
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('RENTER')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        UserDocument document = documentService.getDocumentById(documentId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .body(document.getFileData());
    }

    @GetMapping("/user/{userId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('RENTER')")
    public ResponseEntity<Boolean> checkAllDocumentsVerified(@PathVariable Long userId) {
        boolean status = documentService.areAllDocumentsVerifiedAndPresent(userId);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/{documentId}/verify")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> verifyDocument(@PathVariable Long documentId) {
        documentService.verifyDocument(documentId);
        return ResponseEntity.noContent().build();
    }

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
