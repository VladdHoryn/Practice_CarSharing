package org.example.application;

import org.example.domain.User;
import org.example.dto.UserResponse; // Перевір, щоб імпорт був правильний
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserApplicationService userApplicationService;

  @Test
  void shouldReturnAllUsers() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.activate(); // <--- ДОДАЙ ОЦЕ (або setIsActive(true), залежить від сеттера)

    when(userRepository.findAll()).thenReturn(List.of(user));

    // Act
    List<UserResponse> result = userApplicationService.getAllUsers();

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void shouldReturnUserById() {
    // Arrange
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.activate(); // <--- І ТУТ ТЕЖ

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    UserResponse result = userApplicationService.getUserById(userId);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
    verify(userRepository, times(1)).findById(userId);
  }
}
