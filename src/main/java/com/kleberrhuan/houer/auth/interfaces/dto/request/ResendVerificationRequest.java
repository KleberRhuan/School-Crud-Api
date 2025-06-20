/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResendVerificationRequest(@Email @NotNull String email) {}
