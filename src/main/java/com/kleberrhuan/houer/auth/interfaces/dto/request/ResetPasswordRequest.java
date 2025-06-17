/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.request;

import com.kleberrhuan.houer.common.infra.validation.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
  @NotBlank String token,
  @NotBlank @ValidPassword String newPassword
) {}
