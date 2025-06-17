/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.auth.interfaces.dto.request.ForgotPasswordRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "ForgotPasswordRequest",
  description = "Estrutura de requisição para solicitar redefinição de senha",
  implementation = ForgotPasswordRequest.class,
  example = "{\n  \"email\": \"usuario@example.com\"\n}"
)
public interface ForgotPasswordRequestSchema {}
