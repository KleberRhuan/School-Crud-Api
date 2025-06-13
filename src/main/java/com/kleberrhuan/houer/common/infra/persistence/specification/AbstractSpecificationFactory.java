/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.specification;

import com.kleberrhuan.houer.common.domain.exception.InvalidFilterParamValueException;
import com.kleberrhuan.houer.common.interfaces.dto.filter.FilterSpec;
import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

@RequiredArgsConstructor
public abstract class AbstractSpecificationFactory<E, F extends FilterSpec>
  implements SpecificationFactory<E, F> {

  private final Map<String, Class<?>> fieldCache = new ConcurrentHashMap<>();
  private final Class<E> type;
  private final ConversionService conversionService;

  @Override
  public Specification<E> byFilter(F f, Long me) {
    if (!f.hasFilters()) return null;

    Specification<E> base = dynamicParamsSpec(f.params());
    Specification<E> extra = extraSpecs(f, me);

    return Specification.allOf(base, extra);
  }

  /* ---------- gera spec a partir de params -------------------- */
  private Specification<E> dynamicParamsSpec(Map<String, String> params) {
    if (params == null || params.isEmpty()) return null;

    return params
      .entrySet()
      .stream()
      .map(e -> buildTermSpec(e.getKey(), e.getValue()))
      .filter(Objects::nonNull)
      .reduce(Specification.allOf(), Specification::and);
  }

  /* helpers ---------------------------------------------------- */
  private Specification<E> buildTermSpec(String field, @Nullable String raw) {
    if (raw == null || raw.isBlank()) return null;

    Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(
      getFieldType(field)
    );

    if (
      String.class.equals(fieldType) ||
      Character.class.equals(fieldType) ||
      fieldType.isEnum()
    ) {
      return CommonSpecs.likeUnaccent(r -> r.get(field), raw.trim());
    }

    Object converted;
    try {
      converted = conversionService.convert(raw.trim(), fieldType);
    } catch (ConversionFailedException ex) {
      throw new InvalidFilterParamValueException(field);
    }

    return (r, q, cb) -> cb.equal(r.get(field), converted);
  }

  private Class<?> getFieldType(String fieldName) {
    return fieldCache.computeIfAbsent(
      fieldName,
      fn -> {
        Field f = ReflectionUtils.findField(type, fn);
        if (f == null) throw new IllegalArgumentException(
          "Unknown field: " + fn
        );
        return f.getType();
      }
    );
  }

  /** ganchos que subclasses implementam */
  protected abstract Specification<E> extraSpecs(F filter, Long me);
}
