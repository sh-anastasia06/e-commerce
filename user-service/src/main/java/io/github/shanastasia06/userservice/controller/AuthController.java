package io.github.shanastasia06.userservice.controller;

import io.github.shanastasia06.userservice.dto.LoginRequestDto;
import io.github.shanastasia06.userservice.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.dto.TokenResponseDto;
import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid RegisterRequestDto request) {
        UserResponseDto userResponseDto = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody @Valid LoginRequestDto dto) {
        return authService.login(dto);
    }
}
