/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.common.application.port.persistence.ConstraintParser;
import com.kleberrhuan.houer.common.infra.exception.translator.ConstraintViolationTranslator;
import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstraintViolationTranslatorTest {

  @Test
  @DisplayName(
    "deve traduzir UNIQUE constraint para UniqueConstraintViolationException"
  )
  void shouldTranslateUnique() {
    ConstraintParser parser = ex ->
      java.util.Optional.of(ViolationInfo.unique("email", "dup@gmail.com"));
    ConstraintViolationTranslator t = new ConstraintViolationTranslator(
      List.of(parser)
    );

    RuntimeException out = t.translate("insert", new SQLException());

    assertThat(out)
      .isInstanceOf(
        com.kleberrhuan.houer.common.domain.exception.constraint.UniqueConstraintViolationException.class
      );
  }

  @Test
  @DisplayName("deve delegar para super quando nenhum parser corresponde")
  void shouldFallbackToDefault() {
    ConstraintParser parser = ex -> java.util.Optional.empty();
    ConstraintViolationTranslator t = new ConstraintViolationTranslator(
      List.of(parser)
    );

    RuntimeException out = t.translate("task", new SQLException("generic"));

    // quando não há correspondência, tradutor delega ao pai que pode
    // retornar null caso não reconheça o erro
    assertThat(out).isNull();
  }
}
