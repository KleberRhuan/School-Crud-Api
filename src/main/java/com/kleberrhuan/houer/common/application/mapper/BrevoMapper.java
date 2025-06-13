/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.mapper;

import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.infra.properties.BrevoProps;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailRecipient;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailSender;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.SendSmtpEmail;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrevoMapper {

  private final BrevoProps props;
  private final EmailSender sender = EmailSender
    .builder()
    .name(props.name())
    .email(props.email())
    .build();

  public SendSmtpEmail toBrevo(NotificationModel n) {
    return SendSmtpEmail
      .builder()
      .sender(sender)
      .to(List.of(new EmailRecipient(null, n.to())))
      .subject(n.subject())
      .htmlContent(n.message())
      .build();
  }
}
