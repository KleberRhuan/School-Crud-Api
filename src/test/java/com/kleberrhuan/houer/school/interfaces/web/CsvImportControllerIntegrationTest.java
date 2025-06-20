/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.config.TestSecurityConfig;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("CsvImportController Integration Tests")
class CsvImportControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve fazer upload de CSV com sucesso")
  void shouldUploadCsvSuccessfully() throws Exception {
    // Given
    String csvContent =
      """
        NOMEDEP,DE,MUN,DISTR,CODESC,NOMESC,TIPOESC,TIPOESC_DESC,CODSIT,SALAS_AULA
        REDE TESTE,DE TESTE,SAO PAULO,CENTRO,11111111,ESCOLA TESTE,1,PUBLICA,ATIVA,10
        REDE EXEMPLO,DE EXEMPLO,SAO PAULO,ZONA SUL,22222222,ESCOLA EXEMPLO,1,PUBLICA,ATIVA,8
        """;

    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test-schools.csv",
      "text/csv",
      csvContent.getBytes()
    );

    // When & Then
    mockMvc
      .perform(
        multipart("/api/v1/csv-import/upload")
          .file(file)
          .param("description", "Teste de integração")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.jobId").exists())
      .andExpect(jsonPath("$.filename").value("test-schools.csv"))
      .andExpect(jsonPath("$.status").value(ImportJobStatus.PENDING.name()));
  }

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve rejeitar CSV com formato inválido")
  void shouldRejectInvalidCsvFormat() throws Exception {
    // Given - CSV com apenas algumas colunas (deveria falhar)
    String invalidCsvContent = "CODESC,NOMESC\n1,2"; // Faltam colunas obrigatórias

    MockMultipartFile file = new MockMultipartFile(
      "file",
      "invalid.csv",
      "text/csv",
      invalidCsvContent.getBytes()
    );

    // When & Then
    mockMvc
      .perform(
        multipart("/api/v1/csv-import/upload")
          .file(file)
          .param("description", "CSV inválido")
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve listar jobs de importação com paginação")
  void shouldListImportJobsWithPagination() throws Exception {
    // When & Then
    mockMvc
      .perform(
        get("/api/v1/csv-import/jobs")
          .param("page", "0")
          .param("size", "10")
          .param("sort", "createdAt,desc")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.pageable").exists())
      .andExpect(jsonPath("$.totalElements").exists());
  }

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve filtrar jobs por status")
  void shouldFilterJobsByStatus() throws Exception {
    // When & Then
    mockMvc
      .perform(
        get("/api/v1/csv-import/jobs")
          .param("status", ImportJobStatus.PENDING.name())
          .param("page", "0")
          .param("size", "10")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve retornar erro 404 para job inexistente")
  void shouldReturn404ForNonExistentJob() throws Exception {
    // Given
    String nonExistentJobId = "123e4567-e89b-12d3-a456-426614174000";

    // When & Then
    mockMvc
      .perform(get("/api/v1/csv-import/jobs/{jobId}", nonExistentJobId))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "testuser", authorities = { "ROLE_USER" })
  @DisplayName("Deve retornar erro para arquivo vazio")
  void shouldReturnErrorForEmptyFile() throws Exception {
    // Given
    MockMultipartFile emptyFile = new MockMultipartFile(
      "file",
      "empty.csv",
      "text/csv",
      new byte[0]
    );

    // When & Then
    mockMvc
      .perform(
        multipart("/api/v1/csv-import/upload")
          .file(emptyFile)
          .param("description", "Arquivo vazio")
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve rejeitar upload sem autenticação")
  void shouldRejectUploadWithoutAuthentication() throws Exception {
    // Given
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.csv",
      "text/csv",
      "test,data".getBytes()
    );

    // When & Then
    mockMvc
      .perform(
        multipart("/api/v1/csv-import/upload")
          .file(file)
          .param("description", "Teste sem auth")
      )
      .andExpect(status().isUnauthorized());
  }
}
