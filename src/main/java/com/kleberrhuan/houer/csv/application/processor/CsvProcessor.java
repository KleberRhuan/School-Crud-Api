/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.processor;

import com.kleberrhuan.houer.csv.application.parser.CsvParser;
import com.kleberrhuan.houer.csv.application.port.CsvRecordMapper;
import com.kleberrhuan.houer.csv.application.validator.CompositeRowValidator;
import com.kleberrhuan.houer.csv.application.validator.HeaderValidator;
import com.kleberrhuan.houer.csv.domain.exception.CsvValidationException;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import com.kleberrhuan.houer.csv.infra.exception.CsvProcessingException;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvProcessor<T> {

  private final CsvParser parser;
  private final HeaderValidator headerValidator;
  private final CompositeRowValidator rowValidator;

  @Timed(
    value = "csv.processing.time",
    description = "Tempo de processamento CSV"
  )
  @Counted(
    value = "csv.processing.executions",
    description = "Número de execuções de processamento CSV"
  )
  public Stream<T> process(
    InputStream inputStream,
    String filename,
    Function<String[], T> recordMapper
  ) {
    return processInternal(inputStream, filename, headers -> recordMapper);
  }

  @Timed(
    value = "csv.processing.headers.time",
    description = "Tempo de processamento CSV com headers"
  )
  @Counted(
    value = "csv.processing.headers.executions",
    description = "Execuções de processamento CSV com headers"
  )
  public Stream<T> processWithHeaders(
    InputStream inputStream,
    String filename,
    CsvRecordMapper<T> recordMapper
  ) {
    return processInternal(
      inputStream,
      filename,
      headers -> values -> recordMapper.map(headers, values)
    );
  }

  @Timed(
    value = "csv.processing.factory.time",
    description = "Tempo de processamento CSV com factory"
  )
  @Counted(
    value = "csv.processing.factory.executions",
    description = "Execuções de processamento CSV com factory"
  )
  public Stream<T> processWithFactory(
    InputStream inputStream,
    String filename,
    Function<String[], Function<String[], T>> factoryCreator
  ) {
    return processInternal(inputStream, filename, factoryCreator);
  }

  private Stream<T> processInternal(
    InputStream inputStream,
    String filename,
    Function<String[], Function<String[], T>> mapperFactory
  ) {
    AtomicInteger processedRows = new AtomicInteger();
    AtomicInteger errorRows = new AtomicInteger();

    try {
      log
        .atInfo()
        .addKeyValue("filename", filename)
        .log("Iniciando processamento de CSV");

      Iterator<String[]> csvIterator = parser.readAsIterator(inputStream);

      if (!csvIterator.hasNext()) {
        throw new CsvValidationException("Arquivo CSV vazio ou inválido");
      }

      String[] headers = csvIterator.next();
      headerValidator.validate(headers, filename);

      Function<String[], T> recordMapper = mapperFactory.apply(headers);
      Iterator<T> iterator = createValidatedIterator(
        headers,
        csvIterator,
        recordMapper,
        processedRows,
        errorRows,
        filename
      );

      return StreamSupport
        .stream(
          Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
          false
        )
        .onClose(() ->
          log.info(
            "Processamento concluído: {} linhas processadas, {} erros",
            processedRows.get(),
            errorRows.get()
          )
        );
    } catch (IOException e) {
      throw new UncheckedIOException("Erro ao ler arquivo CSV: " + filename, e);
    } catch (CsvValidationException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new CsvProcessingException(
        "Erro inesperado no processamento do CSV: " + filename,
        e
      );
    }
  }

  private Iterator<T> createValidatedIterator(
    String[] headers,
    Iterator<String[]> csvIterator,
    Function<String[], T> recordMapper,
    AtomicInteger processedRows,
    AtomicInteger errorRows,
    String filename
  ) {
    return new Iterator<>() {
      private int lineNumber = 1;

      @Override
      public boolean hasNext() {
        return csvIterator.hasNext();
      }

      @Override
      @Counted(
        value = "csv.rows.processed",
        description = "Linhas processadas do CSV"
      )
      public T next() {
        String[] values = csvIterator.next();
        lineNumber++;

        try {
          RowContext context = new RowContext(
            headers,
            values,
            lineNumber,
            filename
          );
          rowValidator.validate(context);

          T mapped = recordMapper.apply(values);
          processedRows.incrementAndGet();
          return mapped;
        } catch (CsvValidationException e) {
          errorRows.incrementAndGet();
          throw e;
        } catch (RuntimeException e) {
          errorRows.incrementAndGet();
          throw new CsvProcessingException(
            "Erro inesperado ao processar linha " + lineNumber,
            e
          );
        }
      }
    };
  }
}
