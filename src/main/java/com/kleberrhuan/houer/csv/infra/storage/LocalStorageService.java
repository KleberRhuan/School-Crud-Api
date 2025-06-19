/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.storage;

import com.kleberrhuan.houer.csv.application.port.StorageService;
import com.kleberrhuan.houer.csv.infra.exception.CsvProcessingException;
import com.kleberrhuan.houer.csv.infra.properties.CsvStorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementação de armazenamento local para arquivos CSV. Salva arquivos no filesystem local em um diretório
 * configurável.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocalStorageService implements StorageService {

  private static final DateTimeFormatter DIR_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy/MM/dd");
  private static final String FILE_EXTENSION = ".csv";

  private final CsvStorageProperties properties;

  @Override
  public URI store(MultipartFile file, UUID jobId) {
    try {
      Path targetPath = buildFilePath(jobId, file.getOriginalFilename());
      ensureDirectoryExists(targetPath.getParent());

      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(
          inputStream,
          targetPath,
          StandardCopyOption.REPLACE_EXISTING
        );
      }

      log.info(
        "Arquivo CSV armazenado localmente: {} -> {}",
        file.getOriginalFilename(),
        targetPath
      );

      return targetPath.toUri();
    } catch (IOException e) {
      throw new CsvProcessingException(
        "Erro ao armazenar arquivo CSV: " + file.getOriginalFilename(),
        e
      );
    }
  }

  @Override
  public void delete(URI uri) {
    try {
      Path path = Paths.get(uri);
      if (Files.exists(path)) {
        Files.delete(path);
        log.debug("Arquivo removido: {}", path);
      }
    } catch (IOException e) {
      log.warn("Erro ao remover arquivo: {}", uri, e);
    }
  }

  @Override
  public boolean exists(URI uri) {
    try {
      Path path = Paths.get(uri);
      return Files.exists(path);
    } catch (Exception e) {
      log.warn("Erro ao verificar existência do arquivo: {}", uri, e);
      return false;
    }
  }

  @Override
  public InputStream openInputStream(URI uri) {
    try {
      Path path = Paths.get(uri);
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new CsvProcessingException(
        "Erro ao abrir arquivo para leitura: " + uri,
        e
      );
    }
  }

  private Path buildFilePath(UUID jobId, String originalFilename) {
    String baseDir = properties.getBaseDir();
    Path basePath = Paths.get(baseDir).isAbsolute()
      ? Paths.get(baseDir)
      : Paths.get(System.getProperty("user.dir"), baseDir);

    String dateDir = LocalDateTime.now().format(DIR_FORMATTER);
    String filename = buildFilename(jobId, originalFilename);

    return basePath.resolve(dateDir).resolve(filename);
  }

  private String buildFilename(UUID jobId, String originalFilename) {
    String sanitized = sanitizeFilename(originalFilename);
    String nameWithoutExt = removeExtension(sanitized);
    return String.format("%s_%s%s", nameWithoutExt, jobId, FILE_EXTENSION);
  }

  private String sanitizeFilename(String filename) {
    if (filename == null) {
      return "unknown";
    }
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String removeExtension(String filename) {
    int lastDot = filename.lastIndexOf('.');
    return lastDot > 0 ? filename.substring(0, lastDot) : filename;
  }

  private void ensureDirectoryExists(Path directory) throws IOException {
    if (properties.isCreateDirectories() && !Files.exists(directory)) {
      Files.createDirectories(directory);
      log.debug("Diretório criado: {}", directory);
    }
  }
}
