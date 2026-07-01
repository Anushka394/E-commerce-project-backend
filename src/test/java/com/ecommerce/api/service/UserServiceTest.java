package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.RegisterRequest;
import com.ecommerce.api.dto.response.UserResponse;
import com.ecommerce.api.exception.ConflictException;
import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setUsername("john");
        validRequest.setEmail("john@example.com");
        validRequest.setPassword("secret123");
        validRequest.setAddress("123 Main St");
    }

    @Test
    void register_savesUserAndReturnsResponse() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("john");
        saved.setEmail("john@example.com");
        saved.setPassword("hashed");
        saved.setRole(User.Role.ROLE_USER);
        saved.setAddress("123 Main St");

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(validRequest);

        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsConflict_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("john");
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("john@example.com");
    }

    @Test
    void getUserByUsername_returnsUser_whenFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setRole(User.Role.ROLE_USER);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUsername("john");

        assertThat(response.getUsername()).isEqualTo("john");
    }
}
