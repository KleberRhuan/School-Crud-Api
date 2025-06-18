/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface CsvValidator<T> {
  Stream<T> validate(InputStream inputStream, String filename);

  default List<T> validateToList(InputStream inputStream, String filename) {
    return validate(inputStream, filename).toList();
  }
}
