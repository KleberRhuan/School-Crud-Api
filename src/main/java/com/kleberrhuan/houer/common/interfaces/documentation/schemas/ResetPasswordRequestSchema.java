/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.auth.interfaces.dto.request.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "ResetPasswordRequest",
  description = "Estrutura de requisição para redefinir senha com token",
  implementation = ResetPasswordRequest.class,
  example = "{\n  \"token\": \"abc123def456...\",\n  \"newPassword\": \"NovaSenha123!\"\n}"
)
public interface ResetPasswordRequestSchema {}
