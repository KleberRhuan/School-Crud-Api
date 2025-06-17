/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.application.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.auth.domain.event.UserRegisteredEvent;
import com.kleberrhuan.houer.common.application.dispatcher.notification.NotificationDispatcher;
import com.kleberrhuan.houer.common.application.service.notification.MailTemplateService;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationMailListener")
class VerificationMailListenerTest {

  @Mock
  MailTemplateService templating;

  @Mock
  NotificationDispatcher dispatcher;

  @Test
  @DisplayName("handle deve renderizar template e despachar email")
  void handleEvent() {
    when(templating.render(eq("verify-account"), any(Map.class)))
      .thenReturn("<html>email</html>");

    VerificationMailListener listener = new VerificationMailListener(
      templating,
      dispatcher
    );

    UserRegisteredEvent evt = new UserRegisteredEvent(
      "user@test.com",
      "User",
      "http://verify"
    );

    listener.handle(evt);

    // verifica render chamado com parâmetros corretos
    verify(templating)
      .render(
        eq("verify-account"),
        argThat(m ->
          "User".equals(m.get("name")) &&
          "http://verify".equals(m.get("verifyLink"))
        )
      );

    // captura notificação
    ArgumentCaptor<NotificationModel> captor = ArgumentCaptor.forClass(
      NotificationModel.class
    );
    verify(dispatcher).dispatch(captor.capture());
    NotificationModel sent = captor.getValue();

    assertThat(sent.channel()).isEqualTo(Channel.EMAIL);
    assertThat(sent.to()).isEqualTo("user@test.com");
    assertThat(sent.subject()).isEqualTo("Confirme sua conta");
    assertThat(sent.message()).isEqualTo("<html>email</html>");
  }
}
