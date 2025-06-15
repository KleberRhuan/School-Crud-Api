/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ErrorResponseSchema;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
  @Positive Integer status,
  String type,
  String title,
  String detail,
  String userMessage,
  @JsonFormat(
    shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ssXXX"
  )
  OffsetDateTime timestamp,
  List<Violation> violations
)
  implements ErrorResponseSchema {
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
