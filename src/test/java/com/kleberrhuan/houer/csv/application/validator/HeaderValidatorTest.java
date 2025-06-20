/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.domain.exception.HeaderValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HeaderValidator Tests")
class HeaderValidatorTest {

  private HeaderValidator headerValidator;
  private String[] validHeader;
  private final String filename = "test.csv";

  @BeforeEach
  void setUp() {
    headerValidator = new HeaderValidator(new CsvColumnMetadataService());

    // Cria header válido com todas as colunas do enum
    validHeader =
      Arrays
        .stream(CsvSchoolColumn.values())
        .map(Enum::name)
        .toArray(String[]::new);
  }

  @Test
  @DisplayName("Deve validar header válido com sucesso")
  void shouldValidateValidHeaderSuccessfully() {
    // When & Then
    assertThatNoException()
      .isThrownBy(() -> headerValidator.validate(validHeader, filename));

    String[] result = headerValidator.validate(validHeader, filename);
    assertThat(result).isEqualTo(validHeader);
  }

  @Test
  @DisplayName("Deve rejeitar header com colunas obrigatórias ausentes")
  void shouldRejectHeaderWithMissingRequiredColumns() {
    // Given - Remove algumas colunas obrigatórias
    String[] incompleteHeader = Arrays
      .stream(validHeader)
      .filter(col -> !"CODESC".equals(col) && !"NOMESC".equals(col))
      .toArray(String[]::new);

    // When & Then
    assertThatThrownBy(() ->
        headerValidator.validate(incompleteHeader, filename)
      )
      .isInstanceOf(HeaderValidationException.class)
      .hasMessageContaining("Colunas obrigatórias ausentes")
      .hasMessageContaining("CODESC")
      .hasMessageContaining("NOMESC")
      .hasMessageContaining(filename);
  }

  @Test
  @DisplayName("Deve rejeitar header com colunas extras não permitidas")
  void shouldRejectHeaderWithExtraColumns() {
    // Given - Adiciona colunas extras
    List<String> headerList = Arrays.asList(validHeader);
    String[] headerWithExtra = headerList
      .stream()
      .collect(Collectors.toList())
      .toArray(String[]::new);

    // Cria novo array com colunas extras
    String[] expandedHeader = new String[headerWithExtra.length + 2];
    System.arraycopy(
      headerWithExtra,
      0,
      expandedHeader,
      0,
      headerWithExtra.length
    );
    expandedHeader[headerWithExtra.length] = "COLUNA_EXTRA_1";
    expandedHeader[headerWithExtra.length + 1] = "COLUNA_EXTRA_2";

    // When & Then
    assertThatThrownBy(() -> headerValidator.validate(expandedHeader, filename))
      .isInstanceOf(HeaderValidationException.class)
      .hasMessageContaining("Colunas não permitidas")
      .hasMessageContaining("COLUNA_EXTRA_1")
      .hasMessageContaining("COLUNA_EXTRA_2")
      .hasMessageContaining(filename);
  }

  @Test
  @DisplayName("Deve rejeitar header com quantidade incorreta de colunas")
  void shouldRejectHeaderWithIncorrectColumnCount() {
    // Given - Header com menos colunas
    String[] shortHeader = { "CODESC", "NOMESC", "NOMEDEP" };

    // When & Then
    assertThatThrownBy(() -> headerValidator.validate(shortHeader, filename))
      .isInstanceOf(HeaderValidationException.class)
      .hasMessageContaining("Colunas obrigatórias ausentes")
      .hasMessageContaining(filename);
  }

  @Test
  @DisplayName("Deve validar header em ordem diferente")
  void shouldValidateHeaderInDifferentOrder() {
    // Given - Header válido mas em ordem diferente
    String[] shuffledHeader = {
      "NOMESC",
      "CODESC",
      "NOMEDEP",
      "DE",
      "MUN",
      "DISTR",
      "TIPOESC",
      "TIPOESC_DESC",
      "CODSIT",
    };

    // Adiciona todas as outras colunas métricas
    List<String> allColumns = Arrays
      .stream(CsvSchoolColumn.values())
      .map(Enum::name)
      .collect(Collectors.toList());

    List<String> shuffledList = Arrays.asList(shuffledHeader);
    allColumns.removeAll(shuffledList);

    // Cria array completo usando List para evitar ArrayIndexOutOfBounds
    List<String> completeShuffledList = new ArrayList<>(shuffledList);
    completeShuffledList.addAll(allColumns);

    String[] completeShuffled = completeShuffledList.toArray(String[]::new);

    // When & Then
    assertThatNoException()
      .isThrownBy(() -> headerValidator.validate(completeShuffled, filename));
  }

