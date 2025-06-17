/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.dispatcher.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.port.notification.Notification;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.infra.exception.ProviderNotFoundException;
import com.kleberrhuan.houer.common.infra.properties.ProviderProps;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationDispatcherTest {

  private Notification emailSvc;
  private NotificationDispatcher dispatcher;

  @BeforeEach
  void setup() {
    // mock de Notification para EMAIL/sendgrid
    emailSvc = mock(Notification.class);
    when(emailSvc.provider())
      .thenReturn(new Provider(Channel.EMAIL, "sendgrid"));

    ProviderProps props = new ProviderProps("sendgrid");
    dispatcher = new NotificationDispatcher(List.of(emailSvc), props);
    // init @PostConstruct manual
    dispatcher.init();
  }

  @Test
  @DisplayName("deve despachar notificação para provider configurado")
  void shouldDispatchToConfiguredProvider() {
    NotificationModel n = new NotificationModel(
      Channel.EMAIL,
      "foo@bar.com",
      "hi",
      "hello"
    );

    dispatcher.dispatch(n);

    verify(emailSvc).send(n);
  }

  @Test
  @DisplayName(
    "deve lançar ProviderNotFoundException para provider desconhecido"
  )
  void shouldThrowWhenProviderNotFound() {
    // Props aponta para provider inexistente
    ProviderProps wrong = new ProviderProps("mailgun");
    NotificationDispatcher d = new NotificationDispatcher(
      List.of(emailSvc),
      wrong
    );
    d.init();

    NotificationModel n = new NotificationModel(
      Channel.EMAIL,
      "foo@bar.com",
      "hi",
      "hello"
    );

    assertThatThrownBy(() -> d.dispatch(n))
      .isInstanceOf(ProviderNotFoundException.class);
  }
}
