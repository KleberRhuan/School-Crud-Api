/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.user.application.mapper;

import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.common.application.mapper.GenericMapper;
import com.kleberrhuan.houer.user.domain.model.User;
import com.kleberrhuan.houer.user.interfaces.dto.response.UserResponse;
import java.util.Set;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
  componentModel = "spring",
  imports = { Set.class, com.kleberrhuan.houer.auth.domain.model.Role.class }
)
public interface UserMapper extends GenericMapper<User, UserResponse> {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "roles", expression = "java(Set.of(Role.CLIENT))")
  @Mapping(target = "enabled", constant = "false")
  User toEntity(RegisterRequest dto, @Context PasswordEncoder encoder);

  @AfterMapping
  default void hashPassword(
    RegisterRequest dto,
    @MappingTarget User user,
    @Context PasswordEncoder encoder
  ) {
    user.setPasswordHash(encoder.encode(dto.password()));
  }
}
