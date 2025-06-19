/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.model;

public record RowContext(
  String[] headers,
  String[] values,
  int lineNumber,
  String filename
) {
  public RowContext(String[] headers, String[] values, int lineNumber) {
    this(headers, values, lineNumber, "unknown");
  }
}
