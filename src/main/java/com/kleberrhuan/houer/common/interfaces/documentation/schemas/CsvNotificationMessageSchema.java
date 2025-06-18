/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvNotificationMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsvNotificationMessage", description = "Estrutura de mensagem WebSocket para notificações em tempo real de importação CSV", implementation = CsvNotificationMessage.class, example = """
    {
      "jobId": "123e4567-e89b-12d3-a456-426614174000",
      "userId": 12345,
      "status": "PROCESSING",
      "filename": "escolas_sp_2025.csv",
      "totalRecords": 1500,
      "processedRecords": 750,
      "errorRecords": 5,
      "errorMessage": null,
      "timestamp": "2025-01-15T10:32:15Z"
    }
    """)
public interface CsvNotificationMessageSchema {
}