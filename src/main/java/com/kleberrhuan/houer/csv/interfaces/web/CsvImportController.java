/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.web;

import com.kleberrhuan.houer.common.interfaces.documentation.controllers.CsvImportControllerDocumentation;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.csv.application.mapper.CsvImportMapper;
import com.kleberrhuan.houer.csv.application.service.ConcurrentSafeCsvImportService;
import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportRequestDto;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/csv")
@RequiredArgsConstructor
@Slf4j
public class CsvImportController implements CsvImportControllerDocumentation {

  private final CsvImportService csvImportService;
  private final ConcurrentSafeCsvImportService concurrentSafeCsvImportService;
  private final CsvImportMapper csvImportMapper;

  @PostMapping(
    value = "/import",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<CsvImportResponseDto> startImport(
    @Valid @ModelAttribute CsvImportRequestDto request,
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      log.warn("Tentativa de importação sem usuário autenticado");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UUID jobId = concurrentSafeCsvImportService.startImport(
      request.file(),
      userId
    );

    CsvImportJob job = csvImportService.findJobById(jobId);
    CsvImportResponseDto response = csvImportMapper.toResponseDto(job);

    log.info(
      "Importação CSV iniciada com controle de concorrência: jobId={}, arquivo={}, usuário={}",
      jobId,
      request.file().getOriginalFilename(),
      userId
    );

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<CsvImportResponseDto> getJobDetails(
    @PathVariable UUID jobId,
    Authentication auth
  ) {
    try {
      CsvImportJob job = csvImportService.findJobById(jobId);

      Long userId = getCurrentUserId(auth);
      if (userId == null || !job.getCreatedBy().equals(userId)) {
        log.warn(
          "Usuário {} tentou acessar job {} sem permissão",
          userId,
          jobId
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      CsvImportResponseDto response = csvImportMapper.toResponseDto(job);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Erro ao buscar detalhes do job {}: {}", jobId, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/jobs")
  public ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listUserJobs(
    PageableRequest pageableRequest,
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    PaginatedResponse<List<CsvImportJob>> jobs =
      csvImportService.findJobsByUser(userId, pageableRequest);

    return getPaginatedResponseResponseEntity(jobs);
  }

  @GetMapping("/jobs/active")
  public ResponseEntity<List<CsvImportResponseDto>> listActiveJobs(
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      // Buscar jobs ativos (PENDING, RUNNING) do usuário
      PageableRequest pageableRequest = new PageableRequest(
        0,
        50,
        "createdAt",
        Sort.Direction.DESC
      );
      PaginatedResponse<List<CsvImportJob>> jobs =
        csvImportService.findJobsByUser(userId, pageableRequest);

      List<CsvImportResponseDto> activeJobs = jobs
        .content()
        .stream()
        .filter(job ->
          job.getStatus() == ImportJobStatus.PENDING ||
          job.getStatus() == ImportJobStatus.RUNNING
        )
        .map(csvImportMapper::toResponseDto)
        .toList();

      log.debug("Usuário {} tem {} jobs ativos", userId, activeJobs.size());
      return ResponseEntity.ok(activeJobs);
    } catch (Exception e) {
      log.error(
        "Erro ao listar jobs ativos para usuário {}: {}",
        userId,
        e.getMessage()
      );
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/jobs/status/{status}")
  public ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listJobsByStatus(
    @PathVariable ImportJobStatus status,
    PageableRequest pageableRequest
  ) {
    PaginatedResponse<List<CsvImportJob>> jobs =
      csvImportService.findJobsByStatus(status, pageableRequest);
    return getPaginatedResponseResponseEntity(jobs);
  }

  @GetMapping("/jobs/all")
  public ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listAllJobs(
    PageableRequest pageableRequest
  ) {
    PaginatedResponse<List<CsvImportJob>> jobs = csvImportService.findAllJobs(
      pageableRequest
    );
    return getPaginatedResponseResponseEntity(jobs);
  }

  @PostMapping("/jobs/{jobId}/cancel")
  public ResponseEntity<CsvImportResponseDto> cancelJob(
    @PathVariable UUID jobId,
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      concurrentSafeCsvImportService.cancelJob(jobId, userId);

      CsvImportJob job = csvImportService.findJobById(jobId);
      CsvImportResponseDto response = csvImportMapper.toResponseDto(job);

      log.info(
        "Job {} cancelado com controle de concorrência pelo usuário {}",
        jobId,
        userId
      );

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(
        "Erro ao cancelar job {} pelo usuário {}: {}",
        jobId,
        userId,
        e.getMessage()
      );
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/websocket/status")
  public ResponseEntity<Map<String, Object>> getWebSocketStatus(
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      PageableRequest pageableRequest = new PageableRequest(
        0,
        10,
        "createdAt",
        Sort.Direction.DESC
      );
      PaginatedResponse<List<CsvImportJob>> recentJobs =
        csvImportService.findJobsByUser(userId, pageableRequest);

      long activeJobsCount = recentJobs
        .content()
        .stream()
        .filter(job ->
          job.getStatus() == ImportJobStatus.PENDING ||
          job.getStatus() == ImportJobStatus.RUNNING
        )
        .count();

      Map<String, Object> status = Map.of(
        "userId",
        userId,
        "hasActiveJobs",
        activeJobsCount > 0,
        "activeJobsCount",
        activeJobsCount,
        "websocketEndpoint",
        "/ws",
        "websocketEndpointNative",
        "/ws-native",
        "recommendWebSocket",
        activeJobsCount > 0
      );

      return ResponseEntity.ok(status);
    } catch (Exception e) {
      log.error(
        "Erro ao verificar status WebSocket para usuário {}: {}",
        userId,
        e.getMessage()
      );
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/websocket/info")
  public ResponseEntity<Map<String, String>> getWebSocketInfo(
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Map<String, String> info = Map.of(
      "websocketEndpoint",
      "/ws",
      "websocketEndpointNative",
      "/ws-native",
      "authenticationMethod",
      "JWT Token via query parameter (?token=jwt) ou Authorization header",
      "subscriptionPrefix",
      "/app/csv/",
      "userQueuePrefix",
      "/user/" + userId + "/queue/",
      "publicTopicPrefix",
      "/topic/csv-import/",
      "documentation",
      "Consulte docs/websocket-client-example.md para exemplos de uso"
    );

    return ResponseEntity.ok(info);
  }

  @NotNull
  private ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> getPaginatedResponseResponseEntity(
    PaginatedResponse<List<CsvImportJob>> jobs
  ) {
    List<CsvImportResponseDto> dtoList = jobs
      .content()
      .stream()
      .map(csvImportMapper::toResponseDto)
      .toList();

    PaginatedResponse<List<CsvImportResponseDto>> response =
      new PaginatedResponse<>(
        dtoList,
        jobs.page(),
        jobs.size(),
        jobs.totalElements(),
        jobs.totalPages(),
        jobs.last()
      );

    return ResponseEntity.ok(response);
  }

  private Long getCurrentUserId(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      String subClaim = jwt.getClaim("sub");
      if (subClaim != null) {
        try {
          return Long.valueOf(subClaim);
        } catch (NumberFormatException e) {
          log.error("Erro ao converter claim 'sub' para Long: {}", subClaim);
          return null;
        }
      }
    }
    return null;
  }
}
