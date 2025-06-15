/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.response;

import com.kleberrhuan.houer.common.interfaces.documentation.schemas.TokenResponseSchema;

public record TokenResponse(String tokenType, String accessToken)
  implements TokenResponseSchema {
  public static TokenResponse withDefaults(String accessToken) {
    return new TokenResponse("Bearer", accessToken);
  }
}
