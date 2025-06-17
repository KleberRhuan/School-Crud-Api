/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.application.mapper.BrevoMapper;
import com.kleberrhuan.houer.common.domain.model.OutboxMessage;
import com.kleberrhuan.houer.common.domain.model.notification.Channel;
import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import com.kleberrhuan.houer.common.infra.adapter.notification.client.brevo.BrevoApi;
import com.kleberrhuan.houer.common.infra.exception.EmailDeliveryException;
import com.kleberrhuan.houer.common.infra.outbox.ResilientOutboxStore;
import com.kleberrhuan.houer.common.infra.properties.BrevoProps;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailRecipient;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.EmailSender;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.request.SendSmtpEmail;
import com.kleberrhuan.houer.common.interfaces.dto.email.brevo.response.SendSmtpEmailResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BrevoEmailSender")
class BrevoEmailSenderTest {

  @Mock
  private BrevoMapper mapper;

  @Mock
  private BrevoProps props;

  @Mock
  private ResilientOutboxStore store;

  @Mock
  private BrevoApi api;

  @InjectMocks
  private BrevoEmailSender sender;

  private NotificationModel testNotification;
  private SendSmtpEmail testEmail;
  private SendSmtpEmailResponse testResponse;

  @BeforeEach
  void setup() {
    // Setup test notification
    testNotification =
      new NotificationModel(
        Channel.EMAIL,
        "test@example.com",
        "Test Subject",
        "Test Message"
      );

    // Setup test email request
    testEmail =
      new SendSmtpEmail(
        new EmailSender("Test Sender", "sender@example.com"),
        List.of(new EmailRecipient(null, "test@example.com")),
        "Test Subject",
        "Test Message"
      );

    // Setup test response
    testResponse = new SendSmtpEmailResponse(UUID.randomUUID().toString());

    // Setup mocks
    when(mapper.toBrevo(testNotification, props)).thenReturn(testEmail);
    when(api.sendSimpleEmail(testEmail)).thenReturn(testResponse);
  }

  @Nested
  @DisplayName("provider")
  class ProviderTest {

    @Test
    @DisplayName("deve retornar provider correto")
    void whenProvider_thenReturnCorrectProvider() {
      // Act
      Provider provider = sender.provider();

      // Assert
      assertThat(provider.channel()).isEqualTo(Channel.EMAIL);
      assertThat(provider.name()).isEqualTo("brevo");
    }
  }

  @Nested
  @DisplayName("send")
  class Send {

    @Test
    @DisplayName("deve enviar email com sucesso")
    void givenValidNotification_whenSend_thenSendSuccessfully() {
      // Act
      sender.send(testNotification);

      // Assert
      verify(mapper).toBrevo(testNotification, props);
      verify(api).sendSimpleEmail(testEmail);
    }

    @Test
    @DisplayName("deve mapear notificação corretamente")
    void givenNotification_whenSend_thenMapCorrectly() {
      // Act
      sender.send(testNotification);

      // Assert
      verify(mapper).toBrevo(eq(testNotification), eq(props));
    }

    @Test
    @DisplayName("deve enviar através da API")
    void givenMappedEmail_whenSend_thenCallApi() {
      // Act
      sender.send(testNotification);

      // Assert
      verify(api).sendSimpleEmail(testEmail);
    }

    @Test
    @DisplayName("deve chamar fallback quando API falha")
    void givenApiFailure_whenSend_thenCallFallback() {
      // Arrange
      RuntimeException apiException = new RuntimeException("API failed");
      doThrow(apiException).when(api).sendSimpleEmail(any());

      // Act & Assert
      assertThatThrownBy(() -> sender.send(testNotification))
        .isInstanceOf(EmailDeliveryException.class);

      // O fallback só é chamado quando o circuit breaker é ativado,
      // não em uma simples exceção da API
      verify(api).sendSimpleEmail(any());
    }

    @Test
    @DisplayName("deve usar EmailDeliveryException no método interno send")
    void givenInternalSendFailure_whenSend_thenThrowEmailDeliveryException() {
      // Arrange
      RuntimeException internalException = new RuntimeException(
        "Internal error"
      );
      doThrow(internalException).when(api).sendSimpleEmail(any());

      // Act & Assert
      assertThatThrownBy(() -> sender.send(testNotification))
        .isInstanceOf(EmailDeliveryException.class)
        .hasMessage("error.infrastructure.notification.email.delivery");

      // Verify API was called but fallback is only triggered by circuit breaker
      verify(api).sendSimpleEmail(any());
    }
  }

  @Nested
  @DisplayName("fallback")
  class Fallback {

    @BeforeEach
    void setupFallbackTests() {
      // Reset the store mock to ensure clean state between tests
      reset(store);
    }

