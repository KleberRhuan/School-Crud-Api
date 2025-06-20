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
  private CsvColumnMetadataService metadataService;

  @BeforeEach
  void setUp() {
    // Instancia todos os componentes reais necessários para o pipeline de validação
    metadataService = new CsvColumnMetadataService();

    List<RowRule> rules = List.of(
      new MandatoryRule(metadataService),
      new NumericRule(metadataService),
      new SchoolCodeRule()
    );

    CompositeRowValidator rowValidator = new CompositeRowValidator(rules);
    HeaderValidator headerValidator = new HeaderValidator(metadataService);
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
      // Verificar métricas opcionais se presentes
      if (first.metrics().containsKey(CsvSchoolColumn.SALAS_AULA)) {
        assertThat(first.metrics().get(CsvSchoolColumn.SALAS_AULA))
          .isEqualTo(10L);
      }
    }

    @Test
    @DisplayName("Deve validar CSV com apenas colunas obrigatórias")
    void shouldValidateCsvWithOnlyRequiredColumns() {
      String csvContent = buildCsvWithOnlyRequiredColumns();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
        csvContent.getBytes(StandardCharsets.UTF_8)
      );

      List<CsvSchoolRecord> records = validator
        .validate(inputStream, "test.csv")
        .collect(Collectors.toList());

      assertThat(records).hasSize(1);
      CsvSchoolRecord record = records.get(0);
      assertThat(record.codesc()).isEqualTo("11111111");
      assertThat(record.nomesc()).isEqualTo("ESCOLA OBRIGATORIA");
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

    // Usar apenas colunas obrigatórias + algumas métricas básicas
    String[] columns = {
      "NOMEDEP",
      "DE",
      "MUN",
      "DISTR",
      "CODESC",
      "NOMESC",
      "TIPOESC",
      "TIPOESC_DESC",
      "CODSIT",
      "SALAS_AULA",
      "BIBLIOTECA",
    };

    csv.append(String.join(",", columns)).append('\n');

    // Primeira linha válida
    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,1,PUBLICA,1,10,1\n"
    );

    // Segunda linha válida
    csv.append(
      "REDE EXEMPLO,DE EXEMPLO,RIO DE JANEIRO,ZONA SUL,22222222,ESCOLA EXEMPLO,2,PRIVADA,1,20,1"
    );

    return csv.toString();
  }

  private String buildCsvWithOnlyRequiredColumns() {
    // Usar apenas as colunas obrigatórias básicas (não métricas)
    String[] requiredColumns = metadataService
      .getRequiredHeaders()
      .stream()
      .map(Enum::name)
      .toArray(String[]::new);

    StringBuilder csv = new StringBuilder();
    csv.append(String.join(",", requiredColumns)).append('\n');
    csv.append(
      "REDE OBRIGATORIA,DE OBRIGATORIA,SAO PAULO,CENTRO,11111111,ESCOLA OBRIGATORIA,1,PUBLICA,1"
    );

    return csv.toString();
  }

  private String buildCsvWithMissingColumn() {
    // Remove a coluna CODESC (obrigatória)
    return (
      "NOMEDEP,DE,MUN,DISTR,NOMESC,TIPOESC,TIPOESC_DESC,CODSIT\n" +
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,ESCOLA TESTE,1,PUBLICA,1"
    );
  }

  private String buildCsvWithExtraColumn() {
    StringBuilder csv = new StringBuilder();

    // Usar colunas válidas + uma inválida
    String[] validColumns = {
      "NOMEDEP",
      "DE",
      "MUN",
      "DISTR",
      "CODESC",
      "NOMESC",
      "TIPOESC",
      "TIPOESC_DESC",
      "CODSIT",
    };

    csv.append(String.join(",", validColumns));
    csv.append(",COLUNA_INVALIDA_INEXISTENTE\n"); // Coluna que não existe no enum

    csv.append(
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,1,PUBLICA,1,valor_extra"
    );

    return csv.toString();
  }

  private String buildCsvWithEmptyMainField() {
    return (
      "NOMEDEP,DE,MUN,DISTR,CODESC,NOMESC,TIPOESC,TIPOESC_DESC,CODSIT\n" +
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,,ESCOLA TESTE,1,PUBLICA,1" // CODESC vazio
    );
  }

  private String buildCsvWithInvalidNumericValue() {
    return (
      "NOMEDEP,DE,MUN,DISTR,CODESC,NOMESC,TIPOESC,TIPOESC_DESC,CODSIT\n" +
      "REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,TEXTO_INVALIDO,PUBLICA,1" // TIPOESC com
      // texto
    );
  }
}
