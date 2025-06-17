/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.validation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.PasswordData;
import org.passay.RuleResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordValidator")
class PasswordValidatorTest {

  @Mock
  org.passay.PasswordValidator passay;

  @Mock
  ConstraintValidatorContext ctx;

  PasswordValidator validator;

  void init() {
    validator = new PasswordValidator(passay);
  }

  @Nested
  class IsValid {

    @Test
    @DisplayName("retorna true para senha válida")
    void shouldReturnTrueWhenValid() {
      init();
      RuleResult result = new RuleResult();
      result.setValid(true);
      when(passay.validate(any(PasswordData.class))).thenReturn(result);

      boolean ok = validator.isValid("Password@123", ctx);
      assertThat(ok).isTrue();
      verify(ctx, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("retorna false para senha inválida e popula mensagem")
    void shouldReturnFalseWhenInvalid() {
      init();
      RuleResult result = new RuleResult();
      result.setValid(false);
      when(passay.validate(any(PasswordData.class))).thenReturn(result);
      when(passay.getMessages(result)).thenReturn(java.util.List.of("erro"));

      ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
        ConstraintValidatorContext.ConstraintViolationBuilder.class
      );
      when(ctx.buildConstraintViolationWithTemplate(anyString()))
        .thenReturn(builder);

      boolean ok = validator.isValid("bad", ctx);
      assertThat(ok).isFalse();
      verify(ctx).disableDefaultConstraintViolation();
      verify(builder).addConstraintViolation();
    }
  }
}
