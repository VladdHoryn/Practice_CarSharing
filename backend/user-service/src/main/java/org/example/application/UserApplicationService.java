package org.example.application;

import java.util.List;
import java.util.stream.Collectors;

import org.example.domain.User;
import org.example.domain.UserRole;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.exception.InvalidCredentialsException;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public AuthResponse register(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(encodePassword(request.getPassword()));

        user.setRole(request.getRole() != null ? request.getRole() : UserRole.RENTER);

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .user(mapToResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(
                                () -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        boolean passwordMatches =
                passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return AuthResponse.builder().message("Login successful").user(mapToResponse(user)).build();
    }

    // CREATE
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(encodePassword(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.RENTER);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    // READ ALL
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // READ BY ID
    public UserResponse getUserById(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    // UPDATE
    public UserResponse updateUser(Long id, UserRequest request) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // оновлюємо пароль тільки якщо переданий
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(encodePassword(request.getPassword()));
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return mapToResponse(userRepository.save(user));
    }

    // DELETE (soft delete через deactivate)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ACTIVATE USER
    public UserResponse activateUser(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        user.activate();
        return mapToResponse(userRepository.save(user));
    }

    // DEACTIVATE USER
    public UserResponse deactivateUser(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new RuntimeException("User not found"));

        user.deactivate();

        return mapToResponse(userRepository.save(user));
    }

    // MAPPER
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
