/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.factory;

import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public final class CsvSchoolRecordFactory {

  private static final Pattern DIGITS = Pattern.compile("\\d+");
  private static final char BOM = '\uFEFF';

  private final Map<CsvSchoolColumn, Integer> indexMap;
  private final List<Map.Entry<CsvSchoolColumn, Integer>> metricPositions;

  private CsvSchoolRecordFactory(String[] headers) {
    this.indexMap = createIndexMap(headers);
    this.metricPositions =
      indexMap.entrySet().stream().filter(e -> e.getKey().isMetric()).toList();
  }

  public static Function<String[], CsvSchoolRecord> createMapper(
    String[] headers
  ) {
    CsvSchoolRecordFactory factory = new CsvSchoolRecordFactory(headers);
    return factory::createRecord;
  }

  private CsvSchoolRecord createRecord(String[] values) {
    String nomeDep = stringAt(values, CsvSchoolColumn.NOMEDEP);
    String de = stringAt(values, CsvSchoolColumn.DE);
    String mun = stringAt(values, CsvSchoolColumn.MUN);
    String distr = stringAt(values, CsvSchoolColumn.DISTR);
    String codesc = stringAt(values, CsvSchoolColumn.CODESC);
    String nomesc = stringAt(values, CsvSchoolColumn.NOMESC);
    Long tipoesc = longAt(values, CsvSchoolColumn.TIPOESC);
    String tipoescDesc = stringAt(values, CsvSchoolColumn.TIPOESC_DESC);
    Short codSit = shortAt(values, CsvSchoolColumn.CODSIT);

    EnumMap<CsvSchoolColumn, Long> metrics = new EnumMap<>(
      CsvSchoolColumn.class
    );
    for (var entry : metricPositions) {
      int pos = entry.getValue();
      if (pos < values.length) {
        String raw = values[pos];
        if (raw != null && !raw.isBlank() && DIGITS.matcher(raw).matches()) {
          metrics.put(entry.getKey(), Long.parseLong(raw));
        } else {
          metrics.put(entry.getKey(), 0L);
        }
      }
    }

    return new CsvSchoolRecord(
      nomeDep,
      de,
      mun,
      distr,
      codesc,
      nomesc,
      tipoesc,
      tipoescDesc,
      codSit,
      metrics
    );
  }

  private Short shortAt(String[] values, CsvSchoolColumn csvSchoolColumn) {
    String s = stringAt(values, csvSchoolColumn);
    return (s != null && DIGITS.matcher(s).matches())
      ? Short.parseShort(s)
      : null;
  }

  private String stringAt(String[] values, CsvSchoolColumn col) {
    Integer pos = indexMap.get(col);
    return (pos != null && pos < values.length) ? values[pos] : null;
  }

  private Long longAt(String[] values, CsvSchoolColumn col) {
    String s = stringAt(values, col);
    return (s != null && DIGITS.matcher(s).matches())
      ? Long.parseLong(s)
      : null;
  }

  private static Map<CsvSchoolColumn, Integer> createIndexMap(
    String[] headers
  ) {
    return IntStream
      .range(0, headers.length)
      .boxed()
      .collect(
        java.util.stream.Collectors.toMap(
          i -> CsvSchoolColumn.valueOf(cleanColumnName(headers[i])),
          i -> i,
          (a, b) -> a,
          () -> new EnumMap<>(CsvSchoolColumn.class)
        )
      );
  }

  private static String cleanColumnName(String columnName) {
    if (columnName == null) {
      return null;
    }

    String cleaned = columnName.trim();

    if (!cleaned.isEmpty() && cleaned.charAt(0) == BOM) {
      cleaned = cleaned.substring(1).trim();
    }

    return cleaned;
  }
}
