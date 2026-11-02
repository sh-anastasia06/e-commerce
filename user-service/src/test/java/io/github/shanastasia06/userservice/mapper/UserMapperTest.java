package io.github.shanastasia06.userservice.mapper;

import io.github.shanastasia06.userservice.dto.UserUpdateRequestDto;
import io.github.shanastasia06.userservice.model.Role;
import io.github.shanastasia06.userservice.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserMapperTest {
    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    void updateEntity_withNullLastname_keepsExistingLastname() {
        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("anna@example.com")
                .firstName("Anna")
                .lastName("Smith")
                .role(Role.USER)
                .build();

        // Act
        userMapper.updateEntity(new UserUpdateRequestDto("New firstname", null), user);

        // Assert
        assertThat(user.getFirstName()).isEqualTo("New firstname");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getEmail()).isEqualTo("anna@example.com");
    }
}
