/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.web;

import com.kleberrhuan.houer.common.interfaces.documentation.controllers.CsvImportControllerDocumentation;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.csv.application.mapper.CsvImportMapper;
import com.kleberrhuan.houer.csv.application.service.CsvImportService;
import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportRequestDto;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    UUID jobId = csvImportService.startImport(request.file(), userId);

    CsvImportJob job = csvImportService.findJobById(jobId);
    CsvImportResponseDto response = csvImportMapper.toResponseDto(job);

    log.info(
      "Importação CSV iniciada: jobId={}, arquivo={}, usuário={}",
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
    CsvImportJob job = csvImportService.findJobById(jobId);

    Long userId = getCurrentUserId(auth);
    if (!job.getCreatedBy().equals(userId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    CsvImportResponseDto response = csvImportMapper.toResponseDto(job);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/jobs")
  public ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listUserJobs(
    PageableRequest pageableRequest,
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    PaginatedResponse<List<CsvImportJob>> jobs =
      csvImportService.findJobsByUser(userId, pageableRequest);

    return getPaginatedResponseResponseEntity(jobs);
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

  @PostMapping("/jobs/{jobId}/cancel")
  public ResponseEntity<CsvImportResponseDto> cancelJob(
    @PathVariable UUID jobId,
    Authentication auth
  ) {
    Long userId = getCurrentUserId(auth);

    csvImportService.cancelJob(jobId, userId);

    CsvImportJob job = csvImportService.findJobById(jobId);
    CsvImportResponseDto response = csvImportMapper.toResponseDto(job);

    log.info("Job {} cancelado pelo usuário {}", jobId, userId);

    return ResponseEntity.ok(response);
  }

  private Long getCurrentUserId(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      return jwt.getClaim("sub") != null
        ? Long.valueOf(jwt.getClaim("sub").toString())
        : null;
    }
    return null;
  }
}
