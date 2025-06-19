/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.application.mapper;

import com.kleberrhuan.houer.csv.domain.model.CsvImportJob;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportResponseDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CsvImportMapper {
  @Mapping(target = "jobId", source = "id")
  @Mapping(
    target = "createdAt",
    expression = "java(toInstant(job.getCreatedAt()))"
  )
  CsvImportResponseDto toResponseDto(CsvImportJob job);

  default Instant toInstant(LocalDateTime localDateTime) {
    return localDateTime != null
      ? localDateTime.toInstant(ZoneOffset.UTC)
      : null;
  }
}
