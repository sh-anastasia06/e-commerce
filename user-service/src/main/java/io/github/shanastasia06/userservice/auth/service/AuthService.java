package io.github.shanastasia06.userservice.auth.service;

import io.github.shanastasia06.userservice.auth.dto.LoginRequestDto;
import io.github.shanastasia06.userservice.auth.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.auth.dto.TokenResponseDto;
import io.github.shanastasia06.userservice.auth.mapper.AuthMapper;
import io.github.shanastasia06.userservice.user.dto.UserResponseDto;
import io.github.shanastasia06.userservice.auth.exception.EmailAlreadyExistsException;
import io.github.shanastasia06.userservice.auth.exception.InvalidCredentialsException;
import io.github.shanastasia06.userservice.user.mapper.UserMapper;
import io.github.shanastasia06.userservice.user.model.Role;
import io.github.shanastasia06.userservice.user.model.User;
import io.github.shanastasia06.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        String normalizedEmail = dto.email().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = authMapper.toEntity(dto);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(Role.USER);

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
