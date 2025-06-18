/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SchoolDto", description = "Estrutura de resposta com dados completos da escola, incluindo métricas", implementation = SchoolDto.class, example = """
    {
      "code": 12345678,
      "schoolName": "ESCOLA MUNICIPAL EXEMPLO",
      "administrativeDependency": "Municipal",
      "stateCode": "SP",
      "municipality": "São Paulo",
      "district": "Centro",
      "schoolType": 1,
      "schoolTypeDescription": "Educação Infantil",
      "situationCode": 1,
      "schoolCode": 87654321,
      "createdAt": "2025-01-15T10:30:00Z",
      "updatedAt": "2025-01-15T10:30:00Z",
      "metrics": {
        "schoolCode": 12345678,
        "metrics": {
          "totalStudents": 150,
          "totalTeachers": 12,
          "totalClassrooms": 8
        },
        "createdAt": "2025-01-15T10:30:00Z",
        "updatedAt": "2025-01-15T10:30:00Z"
      }
    }
    """)
public interface SchoolDtoSchema {
}