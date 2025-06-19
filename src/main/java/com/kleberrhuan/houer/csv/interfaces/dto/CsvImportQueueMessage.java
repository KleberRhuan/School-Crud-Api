/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.dto;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/** Mensagem para fila RabbitMQ de processamento CSV. */
public record CsvImportQueueMessage(
  UUID jobId,
  String filename,
  URI fileUri,
  String description,
  Long userId,
  Instant scheduledAt
) {}
