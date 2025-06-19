/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ValidationErrorAggregator Tests")
class ValidationErrorAggregatorTest {

  private ValidationErrorAggregator aggregator;

  @BeforeEach
  void setUp() {
    aggregator = new ValidationErrorAggregator();
  }

  @Test
  @DisplayName("Deve iniciar sem erros")
  void shouldStartWithoutErrors() {
    // When & Then
    assertThat(aggregator.hasErrors()).isFalse();
    assertThat(aggregator.getErrors()).isEmpty();
  }

  @Test
  @DisplayName("Deve adicionar erro único")
  void shouldAddSingleError() {
    // When
    aggregator.addError(1, "CODESC", "Campo obrigatório não pode estar vazio");

    // Then
    assertThat(aggregator.hasErrors()).isTrue();
    assertThat(aggregator.getErrors()).hasSize(1);

    ValidationErrorAggregator.ValidationError error = aggregator
      .getErrors()
      .get(0);
    assertThat(error.lineNumber()).isEqualTo(1);
    assertThat(error.columnName()).isEqualTo("CODESC");
    assertThat(error.message())
      .isEqualTo("Campo obrigatório não pode estar vazio");
  }

  @Test
  @DisplayName("Deve adicionar múltiplos erros")
  void shouldAddMultipleErrors() {
    // When
    aggregator.addError(1, "CODESC", "Campo obrigatório vazio");
    aggregator.addError(2, "NOMESC", "Nome muito longo");
    aggregator.addError(3, "TIPOESC", "Valor numérico inválido");

    // Then
    assertThat(aggregator.hasErrors()).isTrue();
    assertThat(aggregator.getErrors()).hasSize(3);

    List<ValidationErrorAggregator.ValidationError> errors =
      aggregator.getErrors();

    // Primeiro erro
    assertThat(errors.get(0).lineNumber()).isEqualTo(1);
    assertThat(errors.get(0).columnName()).isEqualTo("CODESC");
    assertThat(errors.get(0).message()).isEqualTo("Campo obrigatório vazio");

    // Segundo erro
    assertThat(errors.get(1).lineNumber()).isEqualTo(2);
    assertThat(errors.get(1).columnName()).isEqualTo("NOMESC");
    assertThat(errors.get(1).message()).isEqualTo("Nome muito longo");

    // Terceiro erro
    assertThat(errors.get(2).lineNumber()).isEqualTo(3);
    assertThat(errors.get(2).columnName()).isEqualTo("TIPOESC");
    assertThat(errors.get(2).message()).isEqualTo("Valor numérico inválido");
  }

  @Test
  @DisplayName("Não deve lançar exceção quando não há erros")
  void shouldNotThrowExceptionWhenNoErrors() {
    // When & Then
    assertThatNoException().isThrownBy(() -> aggregator.throwIfErrors());
  }

  @Test
  @DisplayName("Deve lançar AggregatedValidationException quando há erros")
  void shouldThrowAggregatedValidationExceptionWhenHasErrors() {
    // Given
    aggregator.addError(1, "CODESC", "Campo obrigatório vazio");
    aggregator.addError(2, "NOMESC", "Nome inválido");

    // When & Then
    assertThatThrownBy(() -> aggregator.throwIfErrors())
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Validação falhou com 2 erro(s)")
      .hasMessageContaining("Linha 1, Coluna 'CODESC': Campo obrigatório vazio")
      .hasMessageContaining("Linha 2, Coluna 'NOMESC': Nome inválido");
  }

  @Test
  @DisplayName("Deve lançar exceção com erro único")
  void shouldThrowExceptionWithSingleError() {
    // Given
    aggregator.addError(5, "SALAS_AULA", "Número de salas inválido");

    // When & Then
    assertThatThrownBy(() -> aggregator.throwIfErrors())
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessageContaining("Validação falhou com 1 erro(s)")
      .hasMessageContaining(
        "Linha 5, Coluna 'SALAS_AULA': Número de salas inválido"
      );
  }

