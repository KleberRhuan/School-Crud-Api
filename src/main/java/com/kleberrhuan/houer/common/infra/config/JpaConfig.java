/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.infra.exception.translator.ConstraintViolationTranslator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

@Configuration
@RequiredArgsConstructor
public class JpaConfig {

  private final ConstraintViolationTranslator translator;

  @Bean
  public JpaDialect jpaDialect() {
    return new HibernateJpaDialect() {
      @Override
      public DataAccessException translateExceptionIfPossible(
        @NonNull RuntimeException ex
      ) {
        if (
          ex instanceof org.hibernate.exception.ConstraintViolationException cve
        ) {
          RuntimeException domain = translator.translate(
            "JPA",
            cve.getSQLException()
          );
          if (domain instanceof ApiException apiEx) {
            throw apiEx;
          }
        }
        return super.translateExceptionIfPossible(ex);
      }
    };
  }
}
