/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.controllers;

import com.kleberrhuan.houer.common.interfaces.documentation.schemas.CsvImportRequestDtoSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.CsvImportResponseDtoSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ErrorResponseSchema;
import com.kleberrhuan.houer.common.interfaces.dto.request.PageableRequest;
import com.kleberrhuan.houer.common.interfaces.dto.response.PaginatedResponse;
import com.kleberrhuan.houer.csv.domain.model.ImportJobStatus;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportRequestDto;
import com.kleberrhuan.houer.csv.interfaces.dto.CsvImportResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/** Interface de documentação para endpoints de importação CSV de dados escolares. */
@Tag(
  name = "Importação CSV",
  description = "Endpoints para importação e processamento de arquivos CSV com dados de escolas, incluindo acompanhamento em tempo real via WebSocket"
)
public interface CsvImportControllerDocumentation {
  @Operation(
    summary = "Iniciar importação CSV",
    description = """
      Inicia o processamento assíncrono de um arquivo CSV contendo dados de escolas.\n\n
      **Formato do Arquivo CSV:**\n
      • Primeira linha deve conter os cabeçalhos (nomes das colunas)\n
      • Colunas obrigatórias: CODESC, NOMESC, NOMEDEP, DE, MUN, DISTR\n
      • Tamanho máximo: 10MB\n
      • Formato: UTF-8 com separador vírgula (,)\n\n
      **Processamento Assíncrono:**\n
      • O arquivo é validado e enviado para fila de processamento\n
      • Acompanhe o progresso via WebSocket endpoint: `/ws`\n
      • Canal de notificação: `/topic/csv-import/{jobId}`\n
      • Status disponíveis: PENDING, PROCESSING, COMPLETED, FAILED\n\n
      **Notificações em Tempo Real:**\n
      • Conecte-se ao WebSocket: `ws://localhost:8080/ws`\n
      • Subscribe no tópico: `/topic/csv-import/{jobId}`\n
      • Receba atualizações de progresso automaticamente
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "202",
        description = "Importação iniciada com sucesso",
        content = @Content(
          schema = @Schema(implementation = CsvImportResponseDtoSchema.class),
          examples = @ExampleObject(
            name = "Job Criado",
            summary = "Resposta de job criado com sucesso",
            value = """
          {
            "jobId": "123e4567-e89b-12d3-a456-426614174000",
            "filename": "escolas_sp_2025.csv",
            "status": "PENDING",
            "totalRecords": null,
            "processedRecords": 0,
            "errorRecords": 0,
            "errorMessage": null,
            "startedAt": null,
            "finishedAt": null,
            "createdAt": "2025-01-15T10:30:00Z"
          }
          """
          )
        )
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(
        responseCode = "413",
        description = "Arquivo muito grande (máximo 10MB)",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(
        responseCode = "415",
        description = "Tipo de arquivo não suportado (apenas CSV)",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/ValidationError"),
      @ApiResponse(ref = "#/components/responses/TooManyRequests"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<CsvImportResponseDto> startImport(
    @RequestBody(
      description = "Arquivo CSV e metadados da importação",
      required = true,
      content = @Content(
        mediaType = "multipart/form-data",
        schema = @Schema(implementation = CsvImportRequestDtoSchema.class),
        examples = @ExampleObject(
          name = "Upload CSV",
          summary = "Exemplo de upload de arquivo CSV",
          value = """
          {
            "file": "(arquivo CSV binário)",
            "description": "Importação de dados das escolas municipais de São Paulo - Janeiro 2025"
          }
          """
        )
      )
    ) @Valid @ModelAttribute CsvImportRequestDto request,
    Authentication auth
  );

  @Operation(
    summary = "Obter detalhes do job",
    description = """
      Recupera informações detalhadas sobre um job de importação específico.\n\n
      • Inclui status atual, progresso e mensagens de erro\n
      • Apenas o usuário que criou o job pode visualizá-lo\n
      • Ideal para polling quando WebSocket não está disponível\n
      • Timestamps em formato ISO 8601 UTC
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Detalhes do job retornados com sucesso",
        content = @Content(
          schema = @Schema(implementation = CsvImportResponseDtoSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<CsvImportResponseDto> getJobDetails(
    @Parameter(
      description = "ID único do job de importação",
      required = true,
      example = "123e4567-e89b-12d3-a456-426614174000"
    ) @PathVariable UUID jobId,
    Authentication auth
  );

  @Operation(
    summary = "Listar jobs do usuário",
    description = """
      Lista todos os jobs de importação criados pelo usuário autenticado.\n\n
      **Funcionalidades:**\n
      • Paginação completa com ordenação\n
      • Filtros por status, data de criação\n
      • Histórico completo de importações\n
      • Ordenação padrão: mais recentes primeiro\n\n
      **Parâmetros de Paginação:**\n
      • `page`: Página (inicia em 1)\n
      • `size`: Itens por página (1-100)\n
      • `sort`: Campo de ordenação (createdAt, status, filename)\n
      • `direction`: ASC ou DESC
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Lista de jobs retornada com sucesso",
        content = @Content(
          schema = @Schema(
            implementation = PaginatedResponse.class,
            example = """
          {
            "content": [
              {
                "jobId": "123e4567-e89b-12d3-a456-426614174000",
                "filename": "escolas_sp_2025.csv",
                "status": "COMPLETED",
                "totalRecords": 1500,
                "processedRecords": 1485,
                "errorRecords": 15,
                "createdAt": "2025-01-15T10:30:00Z"
              }
            ],
            "page": 1,
            "size": 20,
            "totalElements": 45,
            "totalPages": 3,
            "last": false
          }
          """
          )
        )
      ),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listUserJobs(
    PageableRequest pageableRequest,
    Authentication auth
  );

  @Operation(
    summary = "Listar jobs por status",
    description = """
      Lista jobs de importação filtrados por status específico.\n\n
      **Status Disponíveis:**\n
      • `PENDING`: Aguardando processamento\n
      • `PROCESSING`: Em andamento\n
      • `COMPLETED`: Finalizado com sucesso\n
      • `FAILED`: Falhou durante processamento\n\n
      Útil para dashboards administrativos e monitoramento.
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Jobs filtrados por status retornados",
        content = @Content(
          schema = @Schema(implementation = PaginatedResponse.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listJobsByStatus(
    @Parameter(
      description = "Status dos jobs a serem filtrados",
      required = true,
      example = "COMPLETED"
    ) @PathVariable ImportJobStatus status,
    PageableRequest pageableRequest
  );

  @Operation(
    summary = "Listar todos os jobs (admin)",
    description = """
      Lista todos os jobs de importação do sistema (acesso administrativo).\n\n
      ⚠️ **Acesso Restrito:**\n
      • Requer privilégios de administrador\n
      • Visualiza jobs de todos os usuários\n
      • Usado para monitoramento e auditoria\n\n
      Inclui mesmas funcionalidades de paginação e ordenação dos outros endpoints.
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Todos os jobs retornados com sucesso",
        content = @Content(
          schema = @Schema(implementation = PaginatedResponse.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<PaginatedResponse<List<CsvImportResponseDto>>> listAllJobs(
    PageableRequest pageableRequest
  );

  @Operation(
    summary = "Cancelar job de importação",
    description = """
      Cancela um job de importação em execução ou pendente.\n\n
      **Comportamento:**\n
      • Jobs PENDING: Cancelados imediatamente\n
      • Jobs PROCESSING: Interrompidos no próximo checkpoint\n
      • Jobs COMPLETED/FAILED: Não podem ser cancelados\n\n
      **Permissões:**\n
      • Apenas o usuário que criou o job pode cancelá-lo\n
      • Administradores podem cancelar qualquer job\n\n
      ⚠️ **Atenção:** Cancelamento pode deixar dados parcialmente importados.
      """,
    security = @SecurityRequirement(name = "BearerAuth")
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Job cancelado com sucesso",
        content = @Content(
          schema = @Schema(implementation = CsvImportResponseDtoSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/Forbidden"),
      @ApiResponse(ref = "#/components/responses/NotFound"),
      @ApiResponse(
        responseCode = "409",
        description = "Job já finalizado e não pode ser cancelado",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<CsvImportResponseDto> cancelJob(
    @Parameter(
      description = "ID único do job a ser cancelado",
      required = true,
      example = "123e4567-e89b-12d3-a456-426614174000"
    ) @PathVariable UUID jobId,
    Authentication auth
  );
}
