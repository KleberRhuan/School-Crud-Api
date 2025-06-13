/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
  name = "ApiErrorResponse",
  description = "Resposta de erro padronizada seguindo RFC-7807"
)
public record ApiErrorResponse(
  @Schema(description = "Código de status HTTP", example = "400")
  @Positive
  Integer status,
  @Schema(description = "URI do tipo de erro") String type,
  @Schema(description = "Título do erro") String title,
  @Schema(description = "Detalhes técnicos do erro") String detail,
  @Schema(description = "Mensagem amigável ao usuário") String userMessage,
  @Schema(
    description = "Momento da ocorrência",
    example = "2025-05-22T23:15:43Z",
    format = "date-time"
  )
  @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
  )
  OffsetDateTime timestamp,
  @Schema(description = "Violações de validação") List<Violation> violations
) {
  public ApiErrorResponse {
    if (timestamp == null) {
      timestamp = OffsetDateTime.now(ZoneId.of("UTC"));
    }
    if (violations != null && violations.isEmpty()) {
      violations = null;
    }
  }

  public static ApiErrorResponse of(
    ApiErrorType type,
    HttpStatus status,
    String detail,
    String userMsg,
    List<Violation> violations
  ) {
    return new ApiErrorResponse(
      status.value(),
      type.getUri(),
      type.getTitle(),
      detail,
      userMsg,
      null,
      violations
    );
  }

  public record Violation(String name, String message) {}
}
