package org.example.application;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.domain.DocumentType;
import org.example.domain.User;
import org.example.domain.UserDocument;
import org.example.repository.UserDocumentRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDocumentApplicationService {

    private final UserDocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserDocument uploadDocument(
            Long userId,
            DocumentType documentType,
            byte[] fileData,
            String contentType,
            String originalFileName) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User not found with id: " + userId));

        UserDocument document =
                documentRepository
                        .findByUserIdAndDocumentType(userId, documentType)
                        .orElseGet(
                                () -> {
                                    UserDocument doc = new UserDocument();
                                    doc.setDocumentType(documentType);
                                    user.addDocument(doc);
                                    return doc;
                                });

        document.setFileData(fileData);
        document.setContentType(contentType);
        document.setOriginalFileName(originalFileName);
        document.setIsVerified(false);

        return documentRepository.save(document);
    }

    @Transactional
    public void verifyDocument(Long documentId) {
        UserDocument document =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Document not found with id: " + documentId));

        document.verify();
        documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public boolean areAllDocumentsVerifiedAndPresent(Long userId) {
        List<UserDocument> userDocuments = documentRepository.findByUserId(userId);

        Set<DocumentType> verifiedTypes =
                userDocuments.stream()
                        .filter(UserDocument::getIsVerified)
                        .map(UserDocument::getDocumentType)
                        .collect(Collectors.toSet());

        List<DocumentType> requiredTypes = Arrays.asList(DocumentType.values());

        return verifiedTypes.containsAll(requiredTypes);
    }

    @Transactional(readOnly = true)
    public List<UserDocument> getUserDocuments(Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserDocument getDocumentById(Long documentId) {
        return documentRepository
                .findById(documentId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Document not found with id: " + documentId));
    }

    public List<UserDocument> getAllSystemUnverifiedDocuments() {
        return documentRepository.findByIsVerifiedFalse();
    }
}
