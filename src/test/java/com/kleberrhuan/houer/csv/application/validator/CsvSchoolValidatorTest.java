/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.csv.application.parser.CsvParser;
import com.kleberrhuan.houer.csv.application.processor.CsvProcessor;
import com.kleberrhuan.houer.csv.application.processor.CsvSchoolProcessor;
import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.application.validator.rule.MandatoryRule;
import com.kleberrhuan.houer.csv.application.validator.rule.NumericRule;
import com.kleberrhuan.houer.csv.application.validator.rule.RowRule;
import com.kleberrhuan.houer.csv.application.validator.rule.SchoolCodeRule;
import com.kleberrhuan.houer.csv.domain.exception.AggregatedValidationException;
import com.kleberrhuan.houer.csv.domain.exception.HeaderValidationException;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolColumn;
import com.kleberrhuan.houer.csv.domain.model.CsvSchoolRecord;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CsvSchoolValidator Tests - Atualizados para nova arquitetura")
class CsvSchoolValidatorTest {

  private CsvSchoolValidator validator;

  @BeforeEach
  void setUp() {
    // Instancia todos os componentes reais necessários para o pipeline de validação
    CsvColumnMetadataService metadataService = new CsvColumnMetadataService();

    List<RowRule> rules = List.of(
      new MandatoryRule(metadataService),
      new NumericRule(metadataService),
      new SchoolCodeRule()
    );

    CompositeRowValidator rowValidator = new CompositeRowValidator(rules);
    HeaderValidator headerValidator = new HeaderValidator();
    CsvParser parser = new CsvParser();
    CsvProcessor<CsvSchoolRecord> core = new CsvProcessor<>(
      parser,
      headerValidator,
      rowValidator
    );
    CsvSchoolProcessor schoolProcessor = new CsvSchoolProcessor(core);
    validator = new CsvSchoolValidator(schoolProcessor);
  }

  @Nested
  @DisplayName("Cenários de sucesso")
  class SuccessCases {

    @Test
    @DisplayName("Deve validar CSV válido e mapear registros corretamente")
    void shouldValidateValidCsvSuccessfully() {
      String csvContent = buildValidCsv();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      List<CsvSchoolRecord> records = validator
        .validate(inputStream, "test.csv")
        .collect(Collectors.toList());

      assertThat(records).hasSize(2);
      CsvSchoolRecord first = records.get(0);
      assertThat(first.codesc()).isEqualTo("11111111");
      assertThat(first.nomesc()).isEqualTo("ESCOLA TESTE");
      // Métrica SALAS_AULA foi definida como 10 no builder
      assertThat(first.metrics().get(CsvSchoolColumn.SALAS_AULA))
        .isEqualTo(10L);
    }
  }

  @Nested
  @DisplayName("Cenários de falha no cabeçalho")
  class HeaderFailureCases {

    @Test
    @DisplayName("Deve rejeitar CSV com coluna obrigatória faltando")
    void shouldRejectCsvWithMissingColumn() {
      String csvContent = buildCsvWithMissingColumn();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      assertThatThrownBy(() -> validator.validate(inputStream, "test.csv"))
        .isInstanceOf(HeaderValidationException.class)
        .hasMessageContaining("obrigatórias ausentes");
    }

    @Test
    @DisplayName("Deve rejeitar CSV com coluna extra não permitida")
    void shouldRejectCsvWithExtraColumn() {
      String csvContent = buildCsvWithExtraColumn();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      assertThatThrownBy(() -> validator.validate(inputStream, "test.csv"))
        .isInstanceOf(HeaderValidationException.class)
        .hasMessageContaining("não permitidas");
    }
  }

  @Nested
  @DisplayName("Cenários de falha em linhas")
  class RowFailureCases {

    @Test
    @DisplayName("Deve rejeitar linha com campo obrigatório vazio")
    void shouldRejectEmptyRequiredField() {
      String csvContent = buildCsvWithEmptyMainField();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      assertThatThrownBy(() ->
          validator
            .validate(inputStream, "test.csv")
            .collect(Collectors.toList())
        )
        .isInstanceOf(AggregatedValidationException.class)
        .hasMessageContaining("Campo obrigatório não pode estar vazio");
    }

