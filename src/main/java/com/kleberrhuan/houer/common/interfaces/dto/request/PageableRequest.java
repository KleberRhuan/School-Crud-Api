/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.springframework.data.domain.Sort;

@Builder
public record PageableRequest(
  @Positive Integer page,
  @PositiveOrZero Integer size,
  String sort,
  Sort.Direction direction
) {
  public PageableRequest {
    if (page == null) page = 1;
    if (size == null) size = 10;
    if (sort == null || sort.isBlank()) sort = "id";
    if (direction == null) direction = Sort.Direction.ASC;
  }
}
