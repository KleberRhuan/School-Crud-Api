/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.parser;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsvParser {

  public Stream<String[]> read(InputStream inputStream)
    throws IOException, CsvException {
    try (CSVReader reader = createCsvReader(inputStream)) {
      log.debug("Iniciando leitura do CSV com encoding UTF-8 e separador ';'");
      return reader.readAll().stream();
    }
  }

  public Iterator<String[]> readAsIterator(InputStream inputStream)
    throws IOException {
    CSVReader reader = createCsvReader(inputStream);

    log.debug("Iniciando leitura streaming do CSV com separador ';'");

    Iterator<String[]> originalIterator = reader.iterator();

    return new Iterator<>() {
      private boolean finished = false;

      @Override
      public boolean hasNext() {
        boolean hasNext = originalIterator.hasNext();
        if (!hasNext && !finished) {
          finished = true;
          try {
            reader.close();
            log.debug("CSVReader fechado após leitura completa");
          } catch (IOException e) {
            log.warn("Erro ao fechar CSVReader: {}", e.getMessage());
          }
        }
        return hasNext;
      }

      @Override
      public String[] next() {
        return originalIterator.next();
      }
    };
  }

  private CSVReader createCsvReader(InputStream inputStream) {
    CSVParser parser = new CSVParserBuilder().withSeparator(';').build();

    return new CSVReaderBuilder(
      new InputStreamReader(inputStream, StandardCharsets.UTF_8)
    )
      .withCSVParser(parser)
      .withSkipLines(0)
      .build();
  }
}
