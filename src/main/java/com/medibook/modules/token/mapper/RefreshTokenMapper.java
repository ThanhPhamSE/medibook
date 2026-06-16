package com.medibook.modules.token.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.medibook.modules.token.entity.RefreshToken;
import com.medibook.modules.user.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RefreshTokenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenHash", source = "tokenHash")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "revoked", constant = "false")
    @Mapping(target = "deviceInfo", source = "deviceInfo")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RefreshToken toEntity(User user, String tokenHash, LocalDateTime expiresAt, String deviceInfo, String ipAddress);
}