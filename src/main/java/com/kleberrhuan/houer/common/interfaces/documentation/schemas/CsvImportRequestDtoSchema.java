/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CsvImportRequestDto", description = "Estrutura de requisição para importação de arquivo CSV contendo dados de escolas", implementation = CsvImportRequestDto.class, example = """
    {
      "file": "(arquivo CSV binário)",
      "description": "Importação de dados das escolas do estado de São Paulo - Janeiro 2025"
    }
    """)
public interface CsvImportRequestDtoSchema {
}