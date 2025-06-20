/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.listener;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Listener de eventos para notificações de importação CSV.
 *
 * <p>Recebe eventos publicados pelo DefaultNotificationService e os propaga via WebSocket para clientes conectados.
 *
 * <p>Este listener é habilitado quando app.csv.rabbitmq.notifications.enabled=false (padrão) para usar notificações
 * locais em vez de RabbitMQ.
 */
@Component
@ConditionalOnProperty(
  name = "app.csv.rabbitmq.notifications.enabled",
  havingValue = "false",
  matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class CsvNotificationEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @EventListener
  public void handleCsvImportNotification(CsvImportNotification notification) {
    log.debug(
      "Processando evento local de notificação CSV: jobId={}, status={}, userId={}",
      notification.jobId(),
      notification.status(),
      notification.userId()
    );

    try {
      String publicTopic = "/topic/csv-import/" + notification.jobId();
      messagingTemplate.convertAndSend(publicTopic, notification);

      String userQueue = "/user/" + notification.userId() + "/queue/csv-import";
      messagingTemplate.convertAndSendToUser(
        notification.userId().toString(),
        "/queue/csv-import",
        notification
      );

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
        "Notificação WebSocket local enviada com sucesso: jobId={}, canais=[{}, {}]",
        notification.jobId(),
        publicTopic,
        userQueue
      );
    } catch (Exception e) {
      log.error(
        "Erro ao enviar notificação WebSocket local para job {}: {}",
        notification.jobId(),
        e.getMessage(),
        e
      );
    }
  }
}
