/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.mapper;

import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.domain.model.SchoolMetrics;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SchoolMapper {
  @Mapping(target = "code", source = "code")
  @Mapping(target = "schoolName", source = "nomeEsc")
  @Mapping(target = "administrativeDependency", source = "nomeDep")
  @Mapping(target = "stateCode", source = "de")
  @Mapping(target = "municipality", source = "mun")
  @Mapping(target = "district", source = "distr")
  @Mapping(target = "schoolType", source = "tipoEsc")
  @Mapping(target = "schoolTypeDescription", source = "tipoEscDesc")
  @Mapping(target = "situationCode", source = "codsit")
  @Mapping(target = "schoolCode", source = "codesc")
  @Mapping(
    target = "createdAt",
    source = "createdAt",
    qualifiedByName = "localDateTimeToInstant"
  )
  @Mapping(
    target = "updatedAt",
    source = "updatedAt",
    qualifiedByName = "localDateTimeToInstant"
  )
  @Mapping(target = "metrics", source = "schoolMetrics")
  SchoolDto toDto(School school);

  @Mapping(target = "schoolCode", source = "schoolCode")
  @Mapping(target = "metrics", source = "metrics")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SchoolMetricsDto metricsToDto(SchoolMetrics metrics);

  @Mapping(target = "code", ignore = true)
  @Mapping(target = "schoolMetrics", ignore = true)
  @Mapping(target = "nomeEsc", source = "schoolName")
  @Mapping(target = "nomeDep", source = "administrativeDependency")
  @Mapping(target = "de", source = "stateCode")
  @Mapping(target = "mun", source = "municipality")
  @Mapping(target = "distr", source = "district")
  @Mapping(target = "tipoEsc", source = "schoolType")
  @Mapping(target = "tipoEscDesc", source = "schoolTypeDescription")
  @Mapping(target = "codsit", source = "situationCode")
  @Mapping(target = "codesc", source = "schoolCode")
  @BeanMapping(
          nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
  )
  void updateEntity(@MappingTarget School school, SchoolUpdateRequest request);

  @Mapping(target = "nomeEsc", source = "schoolName")
  @Mapping(target = "nomeDep", source = "administrativeDependency")
  @Mapping(target = "de", source = "stateCode")
  @Mapping(target = "mun", source = "municipality")
  @Mapping(target = "distr", source = "district")
  @Mapping(target = "tipoEsc", source = "schoolType")
  @Mapping(target = "tipoEscDesc", source = "schoolTypeDescription")
  @Mapping(target = "codsit", source = "situationCode")
  @Mapping(target = "codesc", source = "schoolCode")
  @Mapping(target = "schoolMetrics", ignore = true)
  @Mapping(target = "code", ignore = true)
  School toEntity(SchoolCreateRequest request);

  @Named("localDateTimeToInstant")
  default Instant localDateTimeToInstant(LocalDateTime localDateTime) {
    return localDateTime != null
      ? localDateTime.toInstant(ZoneOffset.UTC)
      : null;
  }
}
