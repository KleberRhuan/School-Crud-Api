/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.service;

import com.kleberrhuan.houer.csv.application.port.NotificationService;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationService implements NotificationService {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void send(CsvImportNotification notification) {
    log.debug("Dispatching notification {}", notification);
    eventPublisher.publishEvent(notification);
  }
}
