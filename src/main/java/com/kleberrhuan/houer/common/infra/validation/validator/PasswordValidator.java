/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.validation.validator;

import com.kleberrhuan.houer.common.infra.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.passay.*;

@RequiredArgsConstructor
public class PasswordValidator
  implements ConstraintValidator<ValidPassword, String> {

  private final org.passay.PasswordValidator validator;

  @Override
  public boolean isValid(String password, ConstraintValidatorContext ctx) {
    RuleResult result = validator.validate(new PasswordData(password));
    if (result.isValid()) return true;

    ctx.disableDefaultConstraintViolation();
    String msg = String.join(" ", validator.getMessages(result));
    ctx.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
    return false;
  }
}
