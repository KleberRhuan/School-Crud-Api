/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SchoolCreateRequest", description = "Estrutura de requisição para criação de uma nova escola", implementation = SchoolCreateRequest.class, example = """
    {
      "code": 12345678,
      "schoolName": "ESCOLA MUNICIPAL NOVO HORIZONTE",
      "administrativeDependency": "Municipal",
      "stateCode": "RJ",
      "municipality": "Rio de Janeiro",
      "district": "Copacabana",
      "schoolType": 2,
      "schoolTypeDescription": "Ensino Fundamental",
      "situationCode": 1,
      "schoolCode": 87654321,
      "metrics": {
        "totalStudents": 200,
        "totalTeachers": 15,
        "totalClassrooms": 10,
        "hasLibrary": 1,
        "hasLab": 0
      }
    }
    """)
public interface SchoolCreateRequestSchema {
}