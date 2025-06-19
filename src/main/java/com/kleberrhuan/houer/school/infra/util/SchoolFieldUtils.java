/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.infra.util;

import com.kleberrhuan.houer.school.domain.model.School;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SchoolFieldUtils {

  private static final Set<String> ENTITY_FIELDS = Set
    .of(School.class.getDeclaredFields())
    .stream()
    .map(Field::getName)
    .collect(Collectors.toUnmodifiableSet());

  public static boolean isSchoolField(String fieldName) {
    return fieldName != null && ENTITY_FIELDS.contains(fieldName);
  }
}
