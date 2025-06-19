/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DataType - Testes de Validação e Conversão")
class DataTypeTest {

  @Test
  @DisplayName("Deve converter string para enum corretamente")
  void shouldConvertStringToEnumCorrectly() {
    // Given & When & Then
    assertThat(DataType.fromString("INT")).isEqualTo(DataType.INT);
    assertThat(DataType.fromString("int")).isEqualTo(DataType.INT);
    assertThat(DataType.fromString("STRING")).isEqualTo(DataType.STRING);
    assertThat(DataType.fromString("string")).isEqualTo(DataType.STRING);
    assertThat(DataType.fromString("BOOLEAN")).isEqualTo(DataType.BOOLEAN);
    assertThat(DataType.fromString("DECIMAL")).isEqualTo(DataType.DECIMAL);
  }

  @ParameterizedTest
  @ValueSource(strings = { "INVALID", "FLOAT" })
  @DisplayName("Deve lançar exceção para tipos inválidos")
  void shouldThrowExceptionForInvalidTypes(String invalidType) {
    // Given & When & Then
    assertThatThrownBy(() -> DataType.fromString(invalidType))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Tipo de dados não suportado");
  }

  @ParameterizedTest
  @ValueSource(strings = { "", "   " })
  @DisplayName("Deve lançar exceção para tipos vazios")
  void shouldThrowExceptionForEmptyTypes(String emptyType) {
    // Given & When & Then
    assertThatThrownBy(() -> DataType.fromString(emptyType))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Tipo de dados não pode ser nulo ou vazio");
  }

  @Test
  @DisplayName("Deve lançar exceção para entrada nula")
  void shouldThrowExceptionForNullInput() {
    // Given & When & Then
    assertThatThrownBy(() -> DataType.fromString(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Tipo de dados não pode ser nulo ou vazio");
  }

  @ParameterizedTest
  @CsvSource(
    {
      "INT, '123', true",
      "INT, '-45', true",
      "INT, '12.5', false",
      "INT, 'abc', false",
      "STRING, 'qualquer coisa', true",
      "STRING, '', false",
      "BOOLEAN, 'true', true",
      "BOOLEAN, 'false', true",
      "BOOLEAN, '1', true",
      "BOOLEAN, '0', true",
      "BOOLEAN, 'TRUE', true",
      "BOOLEAN, 'maybe', false",
      "DECIMAL, '12.5', true",
      "DECIMAL, '-3.14', true",
      "DECIMAL, '100', true",
      "DECIMAL, 'abc', false",
    }
  )
  @DisplayName("Deve validar valores corretamente")
  void shouldValidateValuesCorrectly(
    DataType type,
    String value,
    boolean expected
  ) {
    // Given & When
    boolean result = type.isValidValue(value);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("Deve retornar false para valores nulos ou vazios")
  void shouldReturnFalseForNullOrEmptyValues() {
    // Given & When & Then
    assertThat(DataType.INT.isValidValue(null)).isFalse();
    assertThat(DataType.INT.isValidValue("")).isFalse();
    assertThat(DataType.INT.isValidValue("   ")).isFalse();

    assertThat(DataType.STRING.isValidValue(null)).isFalse();
    assertThat(DataType.STRING.isValidValue("")).isFalse();
    assertThat(DataType.STRING.isValidValue("   ")).isFalse();
  }

  @Test
  @DisplayName("Deve retornar descrições corretas")
  void shouldReturnCorrectDescriptions() {
    // Given & When & Then
    assertThat(DataType.INT.getDescription()).isEqualTo("Inteiro");
    assertThat(DataType.STRING.getDescription()).isEqualTo("Texto");
    assertThat(DataType.BOOLEAN.getDescription()).isEqualTo("Booleano");
    assertThat(DataType.DECIMAL.getDescription()).isEqualTo("Decimal");
  }
}
