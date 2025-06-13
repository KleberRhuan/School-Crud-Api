/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.factory;

import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PageableFactory {

  public Pageable create(@NonNull PageableRequest request) {
    return PageRequest.of(
      request.page() - 1,
      request.size(),
      Sort.by(request.direction(), request.sort())
    );
  }
}
