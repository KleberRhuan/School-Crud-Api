/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.exception.translator;

import static com.kleberrhuan.houer.common.domain.exception.constraint.ConstraintType.*;

import com.kleberrhuan.houer.common.application.port.persistence.ConstraintParser;
import com.kleberrhuan.houer.common.domain.exception.constraint.*;
import com.kleberrhuan.houer.common.interfaces.dto.error.ViolationInfo;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConstraintViolationTranslator
  extends SQLErrorCodeSQLExceptionTranslator {

  private final List<ConstraintParser> parsers;
  private static final Map<ConstraintType, Function<ViolationInfo, RuntimeException>> EXCEPTION_MAP;

  static {
    EnumMap<ConstraintType, Function<ViolationInfo, RuntimeException>> tmp =
      new EnumMap<>(ConstraintType.class);

    tmp.put(
      UNIQUE,
      v -> new UniqueConstraintViolationException(v.column(), v.value())
    );
    tmp.put(NOT_NULL, v -> new NotNullConstraintViolationException(v.column()));
    tmp.put(
      FOREIGN_KEY,
      v -> new ForeignKeyConstraintViolationException(v.constraint())
    );
    tmp.put(CHECK, v -> new CheckConstraintViolationException(v.constraint()));
    tmp.put(
      EXCLUSION,
      v -> new ExclusionConstraintViolationException(v.constraint())
    );

    EXCEPTION_MAP = Map.copyOf(tmp);
  }

  public RuntimeException translate(
    @NonNull String task,
    @NonNull SQLException ex
  ) {
    Optional<ViolationInfo> violation = parsers
      .stream()
      .map(p -> p.parse(ex))
      .flatMap(Optional::stream)
      .findFirst();

    if (violation.isPresent()) {
      ViolationInfo v = violation.get();
      Function<ViolationInfo, RuntimeException> factory = EXCEPTION_MAP.get(
        v.type()
      );
      if (factory != null) {
        return factory.apply(v);
      }
    }

    return super.translate(task, null, ex);
  }
}
