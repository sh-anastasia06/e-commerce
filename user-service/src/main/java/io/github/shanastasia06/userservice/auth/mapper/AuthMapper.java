package io.github.shanastasia06.userservice.auth.mapper;

import io.github.shanastasia06.userservice.auth.dto.RegisterRequestDto;
import io.github.shanastasia06.userservice.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequestDto dto);
}
