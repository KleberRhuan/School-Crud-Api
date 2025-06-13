/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.email.brevo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SendSmtpEmailResponse(
  @JsonProperty("messageId") String messageId
) {}
