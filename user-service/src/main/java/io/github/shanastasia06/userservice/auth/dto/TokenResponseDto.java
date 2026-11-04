package io.github.shanastasia06.userservice.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn // sec
) {}
