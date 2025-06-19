/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.port;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;

/** Serviço responsável por entregar notificações de importação (WebSocket, e-mail, push, etc.). */
public interface NotificationService {
  void send(CsvImportNotification notification);
}
