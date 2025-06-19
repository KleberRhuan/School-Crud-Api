/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "SchoolUpdateRequest",
  description = "Estrutura de requisição para atualização dos dados de uma escola. Todos os campos são opcionais.",
  implementation = SchoolUpdateRequest.class,
  example = """
    {
      "schoolName": "ESCOLA MUNICIPAL NOVO HORIZONTE ATUALIZADA",
      "administrativeDependency": "Estadual",
      "municipality": "Niterói",
      "metrics": {
        "totalStudents": 220,
        "totalTeachers": 18
      }
    }
    """
)
public interface SchoolUpdateRequestSchema {}
