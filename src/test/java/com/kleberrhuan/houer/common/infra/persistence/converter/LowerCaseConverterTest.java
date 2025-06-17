/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.converter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LowerCaseConverterTest {

  private final LowerCaseConverter converter = new LowerCaseConverter();

  @Test
  @DisplayName("convertToDatabaseColumn deve retornar lowercase e trim")
  void shouldConvertLowercase() {
    String result = converter.convertToDatabaseColumn("  TeSt@Email.COM  ");
    assertThat(result).isEqualTo("test@email.com");
  }

  @Test
  @DisplayName("convertToDatabaseColumn deve retornar null se entrada null")
  void shouldReturnNullWhenInputNull() {
    assertThat(converter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  @DisplayName("convertToEntityAttribute retorna valor intacto")
  void shouldReturnSameValue() {
    assertThat(converter.convertToEntityAttribute("abc")).isEqualTo("abc");
  }
}
