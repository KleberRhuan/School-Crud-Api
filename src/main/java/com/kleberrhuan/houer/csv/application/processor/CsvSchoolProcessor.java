/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.processor;

import com.kleberrhuan.houer.csv.application.port.ProcessingStrategy;
import com.kleberrhuan.houer.csv.domain.factory.CsvSchoolRecordFactory;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvSchoolProcessor {

  private final CsvProcessor<CsvSchoolRecord> core;
  private final ProcessingStrategy<CsvSchoolRecord> sequential = s -> s;
  private final ProcessingStrategy<CsvSchoolRecord> parallel = Stream::parallel;

  private final ProcessingStrategy<CsvSchoolRecord> deduplicated = stream ->
    stream.filter(distinctByKey(CsvSchoolRecord::codesc));

  private final ProcessingStrategy<CsvSchoolRecord> deduplicatedParallel =
    stream -> stream.filter(distinctByKey(CsvSchoolRecord::codesc)).parallel();

  public Stream<CsvSchoolRecord> process(
    InputStream inputStream,
    String filename,
    ProcessingStrategy<CsvSchoolRecord> strategy
  ) {
    return strategy.apply(
      core.processWithFactory(
        inputStream,
        filename,
        CsvSchoolRecordFactory::createMapper
      )
    );
  }

  public ProcessingStrategy<CsvSchoolRecord> sequential() {
    return sequential;
  }

  public ProcessingStrategy<CsvSchoolRecord> deduplicated() {
    return deduplicated;
  }

  public ProcessingStrategy<CsvSchoolRecord> deduplicatedParallel() {
    return deduplicatedParallel;
  }

  private static <T> Predicate<T> distinctByKey(
    Function<? super T, ?> keyExtractor
  ) {
    ConcurrentHashMap<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> {
      Object key = keyExtractor.apply(t);
      if (key == null) {
        log.warn("Registro com chave nula encontrado, será ignorado: {}", t);
        return false;
      }
      return seen.putIfAbsent(key, Boolean.TRUE) == null;
    };
  }
}
