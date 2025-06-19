/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
@Slf4j
@Validated
public class SchoolItemProcessor
  implements ItemProcessor<CsvSchoolRecord, CsvSchoolRecord> {

  private final CsvColumnMetadataService metadataService;

  @Override
  public CsvSchoolRecord process(CsvSchoolRecord item) {
    Set<CsvSchoolColumn> validMetrics = metadataService.getMetricColumns();
    item
      .metrics()
      .keySet()
      .forEach(metricCol -> {
        if (!validMetrics.contains(metricCol)) {
          throw new IllegalArgumentException(
            "Métrica inválida: " + metricCol.name() + " - " + item
          );
        }
      });

    return item;
  }
}
