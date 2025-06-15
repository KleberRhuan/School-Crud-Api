/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.kleberrhuan.houer.common.infra.properties.PasswordValidationProperties;
import com.kleberrhuan.houer.common.infra.validation.validator.factory.PasswordRulesFactory;
import java.util.List;
import java.util.Locale;
import org.passay.MessageResolver;
import org.passay.Rule;
import org.passay.spring.SpringMessageResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

@Configuration
public class PasswordConfig {

  @Bean
  public org.passay.PasswordValidator passayValidator(
    PasswordValidationProperties props,
    MessageSource messageSource
  ) {
    Locale locale = LocaleContextHolder.getLocale(); // dinâmico

    List<Rule> rules = PasswordRulesFactory.build(props);

    MessageResolver resolver = new SpringMessageResolver(messageSource, locale);
    return new org.passay.PasswordValidator(resolver, rules);
  }
}
