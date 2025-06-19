/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.common.application.factory.PageableFactory;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableFactoryTest {

  private final PageableFactory factory = new PageableFactory();

  @Test
  @DisplayName("deve converter page 0 ou negativo para página 0")
  void shouldConvertZeroOrNegativePageToZero() {
    PageableRequest req = new PageableRequest(0, 10, "id", Sort.Direction.ASC);

    Pageable p = factory.create(req);

    assertThat(p.getPageNumber()).isEqualTo(0);
    assertThat(p.getPageSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("deve criar Pageable com ordenação correta e valores válidos")
  void shouldCreateValidPageable() {
    PageableRequest req = new PageableRequest(
      2,
      25,
      "name",
      Sort.Direction.DESC
    );

    Pageable p = factory.create(req);

    assertThat(p.getPageNumber()).isEqualTo(1); // page 2 -> index 1
    assertThat(p.getPageSize()).isEqualTo(25);
    assertThat(p.getSort()).containsExactly(Sort.Order.desc("name"));
  }
}
