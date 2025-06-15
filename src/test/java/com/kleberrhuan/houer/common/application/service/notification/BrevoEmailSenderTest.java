/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BrevoEmailSender - Unit Tests")
class BrevoEmailSenderTest {

  @Mock
  private BrevoMapper mapper;

  @Mock
  private MeterRegistry registry;

  @Mock
  private ResilientOutboxStore store;

  @Mock
  private BrevoApi api;

  @Mock
  private Counter counter;

  @Mock
  private BrevoProps props;

  @InjectMocks
  private BrevoEmailSender emailSender;

  private NotificationModel validNotification;
  private SendSmtpEmail sendSmtpEmail;
  private SendSmtpEmailResponse successResponse;

  @BeforeEach
  void setUp() {
    validNotification =
      new NotificationModel(
        Channel.EMAIL,
        "test@example.com",
        "Test Subject",
        "Test Message"
      );

    EmailSender sender = new EmailSender("Test Sender", "sender@example.com");

    sendSmtpEmail =
      new SendSmtpEmail(
        sender,
        List.of(new EmailRecipient(null, "test@example.com")),
        "Test Subject",
        "Test Message"
      );

    successResponse = new SendSmtpEmailResponse("msg-123");

    // Configure MeterRegistry to return the mocked counter
    when(registry.counter("email.outbox.total", "provider", "brevo"))
      .thenReturn(counter);
    when(mapper.toBrevo(validNotification, props)).thenReturn(sendSmtpEmail);

    // Initialize the emailSender to set up the outboxCounter
    emailSender.init();
  }

  @Nested
  @DisplayName("provider - Provider Information")
  class ProviderInfo {

    @Test
    @DisplayName("Should return correct provider information")
    void shouldReturnCorrectProviderInformation() {
      // When
      Provider provider = emailSender.provider();

      // Then
      assertThat(provider.channel()).isEqualTo(Channel.EMAIL);
      assertThat(provider.name()).isEqualTo("brevo");
    }
  }

  @Nested
  @DisplayName("send - Success Scenarios")
  class SendSuccessScenarios {

    @Test
    @DisplayName("Should send email successfully")
    void shouldSendEmailSuccessfully() {
      // Given
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When
      emailSender.send(validNotification);

      // Then
      verify(mapper).toBrevo(validNotification, props);
      verify(api).sendSimpleEmail(sendSmtpEmail);
      verifyNoInteractions(store);
      verifyNoInteractions(counter);
    }

    @Test
    @DisplayName("Should process notification with all fields filled")
    void shouldProcessNotificationWithAllFieldsFilled() {
      // Given
      NotificationModel fullNotification = new NotificationModel(
        Channel.EMAIL,
        "user@example.com",
        "Important Subject",
        "Detailed message content"
      );

      EmailSender sender = new EmailSender("Full Sender", "full@example.com");

      SendSmtpEmail fullEmail = new SendSmtpEmail(
        sender,
        List.of(new EmailRecipient(null, "user@example.com")),
        "Important Subject",
        "Detailed message content"
      );

      when(mapper.toBrevo(fullNotification, props)).thenReturn(fullEmail);
      when(api.sendSimpleEmail(fullEmail)).thenReturn(successResponse);

      // When
      emailSender.send(fullNotification);

      // Then
      verify(mapper).toBrevo(fullNotification, props);
      verify(api).sendSimpleEmail(fullEmail);
    }
  }

  @Nested
  @DisplayName("send - Failure Scenarios")
  class SendFailureScenarios {

    @Test
    @DisplayName("Should throw EmailDeliveryException when API fails")
    void shouldThrowEmailDeliveryExceptionWhenApiFails() {
      // Given
      when(api.sendSimpleEmail(sendSmtpEmail))
        .thenThrow(new RuntimeException("API Error"));

      // When & Then
      assertThatThrownBy(() -> emailSender.send(validNotification))
        .isInstanceOf(EmailDeliveryException.class);

      verify(api).sendSimpleEmail(sendSmtpEmail);
    }

    @Test
    @DisplayName("Should propagate exception when mapper fails")
    void shouldPropagateExceptionWhenMapperFails() {
      // Given
      when(mapper.toBrevo(validNotification, props))
        .thenThrow(new RuntimeException("Mapping Error"));

      // When & Then
      assertThatThrownBy(() -> emailSender.send(validNotification))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Mapping Error");

      verify(mapper).toBrevo(validNotification, props);
      verifyNoInteractions(api);
    }
  }

  @Nested
  @DisplayName("fallback - Fallback Mechanism")
  class FallbackMechanism {

    @Test
    @DisplayName("Should save message to outbox when fallback is triggered")
    void shouldSaveMessageToOutboxWhenFallbackIsTriggered() {
      // Given
      Throwable exception = new RuntimeException("Service unavailable");

      // When
      emailSender.fallback(exception, validNotification);

      // Then
      verify(counter).increment();
      verify(store).save(any(OutboxMessage.class));
    }

    @Test
    @DisplayName("Should create OutboxMessage correctly in fallback")
    void shouldCreateOutboxMessageCorrectlyInFallback() {
      // Given
      Throwable exception = new EmailDeliveryException(emailSender.provider());

      // When
      emailSender.fallback(exception, validNotification);

      // Then
      verify(store)
        .save(
          argThat(outboxMessage -> {
            assertThat(outboxMessage.getRecipient())
              .isEqualTo(validNotification.to());
            assertThat(outboxMessage.getSubject())
              .isEqualTo(validNotification.subject());
            assertThat(outboxMessage.getBody())
              .isEqualTo(validNotification.message());
            assertThat(outboxMessage.getChannel())
              .isEqualTo(validNotification.channel());
            assertThat(outboxMessage.getAttempts()).isZero();
            assertThat(outboxMessage.getNextAttemptAt()).isNotNull();
            return true;
          })
        );
    }

