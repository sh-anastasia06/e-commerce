package io.github.shanastasia06.userservice.service;

import io.github.shanastasia06.userservice.dto.LoginRequestDto;
import io.github.shanastasia06.userservice.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.dto.TokenResponseDto;
import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.exception.EmailAlreadyExistsException;
import io.github.shanastasia06.userservice.exception.InvalidCredentialsException;
import io.github.shanastasia06.userservice.mapper.UserMapper;
import io.github.shanastasia06.userservice.model.Role;
import io.github.shanastasia06.userservice.model.User;
import io.github.shanastasia06.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    // creates tested class entity and injects all mocks
    @InjectMocks
    private AuthService authService;

    @Test
    void register_withNewEmail_savesUserWithNormalizedEmailHashedPasswordAndRoleUser() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto(
                "   Anna@Example.COM ",
                "StrongP@ss123",
                "Anna",
                "Smith"
        );
        UserResponseDto expected = new UserResponseDto(
                UUID.randomUUID(),
                "anna@example.com",
                "Anna",
                "Smith"
        );

        when(userRepository.existsByEmail("anna@example.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(new User());
        when(passwordEncoder.encode("StrongP@ss123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(expected);

        // Act
        UserResponseDto result = authService.register(request);

        // Assert
        assertThat(result).isEqualTo(expected);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("anna@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_withExistingEmail_throwsExceptionAndDoesNotSave() {
        // Arrange
        RegisterRequestDto request = new RegisterRequestDto(
                "anna@example.com",
                "StrongP@ss123",
                null,
                null
        );
        when(userRepository.existsByEmail("anna@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_withCorrectPassword_returnsToken() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(
                "anna@example.com",
                "StrongP@ss123"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("anna@example.com")
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();

        TokenResponseDto expected = new TokenResponseDto(
                "accessToken",
                "Bearer",
                1800
        );

        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongP@ss123", "hashed-password")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn(expected);

        // Act
        TokenResponseDto result = authService.login(request);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(tokenService).generateToken(user);
    }

    @Test
    void login_wrongPassword_throwsException() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(
                "anna@example.com",
                "WrongP@ss123"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("anna@example.com")
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongP@ss123", "hashed-password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void login_unknownEmail_throwsException() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto(
                "unknown@test.com",
                "StrongP@ss123"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("anna@example.com")
                .passwordHash("hashed-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenService, never()).generateToken(any());
    }
}
