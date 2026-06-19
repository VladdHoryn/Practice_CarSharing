package org.example.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.example.domain.User;
import org.example.dto.UserResponse;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserApplicationService userApplicationService;

    @Test
    void shouldReturnAllUsers() {
        User user = new User();
        user.setId(1L);
        user.activate();

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userApplicationService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnUserById() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.activate();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse result = userApplicationService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }
}