    @Test
    @DisplayName("Deve rejeitar valor não numérico em coluna numérica")
    void shouldRejectInvalidNumericValue() {
      String csvContent = buildCsvWithInvalidNumericValue();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      assertThatThrownBy(() ->
          validator
            .validate(inputStream, "test.csv")
            .collect(Collectors.toList())
        )
        .isInstanceOf(AggregatedValidationException.class)
        .hasMessageContaining("não é um número válido");
    }
  }

  // ------------------------------------------------------------
  // Builders utilitários para gerar conteúdos CSV de teste
  // ------------------------------------------------------------

  private String buildValidCsv() {
    StringBuilder csv = new StringBuilder();

    CsvSchoolColumn[] allColumns = CsvSchoolColumn.values();
    for (int i = 0; i < allColumns.length; i++) {
      if (i > 0) csv.append(',');
      csv.append(allColumns[i].name());
    }
    csv.append('\n');

    // Primeira linha válida - todas as métricas preenchidas
    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,1,PUBLICA,ATIVA"
    );
    for (int i = 9; i < allColumns.length; i++) {
      csv.append(',').append("10"); // Todas as métricas com valor 10
    }
    csv.append('\n');

    // Segunda linha válida - todas as métricas preenchidas
    csv.append(
      "REDE EXEMPLO,DE EXEMPLO,RIO DE JANEIRO,ZONA SUL,22222222,ESCOLA EXEMPLO,2,PRIVADA,ATIVA"
    );
    for (int i = 9; i < allColumns.length; i++) {
      csv.append(',').append("20"); // Todas as métricas com valor 20
    }

    return csv.toString();
  }

  private String buildCsvWithMissingColumn() {
    // Remove a coluna CODESC
    return (
      "NOMEDEP,DE,MUN,DISTR,NOMESC,TIPOESC,TIPOESC_DESC,CODSIT\n" +
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,ESCOLA TESTE,1,PUBLICA,ATIVA"
    );
  }

  private String buildCsvWithExtraColumn() {
    StringBuilder csv = new StringBuilder();
    CsvSchoolColumn[] allColumns = CsvSchoolColumn.values();

    for (int i = 0; i < allColumns.length; i++) {
      if (i > 0) csv.append(',');
      csv.append(allColumns[i].name());
    }
    csv.append(",COLUNA_EXTRA\n");

    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,1,PUBLICA,ATIVA"
    );
    for (int i = 9; i < allColumns.length; i++) {
      csv.append(',').append('1');
    }
    csv.append(",valor_extra");

    return csv.toString();
  }

  private String buildCsvWithEmptyMainField() {
    StringBuilder csv = new StringBuilder();
    CsvSchoolColumn[] allColumns = CsvSchoolColumn.values();

    for (int i = 0; i < allColumns.length; i++) {
      if (i > 0) csv.append(',');
      csv.append(allColumns[i].name());
    }
    csv.append('\n');

    // CODESC vazio (campo principal obrigatório)
    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,,ESCOLA TESTE,1,PUBLICA,ATIVA"
    );
    for (int i = 9; i < allColumns.length; i++) {
      csv.append(',').append("10"); // Todas as métricas preenchidas
    }

    return csv.toString();
  }

  private String buildCsvWithInvalidNumericValue() {
    StringBuilder csv = new StringBuilder();
    CsvSchoolColumn[] allColumns = CsvSchoolColumn.values();

    for (int i = 0; i < allColumns.length; i++) {
      if (i > 0) csv.append(',');
      csv.append(allColumns[i].name());
    }
    csv.append('\n');

    // TIPOESC com texto inválido (campo principal numérico)
    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,TEXTO_INVALIDO,PUBLICA,ATIVA"
    );
    for (int i = 9; i < allColumns.length; i++) {
      csv.append(',').append("10"); // Todas as métricas preenchidas com valores válidos
    }

    return csv.toString();
  }
}
