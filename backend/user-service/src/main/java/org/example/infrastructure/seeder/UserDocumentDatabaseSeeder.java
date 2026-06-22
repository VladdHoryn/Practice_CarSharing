package org.example.infrastructure.seeder;

import java.io.InputStream;

import org.example.application.UserDocumentApplicationService;
import org.example.domain.DocumentType;
import org.example.repository.UserDocumentRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDocumentDatabaseSeeder implements CommandLineRunner {

    private final UserDocumentApplicationService documentService;
    private final UserDocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (documentRepository.count() > 0) {
            return;
        }

        log.info("Starting to seed user documents...");

        for (long userId = 4L; userId <= 7L; userId++) {
            if (userRepository.existsById(userId)) {
                boolean shouldVerify = (userId >= 4L && userId <= 6L);
                seedDocumentsForUser(userId, shouldVerify);
            } else {
                log.warn(
                        "User with id={} not found. Skipping document seeding for this user.",
                        userId);
            }
        }

        log.info("Finished seeding user documents.");
    }

    private void seedDocumentsForUser(Long userId, boolean shouldVerify) {
        seedSingleDocument(
                userId,
                DocumentType.PASSPORT_MAIN,
                "dummy_passport.jpg",
                "image/jpeg",
                shouldVerify);

        seedSingleDocument(
                userId,
                DocumentType.PASSPORT_REGISTRATION,
                "dummy_registration.jpg",
                "image/jpeg",
                shouldVerify);

        seedSingleDocument(
                userId,
                DocumentType.DRIVING_LICENSE,
                "dummy_driver_license.jpg",
                "image/jpeg",
                shouldVerify);
    }

    private void seedSingleDocument(
            Long userId,
            DocumentType type,
            String fileName,
            String contentType,
            boolean shouldVerify) {
        try {
            ClassPathResource resource = new ClassPathResource("user_documents/" + fileName);

            try (InputStream is = resource.getInputStream()) {
                byte[] fileData = is.readAllBytes();

                var savedDocument =
                        documentService.uploadDocument(
                                userId, type, fileData, contentType, fileName);

                log.info("Seeded document {} for user id={}", type.name(), userId);

                if (shouldVerify && savedDocument != null) {
                    documentService.verifyDocument(savedDocument.getId());
                    log.info("Verified document {} for user id={}", type.name(), userId);
                }
            }
        } catch (Exception e) {
            log.error(
                    "Failed to seed document {} for user {}: {}", fileName, userId, e.getMessage());
        }
    }
}
