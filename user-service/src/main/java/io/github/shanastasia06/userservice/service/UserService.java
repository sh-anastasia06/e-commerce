package io.github.shanastasia06.userservice.service;

import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.dto.UserUpdateRequestDto;
import io.github.shanastasia06.userservice.exception.UserNotFoundException;
import io.github.shanastasia06.userservice.mapper.UserMapper;
import io.github.shanastasia06.userservice.model.User;
import io.github.shanastasia06.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        return userMapper.toDto(user);
    }

    // hibernate dirty checking works, no need to add save()
    @Transactional
    public UserResponseDto updateProfile(UUID id, UserUpdateRequestDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
        userMapper.updateEntity(dto, user);
        return userMapper.toDto(user);
    }
}
