package org.example.application;

import lombok.RequiredArgsConstructor;
import org.example.domain.User;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  private String encodeString(String line){
    return passwordEncoder.encode(line);
  }

  // CREATE
  public UserResponse createUser(UserRequest request) {
    User user = new User();
    user.setName(request.getName());
    user.setPassword(encodeString(request.getPassword()));
    user.setRole(request.getRole());

    User savedUser = userRepository.save(user);
    return mapToResponse(savedUser);
  }

  // READ ALL
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll()
      .stream()
      .map(this::mapToResponse)
      .collect(Collectors.toList());
  }

  // READ BY ID
  public UserResponse getUserById(Long id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found"));

    return mapToResponse(user);
  }

  // UPDATE
  public UserResponse updateUser(Long id, UserRequest request) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new RuntimeException("User not found"));

    user.setName(request.getName());
    user.setPassword(encodeString(request.getPassword()));
    user.setRole(request.getRole());

    return mapToResponse(userRepository.save(user));
  }

  // DELETE
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  // MAPPER
  private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
      .id(user.getId())
      .name(user.getName())
      .role(user.getRole())
      .build();
  }
}
