/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.specification;

import com.kleberrhuan.houer.common.interfaces.dto.filter.FilterSpec;
import org.springframework.data.jpa.domain.Specification;

@FunctionalInterface
public interface SpecificationFactory<E, F extends FilterSpec> {
  Specification<E> byFilter(F filter, Long currentUserId);
}
