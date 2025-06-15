/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "LoginRequest",
  description = "Estrutura de requisição para login de usuário",
  implementation = LoginRequest.class,
  example = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"P@ssw0rd!\",\n  \"rememberMe\": false\n}"
)
public interface LoginRequestSchema {}
