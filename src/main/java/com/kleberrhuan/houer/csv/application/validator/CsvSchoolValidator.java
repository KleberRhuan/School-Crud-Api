/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import com.kleberrhuan.houer.csv.application.port.CsvValidator;
import com.kleberrhuan.houer.csv.application.port.ProcessingStrategy;
import com.kleberrhuan.houer.csv.application.processor.CsvSchoolProcessor;
import com.kleberrhuan.houer.csv.domain.exception.CsvValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import com.kleberrhuan.houer.csv.infra.exception.CsvProcessingException;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.io.InputStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvSchoolValidator implements CsvValidator<CsvSchoolRecord> {

  private final CsvSchoolProcessor processor;

  @Override
  @Timed(
    value = "csv.school.validation.time",
    description = "Tempo de validação do CSV de escolas"
  )
  @Counted(
    value = "csv.school.validation.executions",
    description = "Execuções de validação do CSV de escolas"
  )
  public Stream<CsvSchoolRecord> validate(
    InputStream inputStream,
    String filename
  ) {
    try {
      return processor.process(inputStream, filename, processor.deduplicated());
    } catch (CsvValidationException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CsvProcessingException(
        "Erro inesperado na validação do CSV: " + filename,
        e
      );
    }
  }

  @Timed(
    value = "csv.school.validation.parallel.time",
    description = "Tempo de validação paralela do CSV de escolas"
  )
  @Counted(
    value = "csv.school.validation.parallel.executions",
    description = "Execuções de validação paralela do CSV de escolas"
  )
  public Stream<CsvSchoolRecord> validateParallel(
    InputStream inputStream,
    String filename
  ) {
    try {
      return processor.process(
        inputStream,
        filename,
        processor.deduplicatedParallel()
      );
    } catch (CsvValidationException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CsvProcessingException(
        "Erro inesperado na validação paralela do CSV: " + filename,
        e
      );
    }
  }

  @Timed(
    value = "csv.school.validation.raw.time",
    description = "Tempo de validação sem deduplicação do CSV de escolas"
  )
  @Counted(
    value = "csv.school.validation.raw.executions",
    description = "Execuções de validação sem deduplicação do CSV de escolas"
  )
  public Stream<CsvSchoolRecord> validateRaw(
    InputStream inputStream,
    String filename
  ) {
    try {
      return processor.process(inputStream, filename, processor.sequential());
    } catch (CsvValidationException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CsvProcessingException(
        "Erro inesperado na validação raw do CSV: " + filename,
        e
      );
    }
  }

  @Timed(
    value = "csv.school.validation.strategy.time",
    description = "Tempo de validação com estratégia do CSV de escolas"
  )
  @Counted(
    value = "csv.school.validation.strategy.executions",
    description = "Execuções de validação com estratégia do CSV de escolas"
  )
  public Stream<CsvSchoolRecord> validateWithStrategy(
    InputStream inputStream,
    String filename,
    ProcessingStrategy<CsvSchoolRecord> strategy
  ) {
    try {
      return processor.process(inputStream, filename, strategy);
    } catch (CsvValidationException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CsvProcessingException(
        "Erro inesperado na validação com estratégia do CSV: " + filename,
        e
      );
    }
  }
}
