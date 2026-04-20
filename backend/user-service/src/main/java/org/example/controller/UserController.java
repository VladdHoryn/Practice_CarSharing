package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.application.UserApplicationService;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/v1")
@RequiredArgsConstructor
public class UserController {
  private final UserApplicationService userService;

  // CREATE
  @PostMapping
  public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
    UserResponse response = userService.createUser(request);
    return ResponseEntity.ok(response);
  }

  // READ ALL
  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  // READ BY ID
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  // UPDATE
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
    @PathVariable Long id,
    @RequestBody UserRequest request
  ) {
    return ResponseEntity.ok(userService.updateUser(id, request));
  }

  // DELETE
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
