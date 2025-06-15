/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.auth.interfaces.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "TokenResponse",
  description = "Estrutura de resposta contendo tokens de autenticação",
  implementation = TokenResponse.class,
  example = "{\n  \"tokenType\": \"Bearer\",\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\"\n}"
)
public interface TokenResponseSchema {}
