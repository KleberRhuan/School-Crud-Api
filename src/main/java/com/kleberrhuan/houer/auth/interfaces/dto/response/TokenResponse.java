/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.response;

public record TokenResponse(String tokenType, String accessToken) {
  public static TokenResponse withDefaults(String accessToken) {
    return new TokenResponse("Bearer", accessToken);
  }
}
