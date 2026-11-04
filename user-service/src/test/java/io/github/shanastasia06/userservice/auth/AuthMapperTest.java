package io.github.shanastasia06.userservice.auth;

import io.github.shanastasia06.userservice.auth.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.auth.mapper.AuthMapper;
import io.github.shanastasia06.userservice.auth.mapper.AuthMapperImpl;
import io.github.shanastasia06.userservice.user.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthMapperTest {
    private final AuthMapper mapper = new AuthMapperImpl();

    @Test
    void toEntity_mapsProfileFieldsAndLeavesServerManagedFieldsEmpty() {
        RegisterRequestDto dto =
                new RegisterRequestDto("anna@example.com", "StrongP@ss123", "Anna", "Smith");

        User user = mapper.toEntity(dto);

        assertThat(user.getEmail()).isEqualTo("anna@example.com");
        assertThat(user.getFirstName()).isEqualTo("Anna");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getId()).isNull();
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getRole()).isNull();
    }
}
