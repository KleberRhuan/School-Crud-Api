/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.storage;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.csv.infra.properties.CsvStorageProperties;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("LocalStorageService Tests")
class LocalStorageServiceTest {

  @TempDir
  Path tempDir;

  private LocalStorageService storageService;
  private CsvStorageProperties properties;

  @BeforeEach
  void setUp() {
    properties = new CsvStorageProperties();
    properties.setBaseDir(tempDir.toString());
    properties.setCreateDirectories(true);
    properties.setDeleteAfterProcessing(true);

    storageService = new LocalStorageService(properties);
  }

  @Test
  @DisplayName("Deve armazenar arquivo e retornar URI válida")
  void shouldStoreFileAndReturnValidUri() {
    // Given
    String content = "CODESC;NOMESC\n12345;Escola Teste";
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "escolas.csv",
      "text/csv",
      content.getBytes()
    );
    UUID jobId = UUID.randomUUID();

    // When
    URI uri = storageService.store(file, jobId);

    // Then
    assertThat(uri).isNotNull();
    assertThat(uri.getScheme()).isEqualTo("file");
    assertThat(storageService.exists(uri)).isTrue();

    // Verificar se o arquivo foi criado
    Path filePath = Paths.get(uri);
    assertThat(Files.exists(filePath)).isTrue();
    assertThat(filePath.getFileName().toString()).contains("escolas");
    assertThat(filePath.getFileName().toString()).contains(jobId.toString());
  }

  @Test
  @DisplayName("Deve ler arquivo armazenado via InputStream")
  void shouldReadStoredFileViaInputStream() throws Exception {
    // Given
    String content = "CODESC;NOMESC\n12345;Escola Teste";
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "escolas.csv",
      "text/csv",
      content.getBytes()
    );
    UUID jobId = UUID.randomUUID();

    // When
    URI uri = storageService.store(file, jobId);

    try (InputStream inputStream = storageService.openInputStream(uri)) {
      String readContent = new String(inputStream.readAllBytes());

      // Then
      assertThat(readContent).isEqualTo(content);
    }
  }

  @Test
  @DisplayName("Deve remover arquivo quando chamado delete")
  void shouldRemoveFileWhenDeleteCalled() {
    // Given
    String content = "CODESC;NOMESC\n12345;Escola Teste";
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "escolas.csv",
      "text/csv",
      content.getBytes()
    );
    UUID jobId = UUID.randomUUID();

    // When
    URI uri = storageService.store(file, jobId);
    assertThat(storageService.exists(uri)).isTrue();

    storageService.delete(uri);

    // Then
    assertThat(storageService.exists(uri)).isFalse();
  }

  @Test
  @DisplayName("Deve criar diretórios automaticamente quando configurado")
  void shouldCreateDirectoriesAutomaticallyWhenConfigured() {
    // Given
    properties.setCreateDirectories(true);
    String content = "CODESC;NOMESC\n12345;Escola Teste";
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "escolas.csv",
      "text/csv",
      content.getBytes()
    );
    UUID jobId = UUID.randomUUID();

    // When
    URI uri = storageService.store(file, jobId);

    // Then
    Path filePath = Paths.get(uri);
    assertThat(Files.exists(filePath.getParent())).isTrue();
    assertThat(Files.exists(filePath)).isTrue();
  }

  @Test
  @DisplayName("Deve sanitizar nomes de arquivo inválidos")
  void shouldSanitizeInvalidFilenames() {
    // Given
    String content = "CODESC;NOMESC\n12345;Escola Teste";
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "arquivo com espaços & caracteres @especiais!.csv",
      "text/csv",
      content.getBytes()
    );
    UUID jobId = UUID.randomUUID();

    // When
    URI uri = storageService.store(file, jobId);

    // Then
    Path filePath = Paths.get(uri);
    String filename = filePath.getFileName().toString();
    assertThat(filename).matches("[a-zA-Z0-9._-]+\\.csv");
    assertThat(filename).contains(jobId.toString());
  }
}
