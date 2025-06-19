/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.service;

import com.kleberrhuan.houer.common.application.factory.PageableFactory;
import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.school.application.mapper.SchoolMapper;
import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.domain.repository.SchoolMetricsRepository;
import com.kleberrhuan.houer.school.domain.repository.SchoolRepository;
import com.kleberrhuan.houer.school.infra.specification.SchoolSpecificationFactory;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolFilterSpec;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Serviço para consultas e filtros de dados de escolas. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolQueryService {

  private final SchoolRepository schoolRepository;
  private final SchoolMetricsRepository metricsRepository;
  private final SchoolSpecificationFactory specificationFactory;
  private final SchoolMapper schoolMapper;
  private final PageableFactory pageableFactory;

  @Transactional(readOnly = true)
  @Cacheable(
    cacheNames = "schoolsPage",
    key = "#filter.hashCode() + ':' + #pageable.page + ':' + #pageable.size",
    unless = "#result.content.isEmpty()"
  )
  public PaginatedResponse<List<SchoolDto>> findAllSchoolsWithCache(
    SchoolFilterSpec filter,
    PageableRequest pageable
  ) {
    log.debug(
      "Buscando escolas com filtros: {} e paginação: {}",
      filter,
      pageable
    );

    Specification<School> spec = specificationFactory.byFilter(filter);
    var p = pageableFactory.createForSchool(pageable);

    Page<School> schoolPage = schoolRepository.findAll(spec, p.pageable());
    Page<SchoolDto> schoolDtoPage = schoolPage.map(schoolMapper::toDto);

    log.debug(
      "Encontradas {} escolas na página {}",
      schoolDtoPage.getContent().size(),
      schoolDtoPage.getNumber()
    );

    return PaginatedResponse.of(schoolDtoPage);
  }

  @Transactional(readOnly = true)
  public Optional<SchoolDto> findSchoolByCode(Long code) {
    return schoolRepository.findById(code).map(schoolMapper::toDto);
  }

  @Transactional(readOnly = true)
  public Optional<SchoolMetricsDto> findSchoolMetrics(Long schoolCode) {
    return metricsRepository
      .findById(schoolCode)
      .map(schoolMapper::metricsToDto);
  }

  @Transactional
  public void deleteSchool(Long code) {
    School school = schoolRepository
      .findById(code)
      .orElseThrow(() -> new EntityNotFoundException("Escola", code));
    schoolRepository.delete(school);
  }
}
