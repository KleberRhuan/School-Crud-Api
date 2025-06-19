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
@DisplayName("NumericRule Tests")
class NumericRuleTest {

  @Mock
  private CsvColumnMetadataService metadataService;

  @InjectMocks
  private NumericRule numericRule;

  @BeforeEach
  void setUp() {
    // Setup padrão para colunas conhecidas com lenient
    lenient()
      .when(metadataService.getColumnInfo("TIPOESC"))
      .thenReturn(Optional.of(CsvSchoolColumn.TIPOESC));
    lenient()
      .when(metadataService.getColumnInfo("NOMESC"))
      .thenReturn(Optional.of(CsvSchoolColumn.NOMESC));
    lenient()
      .when(metadataService.getColumnInfo("CODESC"))
      .thenReturn(Optional.of(CsvSchoolColumn.CODESC));
    lenient()
      .when(metadataService.getColumnInfo("SALAS_AULA"))
      .thenReturn(Optional.of(CsvSchoolColumn.SALAS_AULA));
    lenient()
      .when(metadataService.getColumnInfo("BIBLIOTECA"))
      .thenReturn(Optional.of(CsvSchoolColumn.BIBLIOTECA));
    lenient()
      .when(metadataService.getColumnInfo("QUADRA_COBERTA"))
      .thenReturn(Optional.of(CsvSchoolColumn.QUADRA_COBERTA));
    lenient()
      .when(metadataService.getColumnInfo("COLUNA_INEXISTENTE"))
      .thenReturn(Optional.empty());
  }

  @Test
  @DisplayName("Deve validar campos numéricos com valores inteiros válidos")
  void shouldValidateNumericFieldsWithValidIntegerValues() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "1", "15", "1" };
    RowContext context = new RowContext(headers, values, 1);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar campos numéricos com valor zero")
  void shouldValidateNumericFieldsWithZeroValue() {
    // Given
    String[] headers = { "SALAS_AULA", "BIBLIOTECA", "QUADRA_COBERTA" };
    String[] values = { "0", "0", "0" };
    RowContext context = new RowContext(headers, values, 2);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar campos numéricos com números grandes")
  void shouldValidateNumericFieldsWithLargeNumbers() {
    // Given
    String[] headers = { "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "999", "100" };
    RowContext context = new RowContext(headers, values, 3);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar campos numéricos com valores não numéricos")
  void shouldRejectNumericFieldsWithNonNumericValues() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "ABC", "15" };
    RowContext context = new RowContext(headers, values, 4);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 4")
      .hasMessageContaining("TIPOESC")
      .hasMessageContaining("não é um número válido");
  }

  @Test
  @DisplayName("Deve rejeitar campos numéricos com números negativos")
  void shouldRejectNumericFieldsWithNegativeNumbers() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "1", "-5" };
    RowContext context = new RowContext(headers, values, 5);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 5")
      .hasMessageContaining("SALAS_AULA")
      .hasMessageContaining("deve ser um número inteiro não negativo");
  }

  @Test
  @DisplayName("Deve rejeitar campos numéricos com números decimais")
  void shouldRejectNumericFieldsWithDecimalNumbers() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "1.5", "10" };
    RowContext context = new RowContext(headers, values, 6);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 6")
      .hasMessageContaining("TIPOESC")
      .hasMessageContaining("não é um número válido");
  }

  @Test
  @DisplayName("Deve agregar múltiplos erros de validação numérica")
  void shouldAggregateMultipleNumericValidationErrors() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "ABC", "-5", "10.5" };
    RowContext context = new RowContext(headers, values, 7);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 7")
      .hasMessageContaining("TIPOESC")
      .hasMessageContaining("SALAS_AULA")
      .hasMessageContaining("BIBLIOTECA")
      .hasMessageContaining("deve ser um número inteiro não negativo");
  }

  @Test
  @DisplayName("Deve ignorar campos não numéricos")
  void shouldIgnoreNonNumericFields() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "SALAS_AULA" };
    String[] values = { "ABC123", "Escola Teste", "15" };
    RowContext context = new RowContext(headers, values, 8);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar colunas desconhecidas")
  void shouldIgnoreUnknownColumns() {
    // Given
    String[] headers = { "COLUNA_INEXISTENTE", "SALAS_AULA" };
    String[] values = { "QUALQUER_VALOR", "10" };
    RowContext context = new RowContext(headers, values, 9);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar campos numéricos vazios")
  void shouldIgnoreEmptyNumericFields() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "", "15", "" };
    RowContext context = new RowContext(headers, values, 10);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar campos numéricos null")
  void shouldIgnoreNullNumericFields() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { null, "15" };
    RowContext context = new RowContext(headers, values, 11);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar campos numéricos só com espaços")
  void shouldIgnoreWhitespaceOnlyNumericFields() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "   ", "15" };
    RowContext context = new RowContext(headers, values, 12);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve aceitar números com espaços no início e fim")
  void shouldAcceptNumbersWithLeadingTrailingSpaces() {
    // Given
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "  1  ", "  15  " };
    RowContext context = new RowContext(headers, values, 13);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar números com caracteres especiais")
  void shouldRejectNumbersWithSpecialCharacters() {
    // Given
    String[] headers = { "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "1@5", "10#" };
    RowContext context = new RowContext(headers, values, 14);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 14")
      .hasMessageContaining("SALAS_AULA")
      .hasMessageContaining("BIBLIOTECA");
  }

  @Test
  @DisplayName("Deve rejeitar números muito grandes")
  void shouldRejectVeryLargeNumbers() {
    // Given
    String[] headers = { "SALAS_AULA" };
    String[] values = { "999999999999999999999" };
    RowContext context = new RowContext(headers, values, 15);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Linha 15")
      .hasMessageContaining("SALAS_AULA")
      .hasMessageContaining("não é um número válido");
  }

  @Test
  @DisplayName("Deve lidar com arrays de tamanhos diferentes")
  void shouldHandleArraysOfDifferentSizes() {
    // Given - headers maior que values
    String[] headers = { "TIPOESC", "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "1", "15" }; // Falta BIBLIOTECA
    RowContext context = new RowContext(headers, values, 16);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve lidar com values maior que headers")
  void shouldHandleValuesLargerThanHeaders() {
    // Given - values maior que headers
    String[] headers = { "TIPOESC", "SALAS_AULA" };
    String[] values = { "1", "15", "extra" };
    RowContext context = new RowContext(headers, values, 17);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar com headers e values vazios")
  void shouldValidateWithEmptyHeadersAndValues() {
    // Given
    String[] headers = {};
    String[] values = {};
    RowContext context = new RowContext(headers, values, 18);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }

  @Test
  @DisplayName("Deve manter ordem de validação baseada na ordem do header")
  void shouldMaintainValidationOrderBasedOnHeaderOrder() {
    // Given
    String[] headers = { "BIBLIOTECA", "TIPOESC", "SALAS_AULA" };
    String[] values = { "ABC", "XYZ", "-1" };
    RowContext context = new RowContext(headers, values, 19);

    // When & Then
    assertThatThrownBy(() -> numericRule.validate(context))
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("BIBLIOTECA")
      .hasMessageContaining("TIPOESC")
      .hasMessageContaining("SALAS_AULA");
  }

  @Test
  @DisplayName("Deve aceitar zero com espaços")
  void shouldAcceptZeroWithSpaces() {
    // Given
    String[] headers = { "SALAS_AULA", "BIBLIOTECA" };
    String[] values = { "  0  ", "0" };
    RowContext context = new RowContext(headers, values, 20);

    // When & Then
    assertThatNoException().isThrownBy(() -> numericRule.validate(context));
  }
}
