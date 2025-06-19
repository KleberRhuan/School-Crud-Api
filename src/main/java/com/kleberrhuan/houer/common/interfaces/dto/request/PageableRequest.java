/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Sort;

public record PageableRequest(
  @PositiveOrZero Integer page,
  @PositiveOrZero Integer size,
  String sort,
  Sort.Direction direction
) {
  public PageableRequest {
    if (page == null) page = 0;
    if (size == null) size = 10;
    if (sort == null || sort.isBlank()) sort = "code";
    if (direction == null) direction = Sort.Direction.ASC;
  }
}
