/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.port;

@FunctionalInterface
public interface CsvRecordMapper<T> {
  T map(String[] headers, String[] values);
}
