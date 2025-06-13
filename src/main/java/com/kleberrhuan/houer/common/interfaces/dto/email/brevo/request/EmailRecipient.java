/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRecipient(
  @Nullable String name,
  @Email @NotBlank String email
) {}
