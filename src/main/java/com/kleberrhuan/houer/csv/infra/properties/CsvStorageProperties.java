/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Propriedades de configuração para armazenamento de arquivos CSV. Atualmente suporta apenas armazenamento local. */
@Data
@Component
@ConfigurationProperties(prefix = "app.csv.storage")
public class CsvStorageProperties {

  /** Diretório base para armazenamento local. Se não especificado, usa um diretório temporário. */
  private String baseDir = "uploads/csv";

  /** Se deve criar diretórios automaticamente. */
  private boolean createDirectories = true;

  /** Se deve remover arquivos após processamento. */
  private boolean deleteAfterProcessing = true;
}
