/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.error;

import com.kleberrhuan.houer.common.domain.exception.constraint.ConstraintType;

public record ViolationInfo(
  ConstraintType type,
  String column,
  String value,
  String constraint
) {
  public static ViolationInfo unique(String col, String val) {
    return new ViolationInfo(ConstraintType.UNIQUE, col, val, null);
  }

  public static ViolationInfo notNull(String col) {
    return new ViolationInfo(ConstraintType.NOT_NULL, col, null, null);
  }

  public static ViolationInfo foreignKey(String constraint) {
    return new ViolationInfo(
      ConstraintType.FOREIGN_KEY,
      null,
      null,
      constraint
    );
  }

  public static ViolationInfo check(String constraint) {
    return new ViolationInfo(ConstraintType.CHECK, null, null, constraint);
  }

  public static ViolationInfo exclusion(String constraint) {
    return new ViolationInfo(ConstraintType.EXCLUSION, null, null, constraint);
  }
}
