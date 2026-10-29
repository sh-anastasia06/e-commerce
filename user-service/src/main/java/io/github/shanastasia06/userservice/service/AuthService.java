package io.github.shanastasia06.userservice.service;

import io.github.shanastasia06.userservice.dto.LoginRequestDto;
import io.github.shanastasia06.userservice.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.dto.TokenResponseDto;
import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.exception.EmailAlreadyExistsException;
import io.github.shanastasia06.userservice.exception.InvalidCredentialsException;
import io.github.shanastasia06.userservice.mapper.UserMapper;
import io.github.shanastasia06.userservice.model.User;
import io.github.shanastasia06.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        String normalizedEmail = dto.email().toLowerCase().trim();

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public TokenResponseDto login(LoginRequestDto dto) {
        String email = dto.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return tokenService.generateToken(user);
    }
}
