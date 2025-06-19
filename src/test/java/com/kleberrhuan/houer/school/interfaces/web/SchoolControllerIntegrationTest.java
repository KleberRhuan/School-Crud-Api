/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.interfaces.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kleberrhuan.houer.school.domain.model.DataType;
import com.kleberrhuan.houer.school.domain.model.MetricDictionary;
import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.domain.model.SchoolMetrics;
import com.kleberrhuan.houer.school.domain.repository.MetricDictionaryRepository;
import com.kleberrhuan.houer.school.domain.repository.SchoolRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de integração - Filtros por Métricas JSONB")
class SchoolControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private SchoolRepository schoolRepository;

  @Autowired
  private MetricDictionaryRepository metricDictionaryRepository;

  @BeforeEach
  void setupTestData() {
    // Criar dicionário de métricas
    metricDictionaryRepository.save(
      new MetricDictionary(
        "SALAS_AULA",
        "Número de salas de aula",
        DataType.INT
      )
    );

    metricDictionaryRepository.save(
      new MetricDictionary("INTERNET", "Possui internet", DataType.BOOLEAN)
    );

    // Criar escolas de teste
    School school1 = School
      .builder()
      .code(1L)
      .nomeEsc("Escola Teste 1")
      .nomeDep("Municipal")
      .mun("Urbana")
      .codsit((short) 1)
      .build();

    SchoolMetrics metrics1 = SchoolMetrics
      .builder()
      .schoolCode(1L)
      .school(school1)
      .metrics(Map.of("SALAS_AULA", 15L, "INTERNET", 1L))
      .build();

    school1 =
      School
        .builder()
        .code(school1.getCode())
        .nomeEsc(school1.getNomeEsc())
        .nomeDep(school1.getNomeDep())
        .mun(school1.getMun())
        .codsit(school1.getCodsit())
        .schoolMetrics(metrics1)
        .build();

    schoolRepository.save(school1);

    School school2 = School
      .builder()
      .code(2L)
      .nomeEsc("Escola Teste 2")
      .nomeDep("Estadual")
      .mun("Rural")
      .codsit((short) 1)
      .build();

    SchoolMetrics metrics2 = SchoolMetrics
      .builder()
      .schoolCode(2L)
      .school(school2)
      .metrics(Map.of("SALAS_AULA", 5L, "INTERNET", 0L))
      .build();

    school2 =
      School
        .builder()
        .code(school2.getCode())
        .nomeEsc(school2.getNomeEsc())
        .nomeDep(school2.getNomeDep())
        .mun(school2.getMun())
        .codsit(school2.getCodsit())
        .schoolMetrics(metrics2)
        .build();

    schoolRepository.save(school2);
  }

  @Test
  @DisplayName("Deve ordenar escolas por número de salas decrescente")
  void shouldOrderByMetricDesc() throws Exception {
    mockMvc
      .perform(
        get("/api/v1/schools")
          .param("sort", "SALAS_AULA")
          .param("direction", "DESC")
          .param("page", "0")
          .param("size", "10")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data.length()").value(2))
      .andExpect(jsonPath("$.data[0].name").value("Escola Teste 1"))
      .andExpect(jsonPath("$.data[1].name").value("Escola Teste 2"));
  }

  @Test
  @DisplayName("Deve ordenar escolas por número de salas crescente")
  void shouldOrderByMetricAsc() throws Exception {
    mockMvc
      .perform(
        get("/api/v1/schools")
          .param("sort", "SALAS_AULA")
          .param("direction", "ASC")
          .param("page", "0")
          .param("size", "10")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data.length()").value(2))
      .andExpect(jsonPath("$.data[0].name").value("Escola Teste 2"))
      .andExpect(jsonPath("$.data[1].name").value("Escola Teste 1"));
  }

  @Test
  @DisplayName("Deve combinar filtros tradicionais com ordenação por métricas")
  void shouldCombineFiltersWithOrdering() throws Exception {
    mockMvc
      .perform(
        get("/api/v1/schools")
          .param("administrativeDependence", "municipal")
          .param("sort", "SALAS_AULA")
          .param("direction", "DESC")
          .param("page", "0")
          .param("size", "10")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray());
  }
}
