/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
  @Email @NotNull String email,
  @NotBlank @Size(min = 6) String password,
  boolean rememberMe
) {}
