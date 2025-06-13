/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.mapper;

import org.mapstruct.Named;

public interface GenericMapper<S, D> {
  D map(S source);

  @Named("capitalize")
  default String capitalize(String input) {
    if (input == null || input.isBlank()) {
      return input;
    }
    String lower = input.toLowerCase().trim();
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
  }
}
