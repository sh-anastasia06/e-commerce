package io.github.shanastasia06.userservice.mapper;

import io.github.shanastasia06.userservice.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passwordHash", ignore = true) // hashing in service
    User toEntity(RegisterRequestDto dto);

    UserResponseDto toDto(User user);
}
