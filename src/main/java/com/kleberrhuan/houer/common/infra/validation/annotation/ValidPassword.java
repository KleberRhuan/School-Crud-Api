/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.validation.annotation;

import com.kleberrhuan.houer.common.infra.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidPassword {
  String message() default "A senha deve ter entre 8 e 24 caracteres, conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
