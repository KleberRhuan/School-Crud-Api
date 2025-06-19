/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.model;

import lombok.Getter;

/** Tipo de dados suportados pelas métricas das escolas. */
@Getter
public enum DataType {
  INT("Inteiro"),
  STRING("Texto"),
  BOOLEAN("Booleano"),
  DECIMAL("Decimal");

  private final String description;

  DataType(String description) {
    this.description = description;
  }

  public static DataType fromString(String type) {
    if (type == null || type.trim().isEmpty()) {
      throw new IllegalArgumentException(
        "Tipo de dados não pode ser nulo ou vazio"
      );
    }

    try {
      return DataType.valueOf(type.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
        "Tipo de dados não suportado: " + type
      );
    }
  }

  public boolean isValidValue(String value) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }

    return switch (this) {
      case INT -> {
        try {
          Long.parseLong(value.trim());
          yield true;
        } catch (NumberFormatException e) {
          yield false;
        }
      }
      case DECIMAL -> {
        try {
          Double.parseDouble(value.trim());
          yield true;
        } catch (NumberFormatException e) {
          yield false;
        }
      }
      case BOOLEAN -> {
        String normalized = value.trim().toLowerCase();
        yield "true".equals(normalized) ||
        "false".equals(normalized) ||
        "1".equals(normalized) ||
        "0".equals(normalized);
      }
      case STRING -> true;
    };
  }
}
