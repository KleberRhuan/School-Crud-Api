/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.service;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CsvColumnMetadataService Tests")
class CsvColumnMetadataServiceTest {

  private CsvColumnMetadataService metadataService;

  @BeforeEach
  void setUp() {
    metadataService = new CsvColumnMetadataService();
  }

  @Test
  @DisplayName("Deve retornar todas as colunas obrigatórias")
  void shouldReturnAllRequiredHeaders() {
    // When
    Set<CsvSchoolColumn> requiredHeaders = metadataService.getRequiredHeaders();

    // Then
    assertThat(requiredHeaders).isNotEmpty();

    // Agora apenas as colunas principais (não métricas) são obrigatórias
    Set<CsvSchoolColumn> nonMetricColumns = Arrays
      .stream(CsvSchoolColumn.values())
      .filter(col -> !col.isMetric())
      .collect(Collectors.toSet());

    assertThat(requiredHeaders).hasSize(nonMetricColumns.size());

    // Verifica algumas colunas principais específicas
    assertThat(requiredHeaders)
      .contains(
        CsvSchoolColumn.NOMEDEP,
        CsvSchoolColumn.DE,
        CsvSchoolColumn.MUN,
        CsvSchoolColumn.DISTR,
        CsvSchoolColumn.CODESC,
        CsvSchoolColumn.NOMESC,
        CsvSchoolColumn.TIPOESC,
        CsvSchoolColumn.TIPOESC_DESC,
        CsvSchoolColumn.CODSIT
      )
      // Não deve conter métricas
      .doesNotContain(
        CsvSchoolColumn.SALAS_AULA,
        CsvSchoolColumn.LAB_INFO,
        CsvSchoolColumn.BIBLIOTECA
      );
  }

  @Test
  @DisplayName("Deve retornar todas as colunas de métricas")
  void shouldReturnAllMetricColumns() {
    // When
    Set<CsvSchoolColumn> metricColumns = metadataService.getMetricColumns();

    // Then
    assertThat(metricColumns).isNotEmpty();

    // Verifica que são apenas colunas métricas
    assertThat(metricColumns).allMatch(CsvSchoolColumn::isMetric);

    // Verifica algumas colunas métricas específicas
    assertThat(metricColumns)
      .contains(
        CsvSchoolColumn.SALAS_AULA,
        CsvSchoolColumn.BIBLIOTECA,
        CsvSchoolColumn.QUADRA_COBERTA,
        CsvSchoolColumn.SALAS_ED_INF,
        CsvSchoolColumn.LAB_INFO
      );

    // Não deve conter colunas principais
    assertThat(metricColumns)
      .doesNotContain(
        CsvSchoolColumn.NOMEDEP,
        CsvSchoolColumn.CODESC,
        CsvSchoolColumn.NOMESC
      );
  }

  @Test
  @DisplayName("Deve encontrar informação de coluna existente")
  void shouldFindExistingColumnInfo() {
    // When & Then
    Optional<CsvSchoolColumn> codesc = metadataService.getColumnInfo("CODESC");
    assertThat(codesc).isPresent();
    assertThat(codesc.get()).isEqualTo(CsvSchoolColumn.CODESC);
    assertThat(codesc.get().isRequired()).isTrue();
    assertThat(codesc.get().isMetric()).isFalse();

    Optional<CsvSchoolColumn> salasAula = metadataService.getColumnInfo(
      "SALAS_AULA"
    );
    assertThat(salasAula).isPresent();
    assertThat(salasAula.get()).isEqualTo(CsvSchoolColumn.SALAS_AULA);
    assertThat(salasAula.get().isRequired()).isTrue();
    assertThat(salasAula.get().isMetric()).isTrue();

    Optional<CsvSchoolColumn> biblioteca = metadataService.getColumnInfo(
      "BIBLIOTECA"
    );
    assertThat(biblioteca).isPresent();
    assertThat(biblioteca.get()).isEqualTo(CsvSchoolColumn.BIBLIOTECA);
    assertThat(biblioteca.get().isMetric()).isTrue();
  }

  @Test
  @DisplayName("Deve retornar contagem correta de colunas obrigatórias")
  void shouldReturnCorrectRequiredColumnsCount() {
    // When
    int totalCount = metadataService.getTotalRequiredColumnsCount();

    // Then
    // Deve ser igual ao tamanho do set de headers obrigatórios
    Set<CsvSchoolColumn> requiredHeaders = metadataService.getRequiredHeaders();
    assertThat(totalCount).isEqualTo(requiredHeaders.size());

    // Verifica que as colunas obrigatórias são apenas as principais (não métricas)
    Set<CsvSchoolColumn> allColumns = Arrays
      .stream(CsvSchoolColumn.values())
      .collect(Collectors.toSet());
    Set<CsvSchoolColumn> nonMetricColumns = allColumns
      .stream()
      .filter(col -> !col.isMetric())
      .collect(Collectors.toSet());

    assertThat(totalCount).isEqualTo(nonMetricColumns.size());
    assertThat(requiredHeaders).isEqualTo(nonMetricColumns);
  }

