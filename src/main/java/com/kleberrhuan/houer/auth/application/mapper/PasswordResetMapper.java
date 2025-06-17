/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.mapper;

import com.kleberrhuan.houer.auth.domain.model.PasswordReset;
import com.kleberrhuan.houer.user.domain.model.User;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PasswordResetMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "tokenHash", source = "tokenHash")
  @Mapping(target = "expiresAt", source = "expiresAt")
  @Mapping(target = "usedAt", ignore = true)
  PasswordReset toEntity(User user, String tokenHash, Instant expiresAt);
}
