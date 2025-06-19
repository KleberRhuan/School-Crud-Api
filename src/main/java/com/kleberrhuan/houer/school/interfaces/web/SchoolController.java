/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.web;

import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.common.interfaces.documentation.controllers.SchoolControllerDocumentation;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.school.application.service.MetricCatalogService;
import com.kleberrhuan.houer.school.application.service.SchoolQueryService;
import com.kleberrhuan.houer.school.application.service.SchoolService;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolFilterSpec;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller REST para consultas de dados de escolas. */
@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Slf4j
public class SchoolController implements SchoolControllerDocumentation {

  private final SchoolQueryService schoolQueryService;
  private final SchoolService schoolService;
  private final MetricCatalogService metricCatalogService;

  @PostMapping
  public ResponseEntity<SchoolDto> createSchool(
    @Valid @RequestBody SchoolCreateRequest request
  ) {
    try {
      SchoolDto createdSchool = schoolService.create(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdSchool);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{code}")
  public ResponseEntity<SchoolDto> updateSchool(
    @PathVariable Long code,
    @Valid @RequestBody SchoolUpdateRequest request
  ) {
    try {
      SchoolDto updatedSchool = schoolService.update(code, request);
      return ResponseEntity.ok(updatedSchool);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{code}")
  public ResponseEntity<SchoolDto> getSchoolByCode(@PathVariable Long code) {
    return schoolQueryService
      .findSchoolByCode(code)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<PaginatedResponse<List<SchoolDto>>> getAllSchools(
    PageableRequest pageableRequest,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String municipalityName,
    @RequestParam(required = false) String stateAbbreviation,
    @RequestParam(required = false) Integer operationalStatus,
    @RequestParam(required = false) Integer dependencyType,
    @RequestParam(required = false) Short schoolType,
    @RequestParam(required = false) String administrativeRegion,
    @RequestParam(required = false) String administrativeDependence,
    @RequestParam(required = false) String location,
    @RequestParam(required = false) String situation
  ) {
    var filter = new SchoolFilterSpec(
      null,
      name,
      municipalityName,
      stateAbbreviation,
      operationalStatus,
      dependencyType,
      schoolType,
      administrativeRegion,
      null,
      administrativeDependence,
      location,
      situation
    );

    PaginatedResponse<List<SchoolDto>> response =
      schoolQueryService.findAllSchoolsWithCache(filter, pageableRequest);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{code}/metrics")
  public ResponseEntity<SchoolMetricsDto> getSchoolMetrics(
    @PathVariable Long code
  ) {
    return schoolQueryService
      .findSchoolMetrics(code)
      .map(ResponseEntity::ok)
      .orElseThrow(() -> new EntityNotFoundException("Escola", code));
  }

  @GetMapping("/metrics")
  public ResponseEntity<Set<String>> getSchoolMetrics() {
    return ResponseEntity.ok(metricCatalogService.getAllValidMetricCodes());
  }

  @DeleteMapping("/{code}")
  public ResponseEntity<Void> deleteSchool(@PathVariable Long code) {
    schoolQueryService.deleteSchool(code);
    return ResponseEntity.noContent().build();
  }
}
