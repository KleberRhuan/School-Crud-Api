/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.websocket;

import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CsvImportWebSocketController {

  private static final String DEST_PREFIX = "/queue/csv-import";
  private static final String MSG_INFO = "info";
  private static final String MSG_ERROR = "error";

  private final SimpMessagingTemplate messagingTemplate;
  private final CsvImportService csvImportService;
  private final Map<Long, Set<UUID>> subscriptions = new ConcurrentHashMap<>();

  @MessageMapping("/csv/subscribe/{jobId}")
  public void subscribe(
          @DestinationVariable String jobId,
          SimpMessageHeaderAccessor header
  ) {
    getUserId(header)
            .ifPresentOrElse(
                    userId -> {
                        UUID uuid = parseJobId(jobId, userId);
                        if (uuid == null) return;

                        CsvImportJob job = fetchAndAuthorize(uuid, userId);
                        if (job == null) return;

                        subscriptions
                                .computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet())
                                .add(uuid);

                        sendNotification(
                                userId,
                                buildNotification(job, "Inscrito com sucesso no job " + jobId)
                        );
                        log.info("Usuário {} inscrito no job {}", userId, jobId);
                    },
                    () -> log.warn("Tentativa de subscrição sem autenticação")
            );
  }

  @MessageMapping("/csv/unsubscribe/{jobId}")
  public void unsubscribe(
          @DestinationVariable String jobId,
          SimpMessageHeaderAccessor header
  ) {
    getUserId(header)
            .ifPresentOrElse(
                    userId -> {
                        UUID uuid = parseJobId(jobId, userId);
                        if (uuid != null) {
                            subscriptions
                                    .getOrDefault(userId, Collections.emptySet())
                                    .remove(uuid);
                        }

                        sendNotification(
                                userId,
                                new CsvImportNotification(
                                        null,
                                        userId,
                                        null,
                                        null,
                                        0,
                                        0,
                                        0,
                                        "Subscrição cancelada para job " + jobId,
                                        MSG_INFO,
                                        Instant.now()
                                )
                        );
                        log.info("Usuário {} cancelou job {}", userId, jobId);
                    },
                    () -> log.warn("Tentativa de cancelamento sem autenticação")
            );
  }

  @MessageMapping("/csv/status")
  public void status(
          @Payload Map<String, String> payload,
          SimpMessageHeaderAccessor header
  ) {
    getUserId(header)
            .ifPresentOrElse(
                    userId -> {
                        String jobId = payload.get("jobId");
                        if (jobId == null || jobId.isBlank()) {
                            sendError(userId, "ID do job é obrigatório");
                            return;
                        }

                        UUID uuid = parseJobId(jobId, userId);
                        if (uuid == null) return;

                        CsvImportJob job = fetchAndAuthorize(uuid, userId);
                        if (job == null) return;

                        sendNotification(
                                userId,
                                buildNotification(job, "Status atual do job " + jobId)
                        );
                        log.debug("Status do job {} enviado para {}", jobId, userId);
                    },
                    () -> log.warn("Tentativa de solicitar status sem autenticação")
            );
  }

  @MessageMapping("/csv/list-jobs")
  public void listJobs(SimpMessageHeaderAccessor header) {
    getUserId(header)
            .ifPresentOrElse(
                    userId -> {
                        sendNotification(
                                userId,
                                new CsvImportNotification(
                                        null,
                                        userId,
                                        null,
                                        null,
                                        0,
                                        0,
                                        0,
                                        "Para listar jobs, use GET /api/v1/csv/jobs",
                                        MSG_INFO,
                                        Instant.now()
                                )
                        );
                        log.debug("Sugestão de REST enviada para {}", userId);
                    },
                    () -> log.warn("Tentativa de listar jobs sem autenticação")
            );
  }

  // —————————————————————————————————————————————————————————
  // Métodos auxiliares
  // —————————————————————————————————————————————————————————

  private Optional<Long> getUserId(SimpMessageHeaderAccessor header) {
    Authentication auth = (Authentication) header
            .getSessionAttributes()
            .get("user");

    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      try {
        return Optional.of(Long.valueOf(jwt.getClaim("sub")));
      } catch (Exception e) {
        log.error("Claim 'sub' inválido: {}", (Object) jwt.getClaim("sub"));
      }
    }
    return Optional.empty();
  }

  private UUID parseJobId(String jobId, Long userId) {
    try {
      return UUID.fromString(jobId);
    } catch (IllegalArgumentException e) {
      sendError(userId, "ID do job inválido: " + jobId);
      log.error("ID inválido {}", jobId);
      return null;
    }
  }

  private CsvImportJob fetchAndAuthorize(UUID uuid, Long userId) {
    try {
      CsvImportJob job = csvImportService.findJobById(uuid);
      if (!Objects.equals(job.getCreatedBy(), userId)) {
        sendError(userId, "Acesso negado ao job " + uuid);
        log.warn("Usuário {} sem permissão para {}", userId, uuid);
        return null;
      }
      return job;
    } catch (Exception e) {
      sendError(userId, "Erro ao acessar job " + uuid);
      log.error("Erro ao buscar job {}: {}", uuid, e.getMessage());
      return null;
    }
  }

  private void sendNotification(Long userId, CsvImportNotification notif) {
    messagingTemplate.convertAndSendToUser(
            userId.toString(),
            DEST_PREFIX,
            notif
    );
  }

  private void sendError(Long userId, String msg) {
    sendNotification(
            userId,
            new CsvImportNotification(
                    null,
                    userId,
                    null,
                    null,
                    0,
                    0,
                    0,
                    msg,
                    MSG_ERROR,
                    Instant.now()
            )
    );
  }

  private CsvImportNotification buildNotification(
          CsvImportJob job,
          String message
  ) {
    return new CsvImportNotification(
            job.getId(),
            job.getCreatedBy(),
            job.getStatus(),
            job.getFilename(),
            job.getTotalRecords() != null ? job.getTotalRecords() : 0,
            job.getProcessedRecords() != null ? job.getProcessedRecords() : 0,
            job.getErrorRecords() != null ? job.getErrorRecords() : 0,
            message,
            CsvImportWebSocketController.MSG_INFO,
            Instant.now()
    );
  }
}
