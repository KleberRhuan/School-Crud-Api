/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
public record SendSmtpEmail(
  @NotNull EmailSender sender,
  @NotNull List<EmailRecipient> to,
  @NotBlank String subject,
  @NotBlank String htmlContent
) {}