  @Test
  @DisplayName("Deve garantir que métricas não são obrigatórias")
  void shouldEnsureMetricsAreNotRequired() {
    // When
    Set<CsvSchoolColumn> requiredHeaders = metadataService.getRequiredHeaders();
    Set<CsvSchoolColumn> metricColumns = metadataService.getMetricColumns();

    // Then
    // Nenhuma métrica deve estar nas obrigatórias
    assertThat(requiredHeaders).doesNotContainAnyElementsOf(metricColumns);

    // As obrigatórias devem ser apenas as colunas principais
    Set<CsvSchoolColumn> nonMetricColumns = Arrays
      .stream(CsvSchoolColumn.values())
      .filter(col -> !col.isMetric())
      .collect(Collectors.toSet());
    assertThat(requiredHeaders).isEqualTo(nonMetricColumns);
  }

  @Test
  @DisplayName("Deve validar propriedades específicas de colunas conhecidas")
  void shouldValidateSpecificColumnProperties() {
    // Given & When & Then

    // CODESC - Principal, obrigatória, não métrica, não numérica
    Optional<CsvSchoolColumn> codesc = metadataService.getColumnInfo("CODESC");
    assertThat(codesc).isPresent();
    assertThat(codesc.get().isRequired()).isTrue();
    assertThat(codesc.get().isMetric()).isFalse();
    assertThat(codesc.get().isNumeric()).isFalse();

    // TIPOESC - Principal, obrigatória, não métrica, numérica
    Optional<CsvSchoolColumn> tipoesc = metadataService.getColumnInfo(
      "TIPOESC"
    );
    assertThat(tipoesc).isPresent();
    assertThat(tipoesc.get().isRequired()).isTrue();
    assertThat(tipoesc.get().isMetric()).isFalse();
    assertThat(tipoesc.get().isNumeric()).isTrue();

    // SALAS_AULA - Métrica, obrigatória, numérica
    Optional<CsvSchoolColumn> salasAula = metadataService.getColumnInfo(
      "SALAS_AULA"
    );
    assertThat(salasAula).isPresent();
    assertThat(salasAula.get().isRequired()).isTrue();
    assertThat(salasAula.get().isMetric()).isTrue();
    assertThat(salasAula.get().isNumeric()).isTrue();

    // NOMESC - Principal, obrigatória, não métrica, não numérica
    Optional<CsvSchoolColumn> nomesc = metadataService.getColumnInfo("NOMESC");
    assertThat(nomesc).isPresent();
    assertThat(nomesc.get().isRequired()).isTrue();
    assertThat(nomesc.get().isMetric()).isFalse();
    assertThat(nomesc.get().isNumeric()).isFalse();
  }

  @Test
  @DisplayName("Deve ser case-sensitive para nomes de colunas")
  void shouldBeCaseSensitiveForColumnNames() {
    // When & Then
    Optional<CsvSchoolColumn> upperCase = metadataService.getColumnInfo(
      "CODESC"
    );
    assertThat(upperCase).isPresent();

    Optional<CsvSchoolColumn> lowerCase = metadataService.getColumnInfo(
      "codesc"
    );
    assertThat(lowerCase).isEmpty();

    Optional<CsvSchoolColumn> mixedCase = metadataService.getColumnInfo(
      "Codesc"
    );
    assertThat(mixedCase).isEmpty();
  }

  @Test
  @DisplayName("Deve validar todas as colunas do enum estão no lookup")
  void shouldValidateAllEnumColumnsAreInLookup() {
    // When & Then
    for (CsvSchoolColumn column : CsvSchoolColumn.values()) {
      Optional<CsvSchoolColumn> found = metadataService.getColumnInfo(
        column.name()
      );
      assertThat(found).isPresent();
      assertThat(found.get()).isEqualTo(column);
    }
  }

  @Test
  @DisplayName("Deve garantir immutabilidade dos sets retornados")
  void shouldEnsureReturnedSetsAreImmutable() {
    // When
    Set<CsvSchoolColumn> requiredHeaders = metadataService.getRequiredHeaders();
    Set<CsvSchoolColumn> metricColumns = metadataService.getMetricColumns();

    // Then - Tentar modificar deve lançar exceção
    assertThatThrownBy(() -> requiredHeaders.add(CsvSchoolColumn.SALAS_AULA))
      .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(() -> metricColumns.add(CsvSchoolColumn.CODESC))
      .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(() -> requiredHeaders.remove(CsvSchoolColumn.CODESC))
      .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(() -> metricColumns.clear())
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Deve retornar Optional vazio para coluna inexistente")
  void shouldReturnEmptyOptionalForNonExistentColumn() {
    // When & Then
    Optional<CsvSchoolColumn> nonExistent = metadataService.getColumnInfo(
      "COLUNA_INEXISTENTE"
    );
    assertThat(nonExistent).isEmpty();

    Optional<CsvSchoolColumn> empty = metadataService.getColumnInfo("");
    assertThat(empty).isEmpty();

    Optional<CsvSchoolColumn> nullColumn = metadataService.getColumnInfo(null);
    assertThat(nullColumn).isEmpty();
  }
}
