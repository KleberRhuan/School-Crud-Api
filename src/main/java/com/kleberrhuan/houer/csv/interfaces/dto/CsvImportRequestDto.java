/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/** DTO para requisição de importação de arquivo CSV de escolas. */
public record CsvImportRequestDto(
  @NotNull(message = "Arquivo CSV é obrigatório") MultipartFile file
) {}
