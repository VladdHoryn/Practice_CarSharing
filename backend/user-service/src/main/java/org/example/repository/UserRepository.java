package org.example.repository;

import java.util.Optional;

import org.example.domain.User;
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

    boolean existsByDriverCode(String driverCode);

    Optional<User> findByDriverCode(String driverCode);

    @Query("SELECT u.id FROM User u WHERE u.email = :email AND u.driverCode = :driverCode")
    Optional<Long> findIdByEmailAndDriverCode(@Param("email") String email, @Param("driverCode") String driverCode);
}
