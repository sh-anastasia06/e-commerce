package io.github.shanastasia06.userservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
}
