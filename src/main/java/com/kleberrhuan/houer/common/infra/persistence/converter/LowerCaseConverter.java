/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LowerCaseConverter implements AttributeConverter<String, String> {

  @Override
  public String convertToDatabaseColumn(String attr) {
    return attr == null ? null : attr.trim().toLowerCase();
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    return dbData;
  }
}
