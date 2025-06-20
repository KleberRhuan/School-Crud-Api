/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.service;

import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.school.application.mapper.SchoolMapper;
import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.domain.repository.SchoolRepository;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolService {

  private final SchoolRepository repo;
  private final SchoolMapper mapper;

  @Transactional
  public SchoolDto create(SchoolCreateRequest req) {
    School school = mapper.toEntity(req);
    school.setSchoolMetrics(req.metrics());
    School saved = repo.save(school);
    return mapper.toDto(saved);
  }

  @Transactional
  @CacheEvict(
          cacheNames = {"schoolsPage", "metricCatalog"},
          allEntries = true
  )
  public SchoolDto update(Long code, SchoolUpdateRequest req) {
    School school = repo
            .findWithMetricsByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Escola", code));

    mapper.updateEntity(school, req);
    school.setSchoolMetrics(req.metrics());

    School updated = repo.save(school);
    return mapper.toDto(updated);
  }
}
