/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.controllers;

import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ErrorResponseSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.SchoolCreateRequestSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.SchoolDtoSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.SchoolMetricsDtoSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.SchoolUpdateRequestSchema;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolCreateRequest;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolMetricsDto;
import com.kleberrhuan.houer.school.interfaces.dto.SchoolUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Interface de documentação para endpoints de gerenciamento de escolas. */
@Tag(name = "Gerenciamento de Escolas", description = "Endpoints para operações CRUD com dados de escolas, incluindo consultas avançadas e métricas")
public interface SchoolControllerDocumentation {

  @Operation(summary = "Criar nova escola", description = """
      Cria uma nova escola no sistema com todos os dados obrigatórios.\n\n
      • Código da escola deve ser único no sistema\n
      • Métricas podem ser fornecidas opcionalmente no momento da criação\n
      • Campos obrigatórios: code, schoolName\n
      • O sistema aplica valores padrão para métricas não informadas
      """, security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Escola criada com sucesso", content = @Content(schema = @Schema(implementation = SchoolDtoSchema.class))),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(responseCode = "409", description = "Conflito - Código da escola já existe", content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))),
      @ApiResponse(ref = "#/components/responses/ValidationError"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<SchoolDto> createSchool(
      @RequestBody(description = "Dados da escola a ser criada", required = true, content = @Content(schema = @Schema(implementation = SchoolCreateRequestSchema.class), examples = @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Escola Exemplo", summary = "Exemplo de criação de escola", value = """
          {
            "code": 12345678,
            "schoolName": "ESCOLA MUNICIPAL EXEMPLO",
            "administrativeDependency": "Municipal",
            "stateCode": "SP",
            "municipality": "São Paulo",
            "district": "Centro",
            "schoolType": 1,
            "schoolTypeDescription": "Educação Infantil",
            "situationCode": 1,
            "schoolCode": 87654321,
            "metrics": {
              "totalStudents": 150,
              "totalTeachers": 12
            }
          }
          """))) @Valid SchoolCreateRequest request);

  @Operation(summary = "Atualizar escola existente", description = """
      Atualiza os dados de uma escola existente.\n\n
      • Apenas os campos fornecidos serão atualizados\n
      • Métricas são mescladas com valores existentes\n
      • Código da escola não pode ser alterado após criação
      """, security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Escola atualizada com sucesso", content = @Content(schema = @Schema(implementation = SchoolDtoSchema.class))),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(ref = "#/components/responses/ValidationError"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<SchoolDto> updateSchool(
      @Parameter(description = "Código único da escola a ser atualizada", required = true, example = "12345678") @PathVariable Long code,
      @RequestBody(description = "Dados para atualização da escola (campos opcionais)", required = true, content = @Content(schema = @Schema(implementation = SchoolUpdateRequestSchema.class), examples = @io.swagger.v3.oas.annotations.media.ExampleObject(name = "Atualização Exemplo", summary = "Exemplo de atualização parcial", value = """
          {
            "schoolName": "ESCOLA MUNICIPAL EXEMPLO ATUALIZADA",
            "municipality": "São Bernardo do Campo",
            "metrics": {
              "totalStudents": 175
            }
          }
          """))) @Valid SchoolUpdateRequest request);

  @Operation(summary = "Buscar escola por código", description = """
      Recupera os dados completos de uma escola específica pelo seu código único.\n\n
      • Retorna dados básicos da escola\n
      • Inclui métricas associadas\n
      • Inclui timestamps de criação e atualização
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Escola encontrada com sucesso", content = @Content(schema = @Schema(implementation = SchoolDtoSchema.class))),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<SchoolDto> getSchoolByCode(
      @Parameter(description = "Código único da escola", required = true, example = "12345678") @PathVariable Long code);

  @Operation(summary = "Listar escolas com filtros", description = """
      Lista escolas com suporte a filtros avançados e paginação.\n\n
      **Filtros disponíveis:**\n
      • `name`: Busca por nome da escola (busca parcial, insensível a acentos)\n
      • `municipalityName`: Filtro por município\n
      • `stateAbbreviation`: Filtro por estado (ex: SP, RJ)\n
      • `operationalStatus`: Status operacional (1=Ativa, 2=Paralisada, etc.)\n
      • `dependencyType`: Tipo de dependência administrativa\n
      • `schoolType`: Tipo da escola (1=Infantil, 2=Fundamental, etc.)\n
      • `administrativeRegion`: Região administrativa\n
      • `administrativeDependence`: Dependência administrativa específica\n
      • `location`: Localização/município\n
      • `situation`: Situação da escola\n\n
      **Paginação e Ordenação:**\n
      • `page`: Número da página (inicia em 1)\n
      • `size`: Itens por página (máximo 100)\n
      • `sort`: Campo para ordenação (campos da escola ou métricas)\n
      • `direction`: Direção da ordenação (ASC ou DESC)
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Lista de escolas retornada com sucesso", content = @Content(schema = @Schema(implementation = PaginatedResponse.class, example = """
          {
            "content": [
              {
                "code": 12345678,
                "schoolName": "ESCOLA MUNICIPAL EXEMPLO",
                "administrativeDependency": "Municipal",
                "stateCode": "SP",
                "municipality": "São Paulo"
              }
            ],
            "page": 1,
            "size": 20,
            "totalElements": 150,
            "totalPages": 8,
            "last": false
          }
          """))),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<PaginatedResponse<List<SchoolDto>>> getAllSchools(
      PageableRequest pageableRequest,
      @Parameter(description = "Nome da escola (busca parcial)", example = "MUNICIPAL") @RequestParam(required = false) String name,
      @Parameter(description = "Nome do município", example = "São Paulo") @RequestParam(required = false) String municipalityName,
      @Parameter(description = "Sigla do estado", example = "SP") @RequestParam(required = false) String stateAbbreviation,
      @Parameter(description = "Status operacional (1=Ativa, 2=Paralisada)", example = "1") @RequestParam(required = false) Integer operationalStatus,
      @Parameter(description = "Tipo de dependência administrativa", example = "1") @RequestParam(required = false) Integer dependencyType,
      @Parameter(description = "Tipo da escola (1=Infantil, 2=Fundamental)", example = "2") @RequestParam(required = false) Short schoolType,
      @Parameter(description = "Região administrativa", example = "Norte") @RequestParam(required = false) String administrativeRegion,
      @Parameter(description = "Dependência administrativa específica", example = "Municipal") @RequestParam(required = false) String administrativeDependence,
      @Parameter(description = "Localização/município", example = "Centro") @RequestParam(required = false) String location,
      @Parameter(description = "Situação da escola", example = "1") @RequestParam(required = false) String situation);

  @Operation(summary = "Obter métricas da escola", description = """
      Recupera apenas as métricas detalhadas de uma escola específica.\n\n
      • Dados em formato JSON flexível\n
      • Inclui todas as métricas cadastradas\n
      • Útil para dashboards e relatórios específicos
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Métricas da escola retornadas com sucesso", content = @Content(schema = @Schema(implementation = SchoolMetricsDtoSchema.class))),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<SchoolMetricsDto> getSchoolMetrics(
      @Parameter(description = "Código único da escola", required = true, example = "12345678") @PathVariable Long code);

  @Operation(summary = "Excluir escola", description = """
      Remove permanentemente uma escola do sistema.\n\n
      ⚠️ **ATENÇÃO:** Esta operação é irreversível!\n
      • Remove a escola e todas as métricas associadas\n
      • Requer permissões administrativas\n
      • Não é possível desfazer esta ação
      """, security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Escola excluída com sucesso"),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(ref = "#/components/responses/InternalServerError")
  })
  ResponseEntity<Void> deleteSchool(
      @Parameter(description = "Código único da escola a ser excluída", required = true, example = "12345678") @PathVariable Long code);
}