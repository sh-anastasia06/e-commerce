package io.github.shanastasia06.userservice.user.service;

import io.github.shanastasia06.userservice.user.dto.UserResponseDto;
import io.github.shanastasia06.userservice.user.dto.UserUpdateRequestDto;
import io.github.shanastasia06.userservice.user.exception.UserNotFoundException;
import io.github.shanastasia06.userservice.user.mapper.UserMapper;
import io.github.shanastasia06.userservice.user.model.Role;
import io.github.shanastasia06.userservice.user.model.User;
import io.github.shanastasia06.userservice.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_returnsDto() {
        // Arrange
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .email("anna@example.com")
                .firstName("Anna")
                .lastName("Smith")
                .build();

        UserResponseDto expected = new UserResponseDto(id, "anna@example.com", "Anna", "Smith");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expected);

        // Act
        UserResponseDto result = userService.getById(id);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getById_withUnknownId_throwsException() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(userMapper);
    }

    @Test
    void updateProfile_existingUser_updatesEntityAndReturnsDto() {
        UUID id = UUID.randomUUID();
        UserUpdateRequestDto dto =  new UserUpdateRequestDto("New firstname", null);

        User user = User.builder()
                .id(id)
                .email("anna@example.com")
                .firstName("Anna")
                .lastName("Smith")
                .role(Role.USER)
                .build();

        UserResponseDto expected = new UserResponseDto(id, "anna@example.com", "New firstname", "Smith");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expected);

        // Act
        UserResponseDto result = userService.updateProfile(id, dto);

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(userMapper).updateEntity(dto, user);
    }

    @Test
    void updateProfile_withUnknownId_throwsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        UserUpdateRequestDto dto =  new UserUpdateRequestDto("New firstname", null);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(id, dto))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(userMapper);
    }
}
