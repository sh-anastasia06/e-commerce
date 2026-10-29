package io.github.shanastasia06.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto (
    @NotBlank(message = "Email is required")
    @Email(message = "Wrong email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be from 8 to 72 characters")
    String password,

    // optional
    @Size(max = 50, message = "Firstname must not exceed 50 characters")
    String firstName,
    @Size(max = 50, message = "Lastname must not exceed 50 characters")
    String lastName
){}
