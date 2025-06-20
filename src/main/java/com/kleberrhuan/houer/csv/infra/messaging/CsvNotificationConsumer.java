/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.messaging;

import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
  name = "app.csv.rabbitmq.notifications.enabled",
  havingValue = "true",
  matchIfMissing = false
)
@RequiredArgsConstructor
@Slf4j
public class CsvNotificationConsumer {

  private final SimpMessagingTemplate messagingTemplate;

  @RabbitListener(queues = CsvImportConstants.Queues.CSV_NOTIFICATION_QUEUE)
  public void receiveNotification(CsvImportNotification notification) {
    log.debug(
      "Notification received from RabbitMQ - Job: {}, Status: {}, Progress: {}/{}",
      notification.jobId(),
      notification.status(),
      notification.processedRecords(),
      notification.totalRecords()
    );

    messagingTemplate.convertAndSend(
      "/topic/csv-import/" + notification.jobId(),
      notification
    );

    log.debug("Notification forwarded to WebSocket from RabbitMQ");
  }
}
