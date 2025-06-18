/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.processor;

import java.util.stream.Stream;

public interface ProcessingStrategy<T> {
  Stream<T> apply(Stream<T> source);

  static <T> ProcessingStrategy<T> sequential() {
    return source -> source;
  }

  static <T> ProcessingStrategy<T> parallel() {
    return Stream::parallel;
  }
}
