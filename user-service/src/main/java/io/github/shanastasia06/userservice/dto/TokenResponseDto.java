package io.github.shanastasia06.userservice.dto;

public record TokenResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn // sec
) {}
