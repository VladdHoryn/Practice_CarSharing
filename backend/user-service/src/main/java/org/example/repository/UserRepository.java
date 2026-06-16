package org.example.repository;

import java.util.Optional;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByFullName(String fullName);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    void deleteByKeycloakId(String keycloakId);

    /**
     * 1) Загальна кількість користувачів в системі (активних)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Загальна кількість всіх користувачів (включаючи неактивних)
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
    
    /**
     * Кількість користувачів за роллю
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countByRole(@Param("role") UserRole role);
}