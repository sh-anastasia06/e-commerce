package io.github.shanastasia06.userservice.user.controller;

import io.github.shanastasia06.userservice.user.dto.UserResponseDto;
import io.github.shanastasia06.userservice.user.dto.UserUpdateRequestDto;
import io.github.shanastasia06.userservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.getById(userId);
    }

    @PatchMapping("/me")
    public UserResponseDto updateCurrentUser(@AuthenticationPrincipal Jwt jwt,
                                             @RequestBody @Valid UserUpdateRequestDto dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.updateProfile(userId, dto);
    }
}
