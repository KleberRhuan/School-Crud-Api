/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.listener;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listener de eventos para notificações de importação CSV.
 *
 * <p>Recebe eventos publicados pelo DefaultNotificationService e os propaga via WebSocket para clientes conectados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CsvNotificationEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @EventListener
  public void handleCsvImportNotification(CsvImportNotification notification) {
    log.debug(
      "Processando evento de notificação CSV: jobId={}, status={}, userId={}",
      notification.jobId(),
      notification.status(),
      notification.userId()
    );

    try {
      // Envia para canal público do job específico
      String publicTopic = "/topic/csv-import/" + notification.jobId();
      messagingTemplate.convertAndSend(publicTopic, notification);

      // Envia para fila privada do usuário
      String userQueue = "/user/" + notification.userId() + "/queue/csv-import";
      messagingTemplate.convertAndSendToUser(
        notification.userId().toString(),
        "/queue/csv-import",
        notification
      );

      // Envia para fila de progresso se status for RUNNING
      if ("RUNNING".equals(notification.status().name())) {
        String progressQueue =
          "/user/" + notification.userId() + "/queue/csv-progress";
        messagingTemplate.convertAndSendToUser(
          notification.userId().toString(),
          "/queue/csv-progress",
          notification
        );
      }

      log.debug(
        "Notificação WebSocket enviada com sucesso: jobId={}, canais=[{}, {}]",
        notification.jobId(),
        publicTopic,
        userQueue
      );
    } catch (Exception e) {
      log.error(
        "Erro ao enviar notificação WebSocket para job {}: {}",
        notification.jobId(),
        e.getMessage(),
        e
      );
    }
  }
}
