/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence;

import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.infra.exception.translator.ConstraintViolationTranslator;
import java.io.Serial;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomHibernateJpaDialect extends HibernateJpaDialect {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient ConstraintViolationTranslator translator;

  @Override
  public DataAccessException translateExceptionIfPossible(
    @NonNull RuntimeException ex
  ) {
    if (ex instanceof ConstraintViolationException cve) {
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

  @Serial
  private void writeObject(java.io.ObjectOutputStream oos)
    throws java.io.IOException {
    oos.defaultWriteObject();
  }

  @Serial
  private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
    ois.defaultReadObject();
  }
}
