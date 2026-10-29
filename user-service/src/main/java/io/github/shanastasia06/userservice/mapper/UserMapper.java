package io.github.shanastasia06.userservice.mapper;

import io.github.shanastasia06.userservice.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.dto.UserResponseDto;
import io.github.shanastasia06.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)   // hashed in the service
    @Mapping(target = "role", ignore = true)           // set by the service
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequestDto dto);

    UserResponseDto toDto(User user);
}
