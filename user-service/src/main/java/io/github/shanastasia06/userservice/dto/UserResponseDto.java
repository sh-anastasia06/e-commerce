package io.github.shanastasia06.userservice.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String firstName,
        String lastName
) {}
