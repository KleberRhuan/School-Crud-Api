/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CommonSpecs {

  public <E> Specification<E> isNotId(Long id) {
    return id == null
      ? null
      : (root, query, cb) -> cb.notEqual(root.get("id"), id);
  }

  public <E> Specification<E> likeUnaccent(
    Function<Root<E>, Path<String>> pathFn,
    String term
  ) {
    return (r, q, cb) -> {
      Path<String> path = pathFn.apply(r);
      var col = cb.function("unaccent", String.class, cb.lower(path));
      var pat = cb.function(
        "unaccent",
        String.class,
        cb.literal('%' + term.toLowerCase() + '%')
      );
      return cb.like(col, pat);
    };
  }

  public <E> Specification<E> onlyActive(Boolean flag) {
    return Boolean.TRUE.equals(flag)
      ? (r, q, cb) -> cb.isTrue(r.get("active"))
      : null;
  }
}
