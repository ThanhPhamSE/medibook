package com.medibook.modules.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.medibook.modules.auth.dto.request.RegisterRequest;
import com.medibook.modules.auth.dto.response.LoginResponse;
import com.medibook.modules.auth.dto.response.RegisterResponse;
import com.medibook.modules.user.dto.response.UserResponse;
import com.medibook.modules.user.entity.Role;
import com.medibook.modules.user.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toUser(RegisterRequest request, String encodedPassword, Role role);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role", source = "role.name")
    RegisterResponse toRegisterResponse(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "role", source = "user.role.name")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessTokenExpiresAt", source = "accessTokenExpiresAt")
    @Mapping(target = "refreshTokenExpiresAt", source = "refreshTokenExpiresAt")
    @Mapping(target = "issuedAt", source = "issuedAt")
    LoginResponse toLoginResponse(User user, String accessToken, String refreshToken, long accessTokenExpiresAt,
            long refreshTokenExpiresAt, long issuedAt);

    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "profileImage", source = "profileImage")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "doctorId", ignore = true)
    UserResponse toUserResponse(User user);
}