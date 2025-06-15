/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.request;

import com.kleberrhuan.houer.common.infra.validation.annotation.ValidPassword;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.RegisterRequestSchema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
  @NotBlank String name,
  @Email String email,
  @ValidPassword String password
)
  implements RegisterRequestSchema {}
