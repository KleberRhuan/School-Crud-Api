/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.listener;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvNotificationEventListener Tests")
class CsvNotificationEventListenerTest {

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private CsvNotificationEventListener listener;

  private CsvImportNotification notification;
  private UUID jobId;
  private Long userId;

  @BeforeEach
  void setUp() {
    jobId = UUID.randomUUID();
    userId = 123L;

    notification =
      new CsvImportNotification(
        jobId,
        userId,
        ImportJobStatus.RUNNING,
        "test.csv",
        100,
        50,
        2,
        "Processando arquivo",
        "info",
        Instant.now()
      );
  }

  @Test
  @DisplayName("Deve enviar notificação para canal público e fila privada")
  void shouldSendNotificationToPublicAndPrivateChannels() {
    // When
    listener.handleCsvImportNotification(notification);

    // Then
    verify(messagingTemplate)
      .convertAndSend(eq("/topic/csv-import/" + jobId), eq(notification));

    verify(messagingTemplate)
      .convertAndSendToUser(
        eq(userId.toString()),
        eq("/queue/csv-import"),
        eq(notification)
      );
  }

  @Test
  @DisplayName("Deve enviar para fila de progresso quando status for RUNNING")
  void shouldSendToProgressQueueWhenStatusIsRunning() {
    // When
    listener.handleCsvImportNotification(notification);

    // Then
    verify(messagingTemplate)
      .convertAndSendToUser(
        eq(userId.toString()),
        eq("/queue/csv-progress"),
        eq(notification)
      );
  }

  @Test
  @DisplayName(
    "Não deve enviar para fila de progresso quando status não for RUNNING"
  )
  void shouldNotSendToProgressQueueWhenStatusIsNotRunning() {
    // Given
    CsvImportNotification completedNotification = new CsvImportNotification(
      jobId,
      userId,
      ImportJobStatus.COMPLETED,
      "test.csv",
      100,
      100,
      0,
      "Processamento concluído",
      "success",
      Instant.now()
    );

    // When
    listener.handleCsvImportNotification(completedNotification);

    // Then
    verify(messagingTemplate, never())
      .convertAndSendToUser(
        eq(userId.toString()),
        eq("/queue/csv-progress"),
        any()
      );
  }

  @Test
  @DisplayName("Deve continuar funcionando mesmo com erro no WebSocket")
  void shouldContinueWorkingEvenWithWebSocketError() {
    // Given
    doThrow(new RuntimeException("WebSocket error"))
      .when(messagingTemplate)
      .convertAndSend(
        eq("/topic/csv-import/" + jobId),
        any(CsvImportNotification.class)
      );

    // When/Then - não deve lançar exceção
    listener.handleCsvImportNotification(notification);
  }
}
