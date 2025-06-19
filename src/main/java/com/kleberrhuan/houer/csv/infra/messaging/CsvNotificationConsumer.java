/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.messaging;

import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumidor responsável por receber notificações de progresso do processamento CSV. Propaga essas notificações via
 * WebSocket para atualização em tempo real na interface.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CsvNotificationConsumer {

  private final SimpMessagingTemplate messagingTemplate;

  @RabbitListener(queues = CsvImportConstants.Queues.CSV_NOTIFICATION_QUEUE)
  public void receiveNotification(CsvImportNotification notification) {
    log.debug(
      "Notification received - Job: {}, Status: {}, Progress: {}/{}",
      notification.jobId(),
      notification.status(),
      notification.processedRecords(),
      notification.totalRecords()
    );

    messagingTemplate.convertAndSend(
      "/topic/csv-import/" + notification.jobId(),
      notification
    );
  }
}
