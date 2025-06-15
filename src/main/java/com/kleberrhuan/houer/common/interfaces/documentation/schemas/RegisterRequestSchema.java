/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "RegisterRequest",
  description = "Estrutura de requisição para registro de usuário",
  implementation = RegisterRequest.class,
  example = "{\n  \"name\": \"João da Silva\",\n  \"email\": \"joao.silva@example.com\",\n  \"password\": \"P@ssw0rd!\"\n}"
)
public interface RegisterRequestSchema {}
