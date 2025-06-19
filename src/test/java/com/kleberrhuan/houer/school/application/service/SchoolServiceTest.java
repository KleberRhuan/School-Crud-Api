/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.school.application.factory.SchoolFactory;
import com.kleberrhuan.houer.school.application.mapper.SchoolMapper;
import com.kleberrhuan.houer.school.domain.model.School;
import com.kleberrhuan.houer.school.domain.model.SchoolMetrics;
import com.kleberrhuan.houer.school.domain.repository.SchoolRepository;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchoolService - Testes Unitários")
class SchoolServiceTest {

  @Mock
  private SchoolRepository repo;

  @Mock
  private SchoolFactory factory;

  @Mock
  private SchoolMapper mapper;

  @InjectMocks
  private SchoolService schoolService;

  private School mockSchool;
  private SchoolDto mockSchoolDto;
  private SchoolCreateRequest createRequest;
  private SchoolUpdateRequest updateRequest;
  private SchoolMetricsDto metricsDto;

  @BeforeEach
  void setUp() {
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

    SchoolMetrics schoolMetrics = SchoolMetrics
      .builder()
      .schoolCode(12345678L)
      .metrics(metrics)
      .build();

    mockSchool =
      School
        .builder()
        .code(12345678L)
        .nomeEsc("Escola Municipal Vila Nova")
        .nomeDep("Municipal")
        .de("SP")
        .mun("São Paulo")
        .distr("Vila Nova")
        .tipoEsc((short) 1)
        .tipoEscDesc("Pública Municipal")
        .codsit((short) 1)
        .codesc(12345678L)
        .schoolMetrics(schoolMetrics)
        .build();

    metricsDto =
      new SchoolMetricsDto(12345678L, metrics, Instant.now(), Instant.now());

    mockSchoolDto =
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
    @DisplayName("Deve criar escola com sucesso")
    void shouldCreateSchoolSuccessfully() {
      // Given
      when(factory.createFromRequest(createRequest)).thenReturn(mockSchool);
      when(repo.save(mockSchool)).thenReturn(mockSchool);
      when(mapper.toDto(mockSchool)).thenReturn(mockSchoolDto);

      // When
      SchoolDto result = schoolService.create(createRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.code()).isEqualTo(12345678L);
      assertThat(result.schoolName()).isEqualTo("Escola Municipal Vila Nova");
      assertThat(result.administrativeDependency()).isEqualTo("Municipal");
      assertThat(result.metrics()).isNotNull();

      // Verificar chamadas aos mocks
      verify(factory).createFromRequest(createRequest);
      verify(repo).save(mockSchool);
      verify(mapper).toDto(mockSchool);
    }

    @Test
    @DisplayName("Deve criar escola com métricas vazias")
    void shouldCreateSchoolWithEmptyMetrics() {
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

      School schoolWithoutMetrics = School
        .builder()
        .code(12345679L)
        .nomeEsc("Escola Sem Métricas")
        .build();

      SchoolDto schoolDtoWithoutMetrics = new SchoolDto(
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
        null // Sem métricas
      );

      when(factory.createFromRequest(requestWithoutMetrics))
        .thenReturn(schoolWithoutMetrics);
      when(repo.save(schoolWithoutMetrics)).thenReturn(schoolWithoutMetrics);
      when(mapper.toDto(schoolWithoutMetrics))
        .thenReturn(schoolDtoWithoutMetrics);

      // When
      SchoolDto result = schoolService.create(requestWithoutMetrics);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.code()).isEqualTo(12345679L);
      assertThat(result.schoolName()).isEqualTo("Escola Sem Métricas");

      verify(factory).createFromRequest(requestWithoutMetrics);
      verify(repo).save(schoolWithoutMetrics);
      verify(mapper).toDto(schoolWithoutMetrics);
    }
  }

  @Nested
  @DisplayName("Atualização de Escolas")
  class UpdateSchool {

    @Test
    @DisplayName("Deve atualizar escola com sucesso")
    void shouldUpdateSchoolSuccessfully() {
      // Given
      when(repo.findWithMetricsByCode(12345678L))
        .thenReturn(Optional.of(mockSchool));
      when(mapper.toDto(mockSchool)).thenReturn(mockSchoolDto);

      // When
      SchoolDto result = schoolService.update(12345678L, updateRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.code()).isEqualTo(12345678L);

      // Verificar chamadas aos mocks
      verify(repo).findWithMetricsByCode(12345678L);
      verify(factory).updateFromRequest(mockSchool, updateRequest);
      verify(mapper).toDto(mockSchool);
    }

    @Test
    @DisplayName("Deve falhar quando escola não existe")
    void shouldFailWhenSchoolNotExists() {
      // Given
      when(repo.findWithMetricsByCode(99999999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> schoolService.update(99999999L, updateRequest))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("error.business.entity.notFound");

      // Verificar que apenas a busca foi executada
      verify(repo).findWithMetricsByCode(99999999L);
      verify(factory, org.mockito.Mockito.never())
        .updateFromRequest(any(), any());
    }
  }
}
