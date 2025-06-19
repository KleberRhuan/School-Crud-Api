/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "SchoolMetricsDto",
  description = "Estrutura de resposta com métricas detalhadas da escola em formato JSON",
  implementation = SchoolMetricsDto.class,
  example = """
    {
      "schoolCode": 12345678,
      "metrics": {
        "totalStudents": 350,
        "totalTeachers": 25,
        "totalClassrooms": 15,
        "hasLibrary": 1,
        "hasLab": 1,
        "hasCompLab": 1,
        "hasSportsField": 0,
        "hasKitchen": 1,
        "totalBooks": 2500,
        "internetAccess": 1
      },
      "createdAt": "2025-01-15T10:30:00Z",
      "updatedAt": "2025-01-15T15:45:00Z"
    }
    """
)
public interface SchoolMetricsDtoSchema {}
