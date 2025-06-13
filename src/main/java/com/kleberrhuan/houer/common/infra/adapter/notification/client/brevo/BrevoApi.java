/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.adapter.notification.client.brevo;

import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.SendSmtpEmail;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.response.SendSmtpEmailResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/smtp/email")
public interface BrevoApi {
  @PostExchange
  SendSmtpEmailResponse sendSimpleEmail(@RequestBody SendSmtpEmail payload);
}
