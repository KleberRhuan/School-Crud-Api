/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "CsvImportResponseDto",
  description = "Estrutura de resposta com informações detalhadas sobre o job de importação CSV",
  implementation = CsvImportResponseDto.class,
  example = """
    {
      "jobId": "123e4567-e89b-12d3-a456-426614174000",
      "filename": "escolas_sp_2025.csv",
      "status": "COMPLETED",
      "totalRecords": 1500,
      "processedRecords": 1485,
      "errorRecords": 15,
      "errorMessage": null,
      "startedAt": "2025-01-15T10:30:00Z",
      "finishedAt": "2025-01-15T10:35:30Z",
      "createdAt": "2025-01-15T10:29:45Z"
    }
    """
)
public interface CsvImportResponseDtoSchema {}