  @Test
  @DisplayName("Deve rejeitar header vazio")
  void shouldRejectEmptyHeader() {
    // Given
    String[] emptyHeader = {};

    // When & Then
    assertThatThrownBy(() -> headerValidator.validate(emptyHeader, filename))
      .isInstanceOf(HeaderValidationException.class)
      .hasMessageContaining("quantidade incorreta de colunas")
      .hasMessageContaining("0");
  }

  @Test
  @DisplayName("Deve rejeitar header com apenas uma coluna")
  void shouldRejectHeaderWithSingleColumn() {
    // Given
    String[] singleColumnHeader = { "CODESC" };

    // When & Then
    assertThatThrownBy(() ->
        headerValidator.validate(singleColumnHeader, filename)
      )
      .isInstanceOf(HeaderValidationException.class)
      .hasMessageContaining("Colunas obrigatórias ausentes");
  }

  @Test
  @DisplayName("Deve validar com todas as colunas obrigatórias presentes")
  void shouldValidateWithAllRequiredColumnsPresent() {
    // Given - Testa que realmente temos todas as colunas esperadas
    int expectedColumnCount = CsvSchoolColumn.values().length;

    // When
    String[] result = headerValidator.validate(validHeader, filename);

    // Then
    assertThat(result).hasSize(expectedColumnCount);
    assertThat(result)
      .containsExactlyInAnyOrder(
        Arrays
          .stream(CsvSchoolColumn.values())
          .map(Enum::name)
          .toArray(String[]::new)
      );
  }

  @Test
  @DisplayName("Deve ignorar colunas duplicadas e retornar header limpo")
  void shouldIgnoreDuplicateColumnsAndReturnCleanHeader() {
    // Given - Header com coluna duplicada
    String[] duplicateHeader = new String[validHeader.length + 1];
    System.arraycopy(validHeader, 0, duplicateHeader, 0, validHeader.length);
    duplicateHeader[validHeader.length] = "CODESC"; // Duplica CODESC

    // When
    String[] result = headerValidator.validate(duplicateHeader, filename);

    // Then - Deve retornar header sem duplicatas
    assertThat(result).hasSize(validHeader.length);
    assertThat(result).containsExactlyInAnyOrder(validHeader);

    // Verifica que não há duplicatas
    Set<String> uniqueColumns = Set.of(result);
    assertThat(uniqueColumns).hasSize(result.length);
  }

  @Test
  @DisplayName("Deve remover BOM UTF-8 e espaços do header")
  void shouldRemoveBomAndWhitespaceFromHeader() {
    // Given - Header com BOM UTF-8 na primeira coluna e espaços
    char BOM = '\uFEFF';
    String[] headerWithBom = Arrays
      .stream(validHeader)
      .map(col -> {
        if ("NOMEDEP".equals(col)) {
          return BOM + col; // Adiciona BOM na primeira coluna
        }
        return "  " + col + "  "; // Adiciona espaços nas outras
      })
      .toArray(String[]::new);

    // When
    String[] result = headerValidator.validate(headerWithBom, filename);

    // Then - Deve retornar header limpo
    assertThat(result).hasSize(validHeader.length);
    assertThat(result).containsExactlyInAnyOrder(validHeader);

    // Verifica que não há BOM nem espaços
    assertThat(result).allMatch(col -> !col.contains(String.valueOf(BOM)));
    assertThat(result).allMatch(col -> col.equals(col.trim()));
  }

  @Test
  @DisplayName("Deve tratar header com espaços em branco")
  void shouldHandleHeaderWithWhitespace() {
    // Given - Header com espaços, mas colunas corretas
    String[] headerWithSpaces = Arrays
      .stream(validHeader)
      .map(col -> "  " + col + "  ")
      .toArray(String[]::new);

    // When & Then - Agora deve funcionar pois fazemos trim
    assertThatNoException()
      .isThrownBy(() -> headerValidator.validate(headerWithSpaces, filename));

    String[] result = headerValidator.validate(headerWithSpaces, filename);
    assertThat(result).containsExactlyInAnyOrder(validHeader);
  }
}
