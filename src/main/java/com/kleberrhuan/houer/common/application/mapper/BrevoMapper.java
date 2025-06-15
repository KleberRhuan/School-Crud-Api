/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.mapper;

import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.infra.properties.BrevoProps;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailRecipient;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailSender;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.SendSmtpEmail;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
  componentModel = "spring",
  imports = { List.class, EmailRecipient.class }
)
public interface BrevoMapper {
  @Mapping(target = "sender", expression = "java(buildSender(props))")
  @Mapping(
    target = "to",
    expression = "java(List.of(new EmailRecipient(null, n.to())))"
  )
  @Mapping(target = "subject", source = "n.subject")
  @Mapping(target = "htmlContent", source = "n.message")
  SendSmtpEmail toBrevo(NotificationModel n, @Context BrevoProps props);

  default EmailSender buildSender(@Context BrevoProps props) {
    return EmailSender
      .builder()
      .email(props.email())
      .name(props.name())
      .build();
  }
}
