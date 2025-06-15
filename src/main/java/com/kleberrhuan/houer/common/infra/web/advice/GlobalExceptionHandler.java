/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.web.advice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.kleberrhuan.houer.common.infra.exception.ApiException;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorResponse;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final ApiErrorResponseFactory errorFactory;
  private final HttpServletRequest httpReq;

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Object> handleApiException(
    ApiException ex,
    WebRequest req
  ) {
    ApiErrorResponse body = errorFactory.build(
      ex.getStatus(),
      ex.getErrorType(),
      ex.getKey(),
      LocaleContextHolder.getLocale(),
      ex.getArguments()
    );
    return handleExceptionInternal(
      ex,
      body,
      new HttpHeaders(),
      ex.getStatus(),
      req
    );
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    List<ApiErrorResponse.Violation> violations = buildViolations(
      ex.getBindingResult()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.INVALID_PARAMETER,
      HttpStatus.UNPROCESSABLE_ENTITY,
      "Um ou mais parâmetros são inválidos.",
      "Verifique os campos marcados e tente novamente.",
      violations
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.UNPROCESSABLE_ENTITY,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
    @NonNull HttpMessageNotReadableException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    Throwable root = ExceptionUtils.getRootCause(ex);

    if (root instanceof InvalidFormatException ife) {
      return handleInvalidFormat(ife, headers, request);
    }

    if (root instanceof UnrecognizedPropertyException upe) {
      return handleUnknownProperty(upe, headers, request);
    }

    if (root instanceof JsonMappingException jme) {
      return handleJsonMapping(jme, headers, request);
    }

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.MESSAGE_NOT_READABLE,
      HttpStatus.BAD_REQUEST,
      "Corpo da requisição inválido ou mal formatado.",
      "Verifique a sintaxe JSON e tente novamente.",
      null
    );
    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
    HttpRequestMethodNotSupportedException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    String detail = String.format(
      "O recurso '%s' não suporta o método '%s'. Métodos permitidos: %s",
      httpReq.getRequestURI(),
      ex.getMethod(),
      ex.getSupportedHttpMethods()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.METHOD_NOT_ALLOWED,
      HttpStatus.METHOD_NOT_ALLOWED,
      detail,
      "Método não permitido para este endpoint.",
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.METHOD_NOT_ALLOWED,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
    NoHandlerFoundException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    String detail = String.format(
      "O endpoint '%s %s' não foi encontrado.",
      ex.getHttpMethod(),
      ex.getRequestURL()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.RESOURCE_NOT_FOUND,
      HttpStatus.NOT_FOUND,
      detail,
      "Recurso não encontrado.",
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.NOT_FOUND,
      request
    );
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleTypeMismatch(
    MethodArgumentTypeMismatchException ex,
    WebRequest req
  ) {
    String required = ex.getRequiredType() != null
      ? ex.getRequiredType().getSimpleName()
      : "desconhecido";
    String detail = String.format(
      "O parâmetro '%s' recebeu '%s', mas requer o tipo %s.",
      ex.getName(),
      ex.getValue(),
      required
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.INVALID_PARAMETER,
      HttpStatus.BAD_REQUEST,
      detail,
      detail,
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      new HttpHeaders(),
      HttpStatus.BAD_REQUEST,
      req
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Object> handleAuth(
    AuthenticationException ex,
    WebRequest req
  ) {
    Locale locale = LocaleContextHolder.getLocale();
    HttpHeaders headers = new HttpHeaders();
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    MessageKey key;
    Object[] args;

    if (ex instanceof MessageSourceResolvable resolvable) {
      key =
        MessageKey.of(
          Objects
            .requireNonNull(resolvable.getCodes())[0].replace(".detail", "")
        );
      args = resolvable.getArguments();
    } else {
      key = MessageKey.of("error.auth.bad-credentials");
      args = new Object[0];
    }

    ApiErrorResponse body = errorFactory.build(
      status,
      ApiErrorType.INVALID_CREDENTIALS,
      key,
      locale,
      args
    );
    return handleExceptionInternal(ex, body, headers, status, req);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDenied(
    AccessDeniedException ex,
    WebRequest req
  ) {
    Locale locale = LocaleContextHolder.getLocale();
    HttpHeaders headers = new HttpHeaders();
    HttpStatus status = HttpStatus.FORBIDDEN;

    ApiErrorResponse body = errorFactory.build(
      status,
      ApiErrorType.FORBIDDEN,
      MessageKey.of("error.business.security.forbidden"),
      locale,
      new Object[0]
    );
    return handleExceptionInternal(ex, body, headers, status, req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnhandled(Exception ex, WebRequest req) {
    log.error("Erro não tratado: ", ex);

    Locale locale = LocaleContextHolder.getLocale();
    HttpHeaders headers = new HttpHeaders();
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    ApiErrorResponse body = errorFactory.build(
      status,
      ApiErrorType.SYSTEM_ERROR,
      MessageKey.of("error.infrastructure.default"),
      locale,
      new Object[0]
    );
    return handleExceptionInternal(ex, body, headers, status, req);
  }

  private ResponseEntity<Object> handleInvalidFormat(
    InvalidFormatException ex,
    HttpHeaders headers,
    WebRequest request
  ) {
    String path = ex
      .getPath()
      .stream()
      .map(JsonMappingException.Reference::getFieldName)
      .filter(s -> s != null && !s.isEmpty())
      .collect(Collectors.joining("."));

    String detail = String.format(
      "A propriedade '%s' recebeu o valor '%s', incompatível com %s",
      path,
      ex.getValue(),
      ex.getTargetType().getSimpleName()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.MESSAGE_NOT_READABLE,
      HttpStatus.BAD_REQUEST,
      detail,
      detail,
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }

  private List<ApiErrorResponse.Violation> buildViolations(BindingResult br) {
    return br
      .getAllErrors()
      .stream()
      .map(err -> {
        String name = err instanceof FieldError fe
          ? fe.getField()
          : err.getObjectName();
        String msg = err.getDefaultMessage();
        return new ApiErrorResponse.Violation(name, msg);
      })
      .toList();
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
    HttpMediaTypeNotSupportedException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    String detail = String.format(
      "O Content-Type %s não é suportado. Tipos permitidos: %s",
      ex.getContentType(),
      ex.getSupportedMediaTypes()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.UNSUPPORTED_MEDIA_TYPE,
      HttpStatus.UNSUPPORTED_MEDIA_TYPE,
      detail,
      "Tipo de mídia não suportado.",
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.UNSUPPORTED_MEDIA_TYPE,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
    @NonNull HttpMediaTypeNotAcceptableException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.NOT_ACCEPTABLE,
      HttpStatus.NOT_ACCEPTABLE,
      "O cabeçalho Accept não pode ser atendido.",
      "Formato de resposta não disponível.",
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.NOT_ACCEPTABLE,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
    MissingServletRequestParameterException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    String detail = String.format(
      "O parâmetro '%s' é obrigatório.",
      ex.getParameterName()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.INVALID_PARAMETER,
      HttpStatus.BAD_REQUEST,
      detail,
      detail,
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
    MissingServletRequestPartException ex,
    @NonNull HttpHeaders headers,
    @NonNull HttpStatusCode status,
    @NonNull WebRequest request
  ) {
    String detail = String.format(
      "A parte de upload '%s' é obrigatória.",
      ex.getRequestPartName()
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.INVALID_PARAMETER,
      HttpStatus.BAD_REQUEST,
      detail,
      detail,
      null
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }

  private ResponseEntity<Object> handleUnknownProperty(
    UnrecognizedPropertyException ex,
    HttpHeaders headers,
    WebRequest request
  ) {
    String field = ex.getPropertyName();
    String allowed = String.join(
      ", ",
      ex.getKnownPropertyIds().stream().map(Object::toString).toList()
    );

    String detail = "Campo Invalido informado no corpo da requisicao.";
    String userMsg =
      "ocorreu um erro ao processar a requisicao, verifique os campos informados.";

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.MESSAGE_NOT_READABLE,
      HttpStatus.BAD_REQUEST,
      detail,
      userMsg,
      List.of(
        new ApiErrorResponse.Violation(
          field,
          "Campo não reconhecido, campos permitidos: " + allowed
        )
      )
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }

  private ResponseEntity<Object> handleJsonMapping(
    JsonMappingException ex,
    HttpHeaders headers,
    WebRequest request
  ) {
    String path = ex
      .getPath()
      .stream()
      .map(JsonMappingException.Reference::getFieldName)
      .filter(f -> f != null && !f.isBlank())
      .collect(Collectors.joining("."));

    String detail = String.format(
      "Erro de mapeamento na propriedade '%s': %s",
      path.isBlank() ? "<corpo>" : path,
      ex.getOriginalMessage()
    );

    String userMsg = String.format(
      "ocorreu um erro ao tentar ler o campo '%s'.",
      path
    );

    ApiErrorResponse body = ApiErrorResponse.of(
      ApiErrorType.MESSAGE_NOT_READABLE,
      HttpStatus.BAD_REQUEST,
      detail,
      userMsg,
      List.of(new ApiErrorResponse.Violation(path, ex.getOriginalMessage()))
    );

    return handleExceptionInternal(
      ex,
      body,
      headers,
      HttpStatus.BAD_REQUEST,
      request
    );
  }
}
