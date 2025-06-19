/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.parser;

import static org.assertj.core.api.Assertions.*;

import com.opencsv.exceptions.CsvException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CsvParser Tests")
class CsvParserTest {

  private CsvParser csvParser;

  @BeforeEach
  void setUp() {
    csvParser = new CsvParser();
  }

  @Test
  @DisplayName("Deve ler CSV válido e retornar Stream<String[]>")
  void shouldReadValidCsvAndReturnStream() throws IOException, CsvException {
    // Given
    String csvContent =
      """
        CODESC;NOMESC;NOMEDEP
        12345;Escola Teste;Departamento Teste
        67890;Escola Dois;Departamento Dois
        """;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(3);

    // Header
    assertThat(lines.get(0)).containsExactly("CODESC", "NOMESC", "NOMEDEP");

    // First data row
    assertThat(lines.get(1))
      .containsExactly("12345", "Escola Teste", "Departamento Teste");

    // Second data row
    assertThat(lines.get(2))
      .containsExactly("67890", "Escola Dois", "Departamento Dois");
  }

  @Test
  @DisplayName("Deve ler CSV vazio e retornar Stream vazio")
  void shouldReadEmptyCsvAndReturnEmptyStream()
    throws IOException, CsvException {
    // Given
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      "".getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    assertThat(result.toList()).isEmpty();
  }

  @Test
  @DisplayName("Deve ler CSV com caracteres especiais UTF-8")
  void shouldReadCsvWithUtf8Characters() throws IOException, CsvException {
    // Given
    String csvContent =
      """
        NOMESC;DESCRICAO
        Escola São João;Descrição com acentuação
        Escola Ação;Educação física
        """;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(3);
    assertThat(lines.get(1))
      .containsExactly("Escola São João", "Descrição com acentuação");
    assertThat(lines.get(2)).containsExactly("Escola Ação", "Educação física");
  }

  @Test
  @DisplayName("Deve ler CSV com ponto-e-vírgula entre aspas")
  void shouldReadCsvWithSemicolonsInQuotes() throws IOException, CsvException {
    // Given
    String csvContent =
      """
        NOME;ENDERECO
        "Silva; João";"Rua A; 123"
        "Santos; Maria";"Av. B; 456"
        """;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(3);
    assertThat(lines.get(1)).containsExactly("Silva; João", "Rua A; 123");
    assertThat(lines.get(2)).containsExactly("Santos; Maria", "Av. B; 456");
  }

  @Test
  @DisplayName("Deve retornar Iterator quando usar readAsIterator")
  void shouldReturnIteratorWhenUsingReadAsIterator() throws IOException {
    // Given
    String csvContent =
      """
        CODESC;NOMESC
        12345;Escola Um
        67890;Escola Dois
        """;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Iterator<String[]> iterator = csvParser.readAsIterator(inputStream);

    // Then
    assertThat(iterator.hasNext()).isTrue();

    String[] header = iterator.next();
    assertThat(header).containsExactly("CODESC", "NOMESC");

    assertThat(iterator.hasNext()).isTrue();
    String[] firstRow = iterator.next();
    assertThat(firstRow).containsExactly("12345", "Escola Um");

    assertThat(iterator.hasNext()).isTrue();
    String[] secondRow = iterator.next();
    assertThat(secondRow).containsExactly("67890", "Escola Dois");

    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  @DisplayName("Deve lidar com CSV contendo apenas header")
  void shouldHandleCsvWithOnlyHeader() throws IOException, CsvException {
    // Given
    String csvContent = "CODESC;NOMESC;NOMEDEP";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(1);
    assertThat(lines.get(0)).containsExactly("CODESC", "NOMESC", "NOMEDEP");
  }

  @Test
  @DisplayName("Deve lidar com linhas com diferentes números de colunas")
  void shouldHandleLinesWithDifferentColumnCounts()
    throws IOException, CsvException {
    // Given
    String csvContent =
      """
        COL1;COL2;COL3
        A;B;C
        D;E
        F;G;H;I
        """;
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(4);
    assertThat(lines.get(0)).hasSize(3); // Header
    assertThat(lines.get(1)).hasSize(3); // Complete row
    assertThat(lines.get(2)).hasSize(2); // Incomplete row
    assertThat(lines.get(3)).hasSize(4); // Row with extra column
  }

  @Test
  @DisplayName("Deve processar CSV mesmo quando stream tem dados limitados")
  void shouldProcessCsvEvenWithLimitedStreamData()
    throws IOException, CsvException {
    // Given - CSV com apenas uma linha de dados
    String csvContent = "CODESC\n12345";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(
      csvContent.getBytes(StandardCharsets.UTF_8)
    );

    // When
    Stream<String[]> result = csvParser.read(inputStream);

    // Then
    List<String[]> lines = result.toList();
    assertThat(lines).hasSize(2);
    assertThat(lines.get(0)).containsExactly("CODESC");
    assertThat(lines.get(1)).containsExactly("12345");
  }
}
