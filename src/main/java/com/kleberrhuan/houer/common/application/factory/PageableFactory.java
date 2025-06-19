/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.factory;

import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.school.infra.util.SchoolFieldUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageableFactory {

  public Pageable create(@NonNull PageableRequest request) {
    int zeroPage = request.page() != null && request.page() > 0
      ? request.page() - 1
      : 0;
    return PageRequest.of(
      zeroPage,
      request.size(),
      Sort.by(request.direction(), request.sort())
    );
  }

  public record PageableWithMetric(
    Pageable pageable,
    String metric,
    boolean desc
  ) {}

  public PageableWithMetric createForSchool(@NonNull PageableRequest request) {
    int page = request.page() != null && request.page() > 0
      ? request.page() - 1
      : 0;
    int size = request.size();

    String sortProp = request.sort();
    boolean desc = request.direction() == Sort.Direction.DESC;

    Pageable pageable;

    if (SchoolFieldUtils.isSchoolField(sortProp)) {
      pageable =
        PageRequest.of(page, size, Sort.by(request.direction(), sortProp));
      return new PageableWithMetric(pageable, null, false);
    }

    String expr = "CAST(metrics->>'" + sortProp + "' AS BIGINT)";
    Sort metricSort = JpaSort.unsafe(request.direction(), expr);
    pageable = PageRequest.of(page, size, metricSort);
    return new PageableWithMetric(pageable, sortProp, desc);
  }
}
