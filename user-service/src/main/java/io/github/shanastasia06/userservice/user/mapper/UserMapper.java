package io.github.shanastasia06.userservice.user.mapper;

import io.github.shanastasia06.userservice.user.dto.UserResponseDto;
import io.github.shanastasia06.userservice.user.dto.UserUpdateRequestDto;
import io.github.shanastasia06.userservice.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);

    // ignore unchanged fields (don't change them to null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(UserUpdateRequestDto dto, @MappingTarget User user);
}