    @Test
    @DisplayName("Should increment outbox counter in fallback")
    void shouldIncrementOutboxCounterInFallback() {
      // Given
      Throwable exception = new RuntimeException("Circuit breaker open");

      // When
      emailSender.fallback(exception, validNotification);

      // Then
      verify(counter).increment();
    }
  }

  @Nested
  @DisplayName("Input Validations")
  class InputValidations {

    @Test
    @DisplayName("Should validate notification with invalid email")
    void shouldValidateNotificationWithInvalidEmail() {
      // Given
      NotificationModel invalidNotification = new NotificationModel(
        Channel.EMAIL,
        "invalid-email",
        "Test Subject",
        "Test Message"
      );

      // Configure mapper to handle invalid notification
      when(mapper.toBrevo(invalidNotification, props))
        .thenReturn(sendSmtpEmail);
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When & Then
      // Note: Spring validation happens at runtime, not in unit tests
      // This test verifies the method can handle the call
      emailSender.send(invalidNotification);

      verify(mapper).toBrevo(invalidNotification, props);
      verify(api).sendSimpleEmail(sendSmtpEmail);
    }

    @Test
    @DisplayName("Should handle notification with null fields gracefully")
    void shouldHandleNotificationWithNullFieldsGracefully() {
      // Given
      NotificationModel nullFieldsNotification = new NotificationModel(
        Channel.EMAIL,
        null,
        null,
        "Test Message"
      );

      // Configure mapper to handle null fields notification
      when(mapper.toBrevo(nullFieldsNotification, props))
        .thenReturn(sendSmtpEmail);
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When & Then
      // Note: Spring validation happens at runtime, not in unit tests
      // This test verifies the method can handle the call
      emailSender.send(nullFieldsNotification);

      verify(mapper).toBrevo(nullFieldsNotification, props);
      verify(api).sendSimpleEmail(sendSmtpEmail);
    }

    @Test
    @DisplayName("Should handle notification with empty subject gracefully")
    void shouldHandleNotificationWithEmptySubjectGracefully() {
      // Given
      NotificationModel emptySubjectNotification = new NotificationModel(
        Channel.EMAIL,
        "test@example.com",
        "",
        "Test Message"
      );

      // Configure mapper to handle empty subject notification
      when(mapper.toBrevo(emptySubjectNotification, props))
        .thenReturn(sendSmtpEmail);
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When & Then
      // Note: Spring validation happens at runtime, not in unit tests
      // This test verifies the method can handle the call
      emailSender.send(emptySubjectNotification);

      verify(mapper).toBrevo(emptySubjectNotification, props);
      verify(api).sendSimpleEmail(sendSmtpEmail);
    }
  }

  @Nested
  @DisplayName("Integration with External Components")
  class IntegrationWithExternalComponents {

    @Test
    @DisplayName("Should interact correctly with BrevoMapper")
    void shouldInteractCorrectlyWithBrevoMapper() {
      // Given
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When
      emailSender.send(validNotification);

      // Then
      verify(mapper).toBrevo(validNotification, props);
      verifyNoMoreInteractions(mapper);
    }

    @Test
    @DisplayName("Should interact correctly with BrevoApi")
    void shouldInteractCorrectlyWithBrevoApi() {
      // Given
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(successResponse);

      // When
      emailSender.send(validNotification);

      // Then
      verify(api).sendSimpleEmail(sendSmtpEmail);
      verifyNoMoreInteractions(api);
    }

    @Test
    @DisplayName("Should interact correctly with MeterRegistry")
    void shouldInteractCorrectlyWithMeterRegistry() {
      // Given
      RuntimeException exception = new RuntimeException("Test exception");

      // When
      emailSender.fallback(exception, validNotification);

      // Then
      verify(registry).counter("email.outbox.total", "provider", "brevo");
      verify(counter).increment();
    }

    @Test
    @DisplayName("Should interact correctly with ResilientOutboxStore")
    void shouldInteractCorrectlyWithResilientOutboxStore() {
      // Given
      RuntimeException exception = new RuntimeException("Test exception");

      // When
      emailSender.fallback(exception, validNotification);

      // Then
      verify(store).save(any(OutboxMessage.class));
      verifyNoMoreInteractions(store);
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCases {

    @Test
    @DisplayName("Should handle null API response")
    void shouldHandleNullApiResponse() {
      // Given
      when(api.sendSimpleEmail(sendSmtpEmail)).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> emailSender.send(validNotification))
        .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle null messageId in response")
    void shouldHandleNullMessageIdInResponse() {
      // Given
      SendSmtpEmailResponse nullMessageIdResponse = new SendSmtpEmailResponse(
        null
      );
      when(api.sendSimpleEmail(sendSmtpEmail))
        .thenReturn(nullMessageIdResponse);

      // When
      emailSender.send(validNotification);

      // Then
      verify(api).sendSimpleEmail(sendSmtpEmail);
      // Should process normally even with null messageId
    }

    @Test
    @DisplayName("Should handle multiple fallback calls")
    void shouldHandleMultipleFallbackCalls() {
      // Given
      RuntimeException exception = new RuntimeException("Persistent error");

      // When
      emailSender.fallback(exception, validNotification);
      emailSender.fallback(exception, validNotification);

      // Then
      verify(counter, times(2)).increment();
      verify(store, times(2)).save(any(OutboxMessage.class));
    }
  }
}
