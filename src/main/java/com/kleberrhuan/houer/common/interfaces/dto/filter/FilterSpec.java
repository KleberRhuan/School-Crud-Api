/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.filter;

import java.util.Map;

public interface FilterSpec {
  Boolean onlyActive();

  Map<String, String> params();

  default boolean hasFilters() {
    return (params() != null && !params().isEmpty());
  }
}
