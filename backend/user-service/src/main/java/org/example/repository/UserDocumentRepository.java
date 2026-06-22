package org.example.repository;

import java.util.List;
import java.util.Optional;

import org.example.domain.DocumentType;
import org.example.domain.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {

    List<UserDocument> findByUserId(Long userId);

    Optional<UserDocument> findByUserIdAndDocumentType(Long userId, DocumentType documentType);
}
