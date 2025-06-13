/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.notification;

import com.kleberrhuan.houer.common.domain.model.notification.NotificationModel;
import com.kleberrhuan.houer.common.domain.model.notification.Provider;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface Notification {
  Provider provider();

  void send(@Valid NotificationModel notificationModel);
}
