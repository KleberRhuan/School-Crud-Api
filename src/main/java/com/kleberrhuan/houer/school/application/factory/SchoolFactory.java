/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.factory;

import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class SchoolFactory {

  public School createFromRequest(SchoolCreateRequest request) {
    return School
      .builder()
      .code(request.code())
      .nomeEsc(request.schoolName())
      .nomeDep(request.administrativeDependency())
      .de(request.stateCode())
      .mun(request.municipality())
      .distr(request.district())
      .tipoEsc(request.schoolType())
      .tipoEscDesc(request.schoolTypeDescription())
      .codsit(request.situationCode())
      .codesc(request.schoolCode())
      .build();
  }

  public School updateFromRequest(
    School existingSchool,
    SchoolUpdateRequest request
  ) {
    return School
      .builder()
      .code(existingSchool.getCode())
      .nomeEsc(
        request.schoolName() != null
          ? request.schoolName()
          : existingSchool.getNomeEsc()
      )
      .nomeDep(
        request.administrativeDependency() != null
          ? request.administrativeDependency()
          : existingSchool.getNomeDep()
      )
      .de(
        request.stateCode() != null
          ? request.stateCode()
          : existingSchool.getDe()
      )
      .mun(
        request.municipality() != null
          ? request.municipality()
          : existingSchool.getMun()
      )
      .distr(
        request.district() != null
          ? request.district()
          : existingSchool.getDistr()
      )
      .tipoEsc(
        request.schoolType() != null
          ? request.schoolType()
          : existingSchool.getTipoEsc()
      )
      .tipoEscDesc(
        request.schoolTypeDescription() != null
          ? request.schoolTypeDescription()
          : existingSchool.getTipoEscDesc()
      )
      .codsit(
        request.situationCode() != null
          ? request.situationCode()
          : existingSchool.getCodsit()
      )
      .codesc(
        request.schoolCode() != null
          ? request.schoolCode()
          : existingSchool.getCodesc()
      )
      .build();
  }
}
