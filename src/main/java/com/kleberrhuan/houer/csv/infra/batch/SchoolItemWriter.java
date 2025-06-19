/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import com.kleberrhuan.houer.csv.infra.exception.CsvProcessingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class SchoolItemWriter implements ItemWriter<CsvSchoolRecord> {

  private final EntityManager entityManager;
  private final ObjectMapper objectMapper;

  @Value("#{jobParameters['userId']}")
  private Long userId;

  @Override
  @Transactional
  public void write(Chunk<? extends CsvSchoolRecord> chunk) {
    var items = chunk.getItems();

    log.info("Iniciando escrita de {} itens", items.size());

    for (CsvSchoolRecord item : items) {
      try {
        writeSchoolAndMetricsWithUpsert(item);
      } catch (Exception e) {
        log.error(
          "Falha ao processar item {}: {}",
          item.codesc(),
          e.getMessage(),
          e
        );
        throw new CsvProcessingException(
          "Falha ao processar item " + item.codesc() + ": " + e.getMessage()
        );
      }
    }

    log.info("Finalizou escrita de {} itens", items.size());
  }

  private void writeSchoolAndMetricsWithUpsert(CsvSchoolRecord item) {
    Long schoolCode;
    try {
      schoolCode = Long.parseLong(item.codesc());
    } catch (NumberFormatException e) {
      log.warn("Código da escola inválido: {}", item.codesc());
      return;
    }

    // UPSERT para tabela school
    String schoolUpsertSql =
      """
        INSERT INTO school.school (code, nome_esc, nome_dep, de, mun, distr, tipo_esc, tipo_esc_desc, codsit, codesc, created_at, updated_at, created_by, updated_by)
        VALUES (:code, :nomeEsc, :nomeDep, :de, :mun, :distr, :tipoEsc, :tipoEscDesc, :codsit, :codesc, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :userId, :userId)
        ON CONFLICT (code)
        DO UPDATE SET
            nome_esc = EXCLUDED.nome_esc,
            nome_dep = EXCLUDED.nome_dep,
            de = EXCLUDED.de,
            mun = EXCLUDED.mun,
            distr = EXCLUDED.distr,
            tipo_esc = EXCLUDED.tipo_esc,
            tipo_esc_desc = EXCLUDED.tipo_esc_desc,
            codsit = EXCLUDED.codsit,
            codesc = EXCLUDED.codesc,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = :userId
        """;

    Query schoolQuery = entityManager.createNativeQuery(schoolUpsertSql);
    schoolQuery.setParameter("code", schoolCode);
    schoolQuery.setParameter("nomeEsc", item.nomesc());
    schoolQuery.setParameter("nomeDep", item.nomeDep());
    schoolQuery.setParameter("de", item.de());
    schoolQuery.setParameter("mun", item.mun());
    schoolQuery.setParameter("distr", item.distr());
    schoolQuery.setParameter(
      "tipoEsc",
      item.tipoesc() != null ? item.tipoesc().shortValue() : null
    );
    schoolQuery.setParameter("tipoEscDesc", item.tipoescDesc());
    schoolQuery.setParameter("codesc", schoolCode);
    schoolQuery.setParameter("codsit", item.codSit());
    schoolQuery.setParameter("userId", userId);

    int schoolRowsAffected = schoolQuery.executeUpdate();
    log.debug(
      "School upsert para {}: {} linhas afetadas",
      schoolCode,
      schoolRowsAffected
    );

    Map<String, Long> metrics = item
      .metrics()
      .entrySet()
      .stream()
      .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

    if (!metrics.isEmpty()) {
      String metricsUpsertSql =
        """
          INSERT INTO school.school_metrics_jsonb (school_code, metrics, created_at, updated_at, created_by, updated_by)
          VALUES (:schoolCode, CAST(:metrics AS jsonb), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :userId, :userId)
          ON CONFLICT (school_code)
          DO UPDATE SET
              metrics = COALESCE(school.school_metrics_jsonb.metrics, '{}'::jsonb) || EXCLUDED.metrics,
              updated_at = CURRENT_TIMESTAMP,
              updated_by = :userId
          """;

      Query metricsQuery = entityManager.createNativeQuery(metricsUpsertSql);
      metricsQuery.setParameter("schoolCode", schoolCode);
      metricsQuery.setParameter("userId", userId);

      String metricsJson = convertMapToJson(metrics);
      metricsQuery.setParameter("metrics", metricsJson);

      int metricsRowsAffected = metricsQuery.executeUpdate();
      log.debug(
        "Metrics upsert para escola {}: {} linhas afetadas, {} métricas",
        schoolCode,
        metricsRowsAffected,
        metrics.size()
      );
    }
  }

  private String convertMapToJson(Map<String, Long> metrics) {
    try {
      return objectMapper.writeValueAsString(metrics);
    } catch (JsonProcessingException e) {
      log.error("Erro ao converter métricas para JSON: {}", e.getMessage());
      throw new CsvProcessingException("Erro ao processar métricas JSON");
    }
  }
}
