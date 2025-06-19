/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.exception;

import java.util.Set;

/** Exceção específica para problemas na validação do cabeçalho CSV. */
public class HeaderValidationException extends CsvValidationException {

  private final String detailMessage;

  @Override
  public String getMessage() {
    return detailMessage;
  }

  public HeaderValidationException(
    String filename,
    Set<String> missingColumns
  ) {
    super(
      "Colunas obrigatórias ausentes no arquivo " +
      filename +
      ": " +
      missingColumns
    );
    this.detailMessage =
      String.format(
        "Colunas obrigatórias ausentes no arquivo %s: %s",
        filename,
        missingColumns
      );
  }

  public HeaderValidationException(
    String filename,
    Set<String> extraColumns,
    boolean isExtra
  ) {
    super(
      "Colunas não permitidas no arquivo " + filename + ": " + extraColumns
    );
    this.detailMessage =
      String.format(
        "Colunas não permitidas no arquivo %s: %s",
        filename,
        extraColumns
      );
  }

  public HeaderValidationException(String filename, int foundColumns) {
    super(
      "Arquivo " +
      filename +
      " tem quantidade incorreta de colunas: " +
      foundColumns
    );
    this.detailMessage =
      String.format(
        "Arquivo %s tem quantidade incorreta de colunas: %d",
        filename,
        foundColumns
      );
  }

  public HeaderValidationException(String filename, String message) {
    super("Erro no cabeçalho do arquivo " + filename + ": " + message);
    this.detailMessage =
      String.format("Erro no cabeçalho do arquivo %s: %s", filename, message);
  }
}
