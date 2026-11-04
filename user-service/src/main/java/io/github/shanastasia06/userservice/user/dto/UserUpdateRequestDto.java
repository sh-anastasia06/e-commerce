package io.github.shanastasia06.userservice.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(max = 50, message = "Firstname must not exceed 50 characters")
        String firstName,
        @Size(max = 50, message = "Lastname must not exceed 50 characters")
        String lastName
) {}
