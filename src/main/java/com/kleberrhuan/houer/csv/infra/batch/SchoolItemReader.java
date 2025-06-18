/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch;

import com.kleberrhuan.houer.csv.domain.factory.CsvSchoolRecordFactory;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import java.net.URI;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SchoolItemReader {

  @Bean
  @StepScope
  public FlatFileItemReader<CsvSchoolRecord> csvSchoolReader(
      @Value("#{jobParameters['fileUri']}") @NonNull String fileUriString) throws Exception {
    URI fileUri = URI.create(fileUriString);
    Resource csvResource = new UrlResource(fileUri);

    String[] headers;
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8))) {
      String headerLine = br.readLine();
      if (headerLine == null) {
        throw new IllegalStateException("Arquivo CSV vazio");
      }
      headers = headerLine.split(";");
    }

    log.info(
        "Configurando reader para arquivo: {} (URI: {}) - {} colunas",
        csvResource,
        fileUri,
        headers.length);

    return new FlatFileItemReaderBuilder<CsvSchoolRecord>()
        .name("csvSchoolReader")
        .resource(csvResource)
        .linesToSkip(1)
        .lineMapper(createLineMapper(headers))
        .saveState(true)
        .maxItemCount(Integer.MAX_VALUE)
        .currentItemCount(0)
        .strict(true)
        .build();
  }

  private LineMapper<CsvSchoolRecord> createLineMapper(String[] headers) {
    DefaultLineMapper<CsvSchoolRecord> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(createTokenizer());
    lineMapper.setFieldSetMapper(createFieldSetMapper(headers));
    return lineMapper;
  }

  private DelimitedLineTokenizer createTokenizer() {
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(";");
    tokenizer.setStrict(false);
    return tokenizer;
  }

  private FieldSetMapper<CsvSchoolRecord> createFieldSetMapper(String[] headers) {
    return fieldSet -> {
      String[] values = new String[fieldSet.getFieldCount()];
      for (int i = 0; i < fieldSet.getFieldCount(); i++) {
        values[i] = fieldSet.readString(i);
      }
      Function<String[], CsvSchoolRecord> mapper = CsvSchoolRecordFactory.createMapper(headers);
      return mapper.apply(values);
    };
  }
}
