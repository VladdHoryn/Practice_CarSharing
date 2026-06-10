package org.example.repository;

import java.util.Optional;

import org.example.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFullName(String fullName);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    void deleteByKeycloakId(String keycloakId);

    boolean existsByDriverCode(String driverCode);

    Optional<User> findByDriverCode(String driverCode);
}
