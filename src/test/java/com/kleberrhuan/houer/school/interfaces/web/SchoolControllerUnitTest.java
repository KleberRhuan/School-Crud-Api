/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kleberrhuan.houer.config.TestBeansConfig;
import com.kleberrhuan.houer.school.application.service.MetricCatalogService;
import com.kleberrhuan.houer.school.application.service.SchoolQueryService;
import com.kleberrhuan.houer.school.application.service.SchoolService;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SchoolController.class)
@TestPropertySource(properties = { "management.metrics.enabled=false" })
@Import({ TestBeansConfig.class })
@DisplayName("SchoolController - Testes Unitários")
class SchoolControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private SchoolService schoolService;

  @MockBean
  private SchoolQueryService schoolQueryService;

  @MockBean
  private MetricCatalogService metricCatalogService;

  private SchoolDto schoolDto;
  private SchoolCreateRequest createRequest;
  private SchoolUpdateRequest updateRequest;
  private SchoolMetricsDto metricsDto;

  @BeforeEach
  void setUp() {
    // Setup dados de teste
    Map<String, Long> metrics = Map.of(
      "SALAS_AULA",
      15L,
      "BIBLIOTECA",
      1L,
      "QUADRA_COBERTA",
      1L,
      "LAB_INFO",
      1L,
      "REFEITORIO",
      1L
    );

    metricsDto =
      new SchoolMetricsDto(12345678L, metrics, Instant.now(), Instant.now());

    schoolDto =
      new SchoolDto(
        12345678L,
        "Escola Municipal Vila Nova",
        "Municipal",
        "SP",
        "São Paulo",
        "Vila Nova",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        Instant.now(),
        Instant.now(),
        metricsDto
      );

    createRequest =
      new SchoolCreateRequest(
        12345678L,
        "Escola Municipal Vila Nova",
        "Municipal",
        "SP",
        "São Paulo",
        "Vila Nova",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        metrics
      );

    updateRequest =
      new SchoolUpdateRequest(
        "Escola Municipal Vila Nova - Atualizada",
        "Municipal",
        "SP",
        "São Paulo",
        "Vila Nova Conceição",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        Map.of("SALAS_AULA", 18L, "PLAYGROUND", 1L)
      );
  }

  @Nested
  @DisplayName("Criação de Escolas")
  class CreateSchool {

    @Test
    @DisplayName("Deve criar escola com sucesso quando dados válidos")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSchoolSuccessfully() throws Exception {
      // Given
      when(schoolService.create(any(SchoolCreateRequest.class)))
        .thenReturn(schoolDto);

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value(12345678L))
        .andExpect(jsonPath("$.schoolName").value("Escola Municipal Vila Nova"))
        .andExpect(jsonPath("$.administrativeDependency").value("Municipal"))
        .andExpect(jsonPath("$.stateCode").value("SP"))
        .andExpect(jsonPath("$.municipality").value("São Paulo"))
        .andExpect(jsonPath("$.district").value("Vila Nova"))
        .andExpect(jsonPath("$.metrics").exists())
        .andExpect(jsonPath("$.metrics.metrics.SALAS_AULA").value(15))
        .andExpect(jsonPath("$.metrics.metrics.BIBLIOTECA").value(1));
    }

    @Test
    @DisplayName("Deve retornar 400 quando dados inválidos")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenInvalidData() throws Exception {
      // Given - Request com dados inválidos
      SchoolCreateRequest invalidRequest = new SchoolCreateRequest(
        -1L, // Código inválido
        "", // Nome vazio
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
      );

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 quando escola já existe")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenSchoolAlreadyExists() throws Exception {
      // Given
      when(schoolService.create(any(SchoolCreateRequest.class)))
        .thenThrow(new IllegalArgumentException("Escola já existe"));

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 403 quando não é admin")
    @WithMockUser(roles = "CLIENT")
    void shouldReturn403WhenNotAdmin() throws Exception {
      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve criar escola com métricas vazias")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSchoolWithEmptyMetrics() throws Exception {
      // Given
      SchoolCreateRequest requestWithoutMetrics = new SchoolCreateRequest(
        12345679L,
        "Escola Sem Métricas",
        "Municipal",
        "SP",
        "São Paulo",
        "Centro",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345679L,
        Map.of() // Métricas vazias
      );

      SchoolDto schoolWithoutMetrics = new SchoolDto(
        12345679L,
        "Escola Sem Métricas",
        "Municipal",
        "SP",
        "São Paulo",
        "Centro",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345679L,
        Instant.now(),
        Instant.now(),
        new SchoolMetricsDto(12345679L, Map.of(), Instant.now(), Instant.now())
      );

      when(schoolService.create(any(SchoolCreateRequest.class)))
        .thenReturn(schoolWithoutMetrics);

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestWithoutMetrics))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(12345679L))
        .andExpect(jsonPath("$.schoolName").value("Escola Sem Métricas"))
        .andExpect(jsonPath("$.metrics").exists());
    }
  }

  @Nested
  @DisplayName("Atualização de Escolas")
  class UpdateSchool {

    @Test
    @DisplayName("Deve atualizar escola com sucesso")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateSchoolSuccessfully() throws Exception {
      // Given
      SchoolDto updatedSchool = new SchoolDto(
        12345678L,
        "Escola Municipal Vila Nova - Atualizada",
        "Municipal",
        "SP",
        "São Paulo",
        "Vila Nova Conceição",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        Instant.now(),
        Instant.now(),
        new SchoolMetricsDto(
          12345678L,
          Map.of("SALAS_AULA", 18L, "PLAYGROUND", 1L),
          Instant.now(),
          Instant.now()
        )
      );

      when(schoolService.update(eq(12345678L), any(SchoolUpdateRequest.class)))
        .thenReturn(updatedSchool);

      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/12345678")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value(12345678L))
        .andExpect(
          jsonPath("$.schoolName")
            .value("Escola Municipal Vila Nova - Atualizada")
        )
        .andExpect(jsonPath("$.district").value("Vila Nova Conceição"))
        .andExpect(jsonPath("$.metrics.metrics.SALAS_AULA").value(18))
        .andExpect(jsonPath("$.metrics.metrics.PLAYGROUND").value(1));
    }

    @Test
    @DisplayName("Deve atualizar apenas métricas")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOnlyMetrics() throws Exception {
      // Given
      SchoolUpdateRequest metricsOnlyRequest = new SchoolUpdateRequest(
        null, // Nome não alterado
        null, // Dependency não alterada
        null, // State não alterado
        null, // Municipality não alterada
        null, // District não alterado
        null, // SchoolType não alterado
        null, // SchoolTypeDescription não alterada
        null, // SituationCode não alterado
        null, // SchoolCode não alterado
        Map.of("SALAS_AULA", 25L, "LAB_FISICA", 1L) // Apenas métricas
      );

      SchoolDto updatedSchool = new SchoolDto(
        12345678L,
        "Escola Municipal Vila Nova", // Nome original
        "Municipal",
        "SP",
        "São Paulo",
        "Vila Nova",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        Instant.now(),
        Instant.now(),
        new SchoolMetricsDto(
          12345678L,
          Map.of("SALAS_AULA", 25L, "LAB_FISICA", 1L),
          Instant.now(),
          Instant.now()
        )
      );

      when(schoolService.update(eq(12345678L), any(SchoolUpdateRequest.class)))
        .thenReturn(updatedSchool);

      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/12345678")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(metricsOnlyRequest))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schoolName").value("Escola Municipal Vila Nova"))
        .andExpect(jsonPath("$.metrics.metrics.SALAS_AULA").value(25))
        .andExpect(jsonPath("$.metrics.metrics.LAB_FISICA").value(1));
    }

    @Test
    @DisplayName("Deve retornar 404 quando escola não existe")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenSchoolNotExists() throws Exception {
      // Given
      when(schoolService.update(eq(99999999L), any(SchoolUpdateRequest.class)))
        .thenThrow(new IllegalArgumentException("Escola não encontrada"));

      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/99999999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 401 quando não autenticado")
    void shouldReturn401WhenNotAuthenticatedForUpdate() throws Exception {
      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/12345678")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 403 quando não é admin")
    @WithMockUser(roles = "CLIENT")
    void shouldReturn403WhenNotAdminForUpdate() throws Exception {
      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/12345678")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve aceitar atualização com dados parciais")
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptPartialUpdate() throws Exception {
      // Given - Apenas nome da escola
      SchoolUpdateRequest partialRequest = new SchoolUpdateRequest(
        "Escola Municipal Parcialmente Atualizada",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
      );

      SchoolDto partiallyUpdatedSchool = new SchoolDto(
        12345678L,
        "Escola Municipal Parcialmente Atualizada",
        "Municipal", // Dados originais mantidos
        "SP",
        "São Paulo",
        "Vila Nova",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345678L,
        Instant.now(),
        Instant.now(),
        metricsDto
      );

      when(schoolService.update(eq(12345678L), any(SchoolUpdateRequest.class)))
        .thenReturn(partiallyUpdatedSchool);

      // When & Then
      mockMvc
        .perform(
          put("/api/v1/schools/12345678")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(partialRequest))
        )
        .andExpect(status().isOk())
        .andExpect(
          jsonPath("$.schoolName")
            .value("Escola Municipal Parcialmente Atualizada")
        )
        .andExpect(jsonPath("$.administrativeDependency").value("Municipal"));
    }
  }

  @Nested
  @DisplayName("Consulta de Escolas")
  class QuerySchool {

    @Test
    @DisplayName("Deve buscar escola por código com sucesso")
    @WithMockUser(roles = "CLIENT")
    void shouldGetSchoolByCodeSuccessfully() throws Exception {
      // Given
      when(schoolQueryService.findSchoolByCode(12345678L))
        .thenReturn(Optional.of(schoolDto));

      // When & Then
      mockMvc
        .perform(get("/api/v1/schools/12345678"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.code").value(12345678L))
        .andExpect(
          jsonPath("$.schoolName").value("Escola Municipal Vila Nova")
        );
    }

    @Test
    @DisplayName("Deve retornar 404 quando escola não encontrada")
    @WithMockUser(roles = "CLIENT")
    void shouldReturn404WhenSchoolNotFound() throws Exception {
      // Given
      when(schoolQueryService.findSchoolByCode(99999999L))
        .thenReturn(Optional.empty());

      // When & Then
      mockMvc
        .perform(get("/api/v1/schools/99999999"))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar métricas disponíveis")
    @WithMockUser(roles = "CLIENT")
    void shouldListAvailableMetrics() throws Exception {
      // Given
      Set<String> availableMetrics = Set.of(
        "SALAS_AULA",
        "BIBLIOTECA",
        "QUADRA_COBERTA",
        "LAB_INFO",
        "REFEITORIO",
        "PLAYGROUND"
      );

      when(metricCatalogService.getAllValidMetricCodes())
        .thenReturn(availableMetrics);

      // When & Then
      mockMvc
        .perform(get("/api/v1/schools/metrics"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(6))
        .andExpect(
          jsonPath("$[*]")
            .value(
              org.hamcrest.Matchers.hasItems(
                "SALAS_AULA",
                "BIBLIOTECA",
                "QUADRA_COBERTA",
                "LAB_INFO",
                "REFEITORIO",
                "PLAYGROUND"
              )
            )
        );
    }

    @Test
    @DisplayName("Deve buscar métricas de escola específica")
    @WithMockUser(roles = "CLIENT")
    void shouldGetSchoolMetrics() throws Exception {
      // Given
      when(schoolQueryService.findSchoolMetrics(12345678L))
        .thenReturn(Optional.of(metricsDto));

      // When & Then
      mockMvc
        .perform(get("/api/v1/schools/12345678/metrics"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.schoolCode").value(12345678L))
        .andExpect(jsonPath("$.metrics.SALAS_AULA").value(15))
        .andExpect(jsonPath("$.metrics.BIBLIOTECA").value(1));
    }
  }

  @Nested
  @DisplayName("Validações de JSON")
  class JsonValidation {

    @Test
    @DisplayName("Deve retornar 400 para JSON malformado")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForMalformedJson() throws Exception {
      // Given - JSON inválido
      String malformedJson = "{ \"code\": 123, \"schoolName\": ";

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(malformedJson)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 415 para Content-Type inválido")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn415ForInvalidContentType() throws Exception {
      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.TEXT_PLAIN)
            .content("invalid content")
        )
        .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Deve aceitar métricas com valores zero")
    @WithMockUser(roles = "ADMIN")
    void shouldAcceptMetricsWithZeroValues() throws Exception {
      // Given
      Map<String, Long> metricsWithZeros = Map.of(
        "SALAS_AULA",
        10L,
        "PISCINA",
        0L, // Zero válido
        "TEATRO",
        0L, // Zero válido
        "BIBLIOTECA",
        1L
      );

      SchoolCreateRequest requestWithZeros = new SchoolCreateRequest(
        12345679L,
        "Escola Com Zeros",
        "Municipal",
        "SP",
        "São Paulo",
        "Centro",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345679L,
        metricsWithZeros
      );

      SchoolDto schoolWithZeros = new SchoolDto(
        12345679L,
        "Escola Com Zeros",
        "Municipal",
        "SP",
        "São Paulo",
        "Centro",
        (short) 1,
        "Pública Municipal",
        (short) 1,
        12345679L,
        Instant.now(),
        Instant.now(),
        new SchoolMetricsDto(
          12345679L,
          metricsWithZeros,
          Instant.now(),
          Instant.now()
        )
      );

      when(schoolService.create(any(SchoolCreateRequest.class)))
        .thenReturn(schoolWithZeros);

      // When & Then
      mockMvc
        .perform(
          post("/api/v1/schools")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestWithZeros))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.metrics.metrics.PISCINA").value(0))
        .andExpect(jsonPath("$.metrics.metrics.TEATRO").value(0))
        .andExpect(jsonPath("$.metrics.metrics.SALAS_AULA").value(10));
    }
  }
}
