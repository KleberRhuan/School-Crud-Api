/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator.rule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.domain.exception.AggregatedValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MandatoryRule Tests")
class MandatoryRuleTest {

  @Mock
  private CsvColumnMetadataService metadataService;

  @InjectMocks
  private MandatoryRule mandatoryRule;

  @BeforeEach
  void setUp() {
    // Setup padrão para colunas conhecidas com lenient para evitar stubbings
    // desnecessários
    lenient()
      .when(metadataService.getColumnInfo("CODESC"))
      .thenReturn(Optional.of(CsvSchoolColumn.CODESC));
    lenient()
      .when(metadataService.getColumnInfo("NOMESC"))
      .thenReturn(Optional.of(CsvSchoolColumn.NOMESC));
    lenient()
      .when(metadataService.getColumnInfo("NOMEDEP"))
      .thenReturn(Optional.of(CsvSchoolColumn.NOMEDEP));
    lenient()
      .when(metadataService.getColumnInfo("SALAS_AULA"))
      .thenReturn(Optional.of(CsvSchoolColumn.SALAS_AULA));
    lenient()
      .when(metadataService.getColumnInfo("COLUNA_INEXISTENTE"))
      .thenReturn(Optional.empty());
  }

  @Test
  @DisplayName(
    "Deve validar linha com todos os campos obrigatórios preenchidos"
  )
  void shouldValidateRowWithAllMandatoryFieldsFilled() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "12345", "Escola Teste", "Departamento Teste" };
    RowContext context = new RowContext(headers, values, 1);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar linha com campo obrigatório vazio")
  void shouldRejectRowWithEmptyMandatoryField() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "", "Escola Teste", "Departamento Teste" };
    RowContext context = new RowContext(headers, values, 2);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 2")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName("Deve rejeitar linha com campo obrigatório null")
  void shouldRejectRowWithNullMandatoryField() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { null, "Escola Teste", "Departamento Teste" };
    RowContext context = new RowContext(headers, values, 3);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 3")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName("Deve rejeitar linha com campo obrigatório só com espaços")
  void shouldRejectRowWithWhitespaceOnlyMandatoryField() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "   ", "Escola Teste", "Departamento Teste" };
    RowContext context = new RowContext(headers, values, 4);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 4")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName(
    "Deve aceitar valores com espaços no início e fim se não estiverem vazios"
  )
  void shouldAcceptValuesWithLeadingTrailingSpacesIfNotEmpty() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "  12345  ", "  Escola Teste  ", "  Departamento  " };
    RowContext context = new RowContext(headers, values, 5);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve agregar múltiplos erros de campos obrigatórios")
  void shouldAggregateMultipleMandatoryFieldErrors() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "", "", "Departamento Teste" };
    RowContext context = new RowContext(headers, values, 6);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 6")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("NOMESC")
      .hasMessageContaining("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName("Deve ignorar colunas não obrigatórias vazias")
  void shouldIgnoreNonMandatoryEmptyColumns() {
    // Given
    // Mock para coluna não obrigatória
    CsvSchoolColumn nonMandatoryColumn = mock(CsvSchoolColumn.class);
    when(nonMandatoryColumn.isRequired()).thenReturn(false);
    when(metadataService.getColumnInfo("COLUNA_OPCIONAL"))
      .thenReturn(Optional.of(nonMandatoryColumn));

    String[] headers = { "CODESC", "NOMESC", "COLUNA_OPCIONAL" };
    String[] values = { "12345", "Escola Teste", "" };
    RowContext context = new RowContext(headers, values, 7);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar colunas desconhecidas")
  void shouldIgnoreUnknownColumns() {
    // Given
    String[] headers = { "CODESC", "COLUNA_INEXISTENTE", "NOMESC" };
    String[] values = { "12345", "", "Escola Teste" };
    RowContext context = new RowContext(headers, values, 8);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve lidar com arrays de tamanhos diferentes")
  void shouldHandleArraysOfDifferentSizes() {
    // Given - headers maior que values
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "12345", "Escola Teste" }; // Falta NOMEDEP
    RowContext context = new RowContext(headers, values, 9);

    // When & Then
    // Não deve gerar exceção, pois não há valor para validar
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve lidar com values maior que headers")
  void shouldHandleValuesLargerThanHeaders() {
    // Given - values maior que headers
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "12345", "Escola Teste", "Valor Extra" };
    RowContext context = new RowContext(headers, values, 10);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar apenas índices válidos")
  void shouldValidateOnlyValidIndices() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "12345" }; // Só primeiro valor
    RowContext context = new RowContext(headers, values, 11);

    // When & Then
    // Deve validar apenas CODESC (índice 0), ignorar NOMESC e NOMEDEP
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve tratar campo métrico obrigatório vazio")
  void shouldHandleEmptyMandatoryMetricField() {
    // Given
    String[] headers = { "CODESC", "SALAS_AULA" };
    String[] values = { "12345", "" };
    RowContext context = new RowContext(headers, values, 12);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 12")
      .hasMessageContaining("SALAS_AULA")
      .hasMessageContaining("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName("Deve validar com headers e values vazios")
  void shouldValidateWithEmptyHeadersAndValues() {
    // Given
    String[] headers = {};
    String[] values = {};
    RowContext context = new RowContext(headers, values, 13);

    // When & Then
    assertThatNoException().isThrownBy(() -> mandatoryRule.validate(context));
  }

  @Test
  @DisplayName("Deve manter ordem de validação baseada na ordem do header")
  void shouldMaintainValidationOrderBasedOnHeaderOrder() {
    // Given
    String[] headers = { "NOMESC", "CODESC", "NOMEDEP" };
    String[] values = { "", "", "" };
    RowContext context = new RowContext(headers, values, 14);

    // When & Then
    assertThatThrownBy(() -> mandatoryRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("NOMESC")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("NOMEDEP");
  }
}
