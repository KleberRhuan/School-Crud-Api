/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator.rule;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.csv.domain.exception.RowValidationException;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SchoolCodeRule Tests")
class SchoolCodeRuleTest {

  private SchoolCodeRule schoolCodeRule;

  @BeforeEach
  void setUp() {
    schoolCodeRule = new SchoolCodeRule();
  }

  @Test
  @DisplayName("Deve validar linha com código de escola numérico positivo")
  void shouldValidateRowWithPositiveNumericSchoolCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "12345", "Escola Teste", "Departamento" };
    RowContext context = new RowContext(headers, values, 1);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar códigos positivos pequenos")
  void shouldValidateSmallPositiveCodes() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "1", "Escola Um" };
    RowContext context = new RowContext(headers, values, 2);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar código de escola zero")
  void shouldRejectZeroSchoolCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "0", "Escola Zero" };
    RowContext context = new RowContext(headers, values, 3);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Código da escola deve ser positivo");
  }

  @Test
  @DisplayName("Deve rejeitar código de escola negativo")
  void shouldRejectNegativeSchoolCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "-1", "Escola Negativa" };
    RowContext context = new RowContext(headers, values, 4);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Código da escola deve ser positivo");
  }

  @Test
  @DisplayName("Deve rejeitar código não numérico")
  void shouldRejectNonNumericCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "ABC", "Escola Alfa" };
    RowContext context = new RowContext(headers, values, 5);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining(
        "Código da escola deve ser um número inteiro válido"
      );
  }

  @Test
  @DisplayName("Deve rejeitar código alfanumérico")
  void shouldRejectAlphanumericCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "123ABC", "Escola Alfanumérica" };
    RowContext context = new RowContext(headers, values, 6);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining(
        "Código da escola deve ser um número inteiro válido"
      );
  }

  @Test
  @DisplayName("Deve rejeitar código decimal")
  void shouldRejectDecimalCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "123.45", "Escola Decimal" };
    RowContext context = new RowContext(headers, values, 7);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining(
        "Código da escola deve ser um número inteiro válido"
      );
  }

  @Test
  @DisplayName("Deve rejeitar código vazio")
  void shouldRejectEmptyCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "", "Escola Vazia" };
    RowContext context = new RowContext(headers, values, 8);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Código da escola (CODESC) é obrigatório");
  }

  @Test
  @DisplayName("Deve rejeitar código null")
  void shouldRejectNullCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { null, "Escola Null" };
    RowContext context = new RowContext(headers, values, 9);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Código da escola (CODESC) é obrigatório");
  }

  @Test
  @DisplayName("Deve rejeitar código só com espaços")
  void shouldRejectWhitespaceOnlyCode() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "   ", "Escola Espaços" };
    RowContext context = new RowContext(headers, values, 10);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Código da escola (CODESC) é obrigatório");
  }

  @Test
  @DisplayName("Deve validar código com espaços no início e fim")
  void shouldValidateCodeWithLeadingTrailingSpaces() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "  12345  ", "Escola Com Espaços" };
    RowContext context = new RowContext(headers, values, 11);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve ignorar linha sem coluna CODESC")
  void shouldIgnoreRowWithoutCodescColumn() {
    // Given
    String[] headers = { "NOMESC", "NOMEDEP" };
    String[] values = { "Escola Sem Código", "Departamento" };
    RowContext context = new RowContext(headers, values, 12);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve lidar com coluna CODESC em posição diferente")
  void shouldHandleCodescColumnInDifferentPosition() {
    // Given
    String[] headers = { "NOMESC", "CODESC", "NOMEDEP" };
    String[] values = { "Escola Teste", "12345", "Departamento" };
    RowContext context = new RowContext(headers, values, 13);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve lidar com arrays de tamanhos diferentes")
  void shouldHandleArraysOfDifferentSizes() {
    // Given - headers maior que values
    String[] headers = { "CODESC", "NOMESC", "NOMEDEP" };
    String[] values = { "12345", "Escola" }; // Falta NOMEDEP
    RowContext context = new RowContext(headers, values, 14);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName(
    "Deve lidar com values menor que headers quando CODESC está fora do range"
  )
  void shouldHandleValuesSmalllerThanHeadersWhenCodescOutOfRange() {
    // Given - values menor que headers, CODESC em posição não coberta
    String[] headers = { "NOMESC", "CODESC", "NOMEDEP" };
    String[] values = { "Escola" }; // Só cobre índice 0, CODESC está no índice 1
    RowContext context = new RowContext(headers, values, 15);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve validar com headers e values vazios")
  void shouldValidateWithEmptyHeadersAndValues() {
    // Given
    String[] headers = {};
    String[] values = {};
    RowContext context = new RowContext(headers, values, 16);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar números muito grandes que causam overflow")
  void shouldRejectVeryLargeNumbersCausingOverflow() {
    // Given
    String[] headers = { "CODESC" };
    String[] values = { "999999999999999999999" };
    RowContext context = new RowContext(headers, values, 17);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining(
        "Código da escola deve ser um número inteiro válido"
      );
  }

  @Test
  @DisplayName("Deve validar códigos positivos grandes")
  void shouldValidateLargePositiveCodes() {
    // Given
    String[] headers = { "CODESC", "NOMESC" };
    String[] values = { "999999", "Escola Grande" };
    RowContext context = new RowContext(headers, values, 18);

    // When & Then
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }

  @Test
  @DisplayName("Deve rejeitar código com caracteres especiais")
  void shouldRejectCodeWithSpecialCharacters() {
    // Given
    String[] headers = { "CODESC" };
    String[] values = { "123@45" };
    RowContext context = new RowContext(headers, values, 19);

    // When & Then
    assertThatThrownBy(() -> schoolCodeRule.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining(
        "Código da escola deve ser um número inteiro válido"
      );
  }

  @Test
  @DisplayName("Deve parar na primeira ocorrência de CODESC")
  void shouldStopAtFirstCodescOccurrence() {
    // Given - Duas colunas CODESC (cenário anômalo)
    String[] headers = { "CODESC", "NOMESC", "CODESC" };
    String[] values = { "12345", "Escola", "INVALID" };
    RowContext context = new RowContext(headers, values, 20);

    // When & Then - Deve validar apenas a primeira ocorrência
    assertThatNoException().isThrownBy(() -> schoolCodeRule.validate(context));
  }
}
