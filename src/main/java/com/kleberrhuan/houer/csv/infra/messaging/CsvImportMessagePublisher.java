/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.messaging;

import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Exchanges;
import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Queues;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportQueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvImportMessagePublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publishImportMessage(CsvImportQueueMessage message) {
    log.debug("Enviando mensagem para fila de importação: {}", message.jobId());
    rabbitTemplate.convertAndSend(
      Exchanges.CSV_IMPORT_EXCHANGE,
      Queues.CSV_IMPORT_QUEUE,
      message
    );
  }

  public void publishNotification(CsvImportNotification notification) {
    log.debug(
      "Enviando notificação para fila RabbitMQ - job {}: {}",
      notification.jobId(),
      notification.message()
    );

    rabbitTemplate.convertAndSend(
      Exchanges.CSV_NOTIFICATION_EXCHANGE,
      Queues.CSV_NOTIFICATION_QUEUE,
      notification
    );
  }
}
