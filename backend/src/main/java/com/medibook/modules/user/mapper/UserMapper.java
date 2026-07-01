package com.medibook.modules.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.medibook.modules.user.entity.User;
import com.medibook.modules.user.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.name", target = "roleName")
    UserResponse toResponse(User user);
}