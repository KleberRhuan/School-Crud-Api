/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.validator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.csv.application.service.CsvColumnMetadataService;
import com.kleberrhuan.houer.csv.application.validator.rule.MandatoryRule;
import com.kleberrhuan.houer.csv.application.validator.rule.NumericRule;
import com.kleberrhuan.houer.csv.domain.exception.RowValidationException;
import com.kleberrhuan.houer.csv.domain.model.RowContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeRowValidator Tests")
class CompositeRowValidatorTest {

  @Mock
  private CsvColumnMetadataService metadataService;

  private CompositeRowValidator validator;
  private RowContext context;
  private MandatoryRule rule1;
  private NumericRule rule2;

  @BeforeEach
  void setUp() {
    rule1 = spy(new MandatoryRule(metadataService));
    rule2 = spy(new NumericRule(metadataService));
    validator = new CompositeRowValidator(List.of(rule1, rule2));
    context =
      new RowContext(
        new String[] { "COL1" },
        new String[] { "VAL1" },
        1,
        "test.csv"
      );
  }

  @Test
  @DisplayName("Deve executar todas as regras quando nenhuma lança exceção")
  void shouldExecuteAllRulesWhenNoExceptionThrown() {
    // Given - regras não lançam exceção
    doNothing().when(rule1).validate(any());
    doNothing().when(rule2).validate(any());

    // When
    assertThatNoException().isThrownBy(() -> validator.validate(context));

    // Then
    verify(rule1).validate(context);
    verify(rule2).validate(context);
  }

  @Test
  @DisplayName(
    "Deve propagar exceção e não continuar após a primeira regra falhar"
  )
  void shouldPropagateExceptionAndStopOnFirstFailure() {
    // Given - primeira regra lança exceção, segunda não deve ser chamada
    doThrow(new RowValidationException("test.csv", 1, "Erro na regra 1"))
      .when(rule1)
      .validate(any());

    // When & Then
    assertThatThrownBy(() -> validator.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Erro na regra 1");

    verify(rule1).validate(context);
    verifyNoInteractions(rule2);
  }

  @Test
  @DisplayName("Deve parar na segunda regra se ela falhar")
  void shouldStopAtSecondRuleIfItFails() {
    // Given
    doNothing().when(rule1).validate(any());
    doThrow(new RowValidationException("test.csv", 1, "Erro na regra 2"))
      .when(rule2)
      .validate(any());

    // When & Then
    assertThatThrownBy(() -> validator.validate(context))
      .isInstanceOf(RowValidationException.class)
      .hasMessageContaining("Erro na regra 2");

    verify(rule1).validate(context);
    verify(rule2).validate(context);
  }
}
