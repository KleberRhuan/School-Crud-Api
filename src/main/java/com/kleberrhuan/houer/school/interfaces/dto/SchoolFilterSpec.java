/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.dto;

import com.kleberrhuan.houer.common.interfaces.dto.filter.FilterSpec;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record SchoolFilterSpec(
  Boolean onlyActive,
  @Size(max = 200) String name,
  @Size(max = 100) String municipalityName,
  @Size(max = 2) String stateAbbreviation,
  Integer operationalStatus,
  Integer dependencyType,
  Short schoolType,
  @Size(max = 100) String administrativeRegion,
  Map<String, String> customParams,
  @Size(
    min = 1,
    max = 50,
    message = "Dependência administrativa deve ter entre 1 e 50 caracteres"
  )
  String administrativeDependence,
  @Size(
    min = 1,
    max = 50,
    message = "Localização deve ter entre 1 e 50 caracteres"
  )
  String location,
  @Size(
    min = 1,
    max = 20,
    message = "Situação deve ter entre 1 e 20 caracteres"
  )
  String situation
)
  implements FilterSpec {
  public SchoolFilterSpec {
    name = normalizeString(name);
    municipalityName = normalizeString(municipalityName);
    stateAbbreviation = normalizeString(stateAbbreviation);
    administrativeRegion = normalizeString(administrativeRegion);
    administrativeDependence = normalizeString(administrativeDependence);
    location = normalizeString(location);
    situation = normalizeString(situation);
  }

  private static String normalizeString(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim().toLowerCase();
  }

  @Override
  public Map<String, String> params() {
    if (customParams != null && !customParams.isEmpty()) {
      return customParams;
    }

    Map<String, String> dynamicParams = new java.util.HashMap<>();

    if (name != null) {
      dynamicParams.put("nomeEsc", name);
    }
    if (municipalityName != null) {
      dynamicParams.put("mun", municipalityName);
    }
    if (stateAbbreviation != null) {
      dynamicParams.put("de", stateAbbreviation);
    }
    if (operationalStatus != null) {
      dynamicParams.put("codsit", operationalStatus.toString());
    }
    if (dependencyType != null) {
      dynamicParams.put("tipoEsc", dependencyType.toString());
    }
    if (schoolType != null) {
      dynamicParams.put("tipoEsc", schoolType.toString());
    }
    if (administrativeRegion != null) {
      dynamicParams.put("nomeDep", administrativeRegion);
    }
    if (administrativeDependence != null) {
      dynamicParams.put("nomeDep", administrativeDependence);
    }
    if (location != null) {
      dynamicParams.put("mun", location);
    }
    if (situation != null) {
      dynamicParams.put("codsit", situation);
    }

    return dynamicParams;
  }
}
