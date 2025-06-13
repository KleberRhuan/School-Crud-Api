/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EmailSender(String name, @NotBlank @Email String email) {}
