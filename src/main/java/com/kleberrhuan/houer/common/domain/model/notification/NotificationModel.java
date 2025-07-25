/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.model.notification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificationModel(
  Channel channel,
  @NotBlank @Email String to,
  @NotBlank String subject,
  @NotBlank String message
) {}