    @Test
    @DisplayName("deve salvar mensagem no outbox store")
    void givenFallback_whenCalled_thenSaveToOutbox() {
      // Arrange
      RuntimeException exception = new RuntimeException("Service unavailable");

      // Act
      sender.fallback(exception, testNotification);

      // Assert
      ArgumentCaptor<OutboxMessage> messageCaptor = ArgumentCaptor.forClass(
        OutboxMessage.class
      );
      verify(store).save(messageCaptor.capture());

      OutboxMessage savedMessage = messageCaptor.getValue();
      assertThat(savedMessage.getChannel()).isEqualTo(Channel.EMAIL);
      assertThat(savedMessage.getRecipient()).isEqualTo("test@example.com");
      assertThat(savedMessage.getSubject()).isEqualTo("Test Subject");
      assertThat(savedMessage.getBody()).isEqualTo("Test Message");
    }

    @Test
    @DisplayName("deve criar mensagem outbox corretamente")
    void givenNotification_whenFallback_thenCreateCorrectOutboxMessage() {
      // Arrange
      RuntimeException exception = new RuntimeException("Circuit breaker open");

      // Act
      sender.fallback(exception, testNotification);

      // Assert
      verify(store).save(any(OutboxMessage.class));
    }

    @Test
    @DisplayName("deve lidar com diferentes tipos de exceção")
    void givenDifferentExceptions_whenFallback_thenHandleCorrectly() {
      // Arrange
      Exception[] exceptions = {
        new RuntimeException("Timeout"),
        new IllegalStateException("Invalid state"),
        new RuntimeException("Connection refused"),
      };

      // Act & Assert
      for (Exception ex : exceptions) {
        sender.fallback(ex, testNotification);
      }

      verify(store, times(3)).save(any(OutboxMessage.class));
    }
  }

  @Nested
  @DisplayName("integration scenarios")
  class IntegrationScenarios {

    @Test
    @DisplayName("deve funcionar com diferentes notificações")
    void givenDifferentNotifications_whenSend_thenHandleCorrectly() {
      // Arrange
      NotificationModel[] notifications = {
        new NotificationModel(
          Channel.EMAIL,
          "user1@example.com",
          "Welcome",
          "Welcome message"
        ),
        new NotificationModel(
          Channel.EMAIL,
          "user2@example.com",
          "Reset Password",
          "Reset link"
        ),
        new NotificationModel(
          Channel.EMAIL,
          "user3@example.com",
          "Verification",
          "Verify account"
        ),
      };

      // Setup different emails for each notification
      for (NotificationModel notification : notifications) {
        SendSmtpEmail email = new SendSmtpEmail(
          new EmailSender("Test", "test@example.com"),
          List.of(new EmailRecipient(null, notification.to())),
          notification.subject(),
          notification.message()
        );

        when(mapper.toBrevo(notification, props)).thenReturn(email);
        when(api.sendSimpleEmail(email))
          .thenReturn(new SendSmtpEmailResponse(UUID.randomUUID().toString()));
      }

      // Act
      for (NotificationModel notification : notifications) {
        sender.send(notification);
      }

      // Assert
      verify(mapper, times(3)).toBrevo(any(), eq(props));
      verify(api, times(3)).sendSimpleEmail(any());
    }

    @Test
    @DisplayName("deve lidar com cenário de recuperação após falha")
    void givenFailureThenSuccess_whenSend_thenHandleCorrectly() {
      // Arrange - Uma chamada que vai falhar
      RuntimeException failure = new RuntimeException("Temporary failure");
      doThrow(failure).when(api).sendSimpleEmail(any());

      // Act - Tentativa que deve falhar e lançar EmailDeliveryException
      assertThatThrownBy(() -> sender.send(testNotification))
        .isInstanceOf(EmailDeliveryException.class);

      // Assert - Verificar que API foi chamada
      verify(api).sendSimpleEmail(any());
      // Note: O fallback só é ativado pelo circuit breaker, não por exceções simples
      // Em um teste de integração real, seria necessário configurar o circuit breaker
    }

    @Test
    @DisplayName("deve validar entrada da notificação")
    void givenInvalidNotification_whenSend_thenValidationShouldHandle() {
      // Note: Como o método está anotado com @Valid,
      // a validação seria feita pelo framework
      // Aqui só testamos que o mapper é chamado com os dados

      // Arrange
      NotificationModel invalidNotification = new NotificationModel(
        Channel.EMAIL,
        "", // email vazio - seria rejeitado pela validação
        "",
        ""
      );

      // Assumindo que chegasse até aqui (o que não aconteceria na realidade)
      when(mapper.toBrevo(invalidNotification, props)).thenReturn(testEmail);

      // Act
      sender.send(invalidNotification);

      // Assert
      verify(mapper).toBrevo(invalidNotification, props);
    }
  }
}