  @Test
  @DisplayName("Deve retornar lista imutável de erros")
  void shouldReturnImmutableErrorList() {
    // Given
    aggregator.addError(1, "CODESC", "Erro teste");

    // When
    List<ValidationErrorAggregator.ValidationError> errors =
      aggregator.getErrors();

    // Then
    assertThatThrownBy(() ->
        errors.add(
          new ValidationErrorAggregator.ValidationError(
            2,
            "NOMESC",
            "Novo erro"
          )
        )
      )
      .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(() -> errors.remove(0))
      .isInstanceOf(UnsupportedOperationException.class);

    assertThatThrownBy(() -> errors.clear())
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Deve manter ordem de inserção dos erros")
  void shouldMaintainInsertionOrderOfErrors() {
    // Given
    aggregator.addError(3, "COLUNA_C", "Terceiro erro");
    aggregator.addError(1, "COLUNA_A", "Primeiro erro");
    aggregator.addError(2, "COLUNA_B", "Segundo erro");

    // When
    List<ValidationErrorAggregator.ValidationError> errors =
      aggregator.getErrors();

    // Then
    assertThat(errors).hasSize(3);
    assertThat(errors.get(0).columnName()).isEqualTo("COLUNA_C");
    assertThat(errors.get(1).columnName()).isEqualTo("COLUNA_A");
    assertThat(errors.get(2).columnName()).isEqualTo("COLUNA_B");
  }

  @Test
  @DisplayName("Deve formatar toString do ValidationError corretamente")
  void shouldFormatValidationErrorToStringCorrectly() {
    // Given
    ValidationErrorAggregator.ValidationError error =
      new ValidationErrorAggregator.ValidationError(
        10,
        "BIBLIOTECA",
        "Valor deve ser positivo"
      );

    // When
    String toString = error.toString();

    // Then
    assertThat(toString)
      .isEqualTo("Linha 10, Coluna 'BIBLIOTECA': Valor deve ser positivo");
  }

  @Test
  @DisplayName("Deve permitir adicionar erros com números de linha negativos")
  void shouldAllowAddingErrorsWithNegativeLineNumbers() {
    // Given & When
    aggregator.addError(-1, "HEADER", "Erro no cabeçalho");

    // Then
    assertThat(aggregator.hasErrors()).isTrue();
    ValidationErrorAggregator.ValidationError error = aggregator
      .getErrors()
      .get(0);
    assertThat(error.lineNumber()).isEqualTo(-1);
    assertThat(error.toString())
      .isEqualTo("Linha -1, Coluna 'HEADER': Erro no cabeçalho");
  }

  @Test
  @DisplayName("Deve permitir adicionar erros com valores null")
  void shouldAllowAddingErrorsWithNullValues() {
    // Given & When
    aggregator.addError(1, null, "Mensagem com coluna null");
    aggregator.addError(2, "COLUNA", null);
    aggregator.addError(3, null, null);

    // Then
    assertThat(aggregator.hasErrors()).isTrue();
    assertThat(aggregator.getErrors()).hasSize(3);

    ValidationErrorAggregator.ValidationError firstError = aggregator
      .getErrors()
      .get(0);
    assertThat(firstError.columnName()).isNull();
    assertThat(firstError.message()).isEqualTo("Mensagem com coluna null");

    ValidationErrorAggregator.ValidationError secondError = aggregator
      .getErrors()
      .get(1);
    assertThat(secondError.columnName()).isEqualTo("COLUNA");
    assertThat(secondError.message()).isNull();

    ValidationErrorAggregator.ValidationError thirdError = aggregator
      .getErrors()
      .get(2);
    assertThat(thirdError.columnName()).isNull();
    assertThat(thirdError.message()).isNull();
  }

  @Test
  @DisplayName("Deve lidar com muitos erros")
  void shouldHandleManyErrors() {
    // Given
    int numberOfErrors = 100;

    // When
    for (int i = 1; i <= numberOfErrors; i++) {
      aggregator.addError(i, "COLUNA_" + i, "Erro número " + i);
    }

    // Then
    assertThat(aggregator.hasErrors()).isTrue();
    assertThat(aggregator.getErrors()).hasSize(numberOfErrors);

    // Verifica alguns erros específicos
    assertThat(aggregator.getErrors().get(0).message())
      .isEqualTo("Erro número 1");
    assertThat(aggregator.getErrors().get(49).message())
      .isEqualTo("Erro número 50");
    assertThat(aggregator.getErrors().get(99).message())
      .isEqualTo("Erro número 100");
  }

  @Test
  @DisplayName(
    "Deve lançar exceção com mensagem formatada para múltiplos erros"
  )
  void shouldThrowExceptionWithFormattedMessageForMultipleErrors() {
    // Given
    aggregator.addError(1, "CODESC", "Código inválido");
    aggregator.addError(2, "NOMESC", "Nome muito curto");
    aggregator.addError(3, "SALAS_AULA", "Número negativo");

    // When & Then
    assertThatThrownBy(() -> aggregator.throwIfErrors())
      .isInstanceOf(AggregatedValidationException.class)
      .hasMessage(
        "Validação falhou com 3 erro(s): " +
        "Linha 1, Coluna 'CODESC': Código inválido; " +
        "Linha 2, Coluna 'NOMESC': Nome muito curto; " +
        "Linha 3, Coluna 'SALAS_AULA': Número negativo"
      );
  }
}
