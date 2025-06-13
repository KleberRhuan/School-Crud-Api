/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.response;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;

public record PaginatedResponse<T extends Collection<?>>(
  T content,
  int page,
  int size,
  long totalElements,
  int totalPages,
  boolean last
) {
  public static <E> PaginatedResponse<List<E>> of(Page<E> page) {
    return new PaginatedResponse<>(
      page.getContent(),
      page.getNumber() + 1,
      page.getSize(),
      page.getTotalElements(),
      page.getTotalPages(),
      page.isLast()
    );
  }
}
