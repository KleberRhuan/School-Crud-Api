/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.schemas;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
  name = "ErrorResponse",
  description = "Estrutura padrão de resposta de erro da API",
  implementation = ApiErrorResponse.class,
  example = "{\n  \"status\": 400,\n  \"type\": \"/problema/dados-invalidos\",\n  \"title\": \"Dados inválidos\",\n  \"detail\": \"O campo 'page' deve ser positivo.\",\n  \"userMessage\": \"Alguns campos estão com valores inválidos. Corrija-os e tente novamente.\",\n  \"timestamp\": \"2025-05-18T15:36:27Z\",\n  \"violations\": [ { \"name\": \"page\", \"message\": \"deve ser maior ou igual a 1\" } ]\n}"
)
public interface ErrorResponseSchema {}
