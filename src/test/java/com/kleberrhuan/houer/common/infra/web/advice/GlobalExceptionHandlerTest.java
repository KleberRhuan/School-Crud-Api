/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.web.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kleberrhuan.houer.common.domain.exception.BusinessException;
import com.kleberrhuan.houer.common.infra.exception.InfrastructureException;
import com.kleberrhuan.houer.common.infra.exception.factory.ApiErrorResponseFactory;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorResponse;
import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import com.kleberrhuan.houer.common.interfaces.dto.error.MessageKey;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  @Mock
  private ApiErrorResponseFactory factory;

  @Mock
  private HttpServletRequest request;

  @Mock
  private WebRequest webRequest;

  @InjectMocks
  private GlobalExceptionHandler handler;

  private ApiErrorResponse mockErrorResponse;

  @BeforeEach
  void setUp() {
    // Configure base URL for ApiErrorType to avoid IllegalStateException
    ApiErrorType.setBaseUrl("http://localhost:8080");

    LocaleContextHolder.setLocale(Locale.forLanguageTag("pt-BR"));
    mockErrorResponse =
      ApiErrorResponse.of(
        ApiErrorType.SYSTEM_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal system error",
        "Try again later",
        null
      );
  }

  @Nested
  @DisplayName("API Exception Handling")
  class ApiExceptionHandling {

    @Test
    @DisplayName("Should handle BusinessException correctly")
    void shouldHandleBusinessException() {
      // Given
      BusinessException exception = new BusinessException(
        HttpStatus.BAD_REQUEST,
        ApiErrorType.INVALID_PARAMETER,
        MessageKey.of("error.business.invalid"),
        "param1"
      );

      when(
        factory.build(
          eq(HttpStatus.BAD_REQUEST),
          eq(ApiErrorType.INVALID_PARAMETER),
          eq(MessageKey.of("error.business.invalid")),
          any(Locale.class),
          eq(new Object[] { "param1" })
        )
      )
        .thenReturn(mockErrorResponse);

      // When
      ResponseEntity<Object> response = handler.handleApiException(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isEqualTo(mockErrorResponse);
    }

    @Test
    @DisplayName("Should handle InfrastructureException correctly")
    void shouldHandleInfrastructureException() {
      // Given
      InfrastructureException exception = new InfrastructureException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorType.SYSTEM_ERROR,
        MessageKey.of("error.infrastructure.database")
      );

      when(
        factory.build(
          eq(HttpStatus.INTERNAL_SERVER_ERROR),
          eq(ApiErrorType.SYSTEM_ERROR),
          eq(MessageKey.of("error.infrastructure.database")),
          any(Locale.class),
          eq(new Object[0])
        )
      )
        .thenReturn(mockErrorResponse);

      // When
      ResponseEntity<Object> response = handler.handleApiException(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(mockErrorResponse);
    }
  }

  @Nested
  @DisplayName("Validation Exception Handling")
  class ValidationExceptionHandling {

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException correctly")
    void shouldHandleMethodArgumentNotValidException() {
      // Given
      MethodParameter parameter = mock(MethodParameter.class);
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = new FieldError("user", "email", "Invalid email");

      // Mock getAllErrors since buildViolations uses getAllErrors
      when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

      MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(parameter, bindingResult);

      // When
      ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
        exception,
        HttpHeaders.EMPTY,
        HttpStatus.BAD_REQUEST,
        webRequest
      );

      // Then
      assertNotNull(response);
      assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);

      ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
      assertThat(errorResponse.type()).contains("invalid-parameter");
      assertThat(errorResponse.status()).isEqualTo(422);
      assertThat(errorResponse.violations()).hasSize(1);
      assertThat(errorResponse.violations().getFirst().name())
        .isEqualTo("email");
      assertThat(errorResponse.violations().getFirst().message())
        .isEqualTo("Invalid email");
    }
  }

  @Nested
  @DisplayName("HTTP Exception Handling")
  class HttpExceptionHandling {

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException")
    void shouldHandleHttpMessageNotReadableException() {
      // Given
      HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
      HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException(
          "JSON parse error",
          httpInputMessage
        );

      // When
      ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(
        exception,
        HttpHeaders.EMPTY,
        HttpStatus.BAD_REQUEST,
        webRequest
      );

      // Then
      assertNotNull(response);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);

      ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
      assertNotNull(errorResponse);
      assertThat(errorResponse.type())
        .isEqualTo(ApiErrorType.MESSAGE_NOT_READABLE.getUri());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException")
    void shouldHandleHttpRequestMethodNotSupportedException() {
      // Given
      HttpRequestMethodNotSupportedException exception =
        new HttpRequestMethodNotSupportedException(
          "POST",
          List.of("GET", "PUT")
        );

      when(request.getRequestURI()).thenReturn("/api/test");

      // When
      ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
          exception,
          HttpHeaders.EMPTY,
          HttpStatus.METHOD_NOT_ALLOWED,
          webRequest
        );

      // Then
      assertNotNull(response);
      assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
      assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);

      ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
      assertNotNull(errorResponse);
      assertThat(errorResponse.type())
        .isEqualTo(ApiErrorType.METHOD_NOT_ALLOWED.getUri());
    }

    @Test
    @DisplayName("Should handle NoHandlerFoundException")
    void shouldHandleNoHandlerFoundException() {
      // Given
      NoHandlerFoundException exception = new NoHandlerFoundException(
        "GET",
        "/api/nonexistent",
        HttpHeaders.EMPTY
      );

      // When
      ResponseEntity<Object> response = handler.handleNoHandlerFoundException(
        exception,
        HttpHeaders.EMPTY,
        HttpStatus.NOT_FOUND,
        webRequest
      );

      // Then
      assertNotNull(response);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);

      ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
      assertNotNull(errorResponse);
      assertThat(errorResponse.type())
        .isEqualTo(ApiErrorType.RESOURCE_NOT_FOUND.getUri());
    }
  }

  @Nested
  @DisplayName("Parameter Exception Handling")
  class ParameterExceptionHandling {

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException")
    void shouldHandleMethodArgumentTypeMismatchException() {
      // Given
      MethodArgumentTypeMismatchException exception =
        new MethodArgumentTypeMismatchException(
          "invalid",
          Long.class,
          "id",
          null,
          null
        );

      // When
      ResponseEntity<Object> response = handler.handleTypeMismatch(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);

      ApiErrorResponse errorResponse = (ApiErrorResponse) response.getBody();
      assertNotNull(errorResponse);
      assertThat(errorResponse.type())
        .isEqualTo(ApiErrorType.INVALID_PARAMETER.getUri());
    }
  }

  @Nested
  @DisplayName("Security Exception Handling")
  class SecurityExceptionHandling {

    @Test
    @DisplayName("Should handle AuthenticationException")
    void shouldHandleAuthenticationException() {
      // Given
      AuthenticationException exception = new AuthenticationException(
        "Bad credentials"
      ) {};

      when(
        factory.build(
          eq(HttpStatus.UNAUTHORIZED),
          eq(ApiErrorType.INVALID_CREDENTIALS),
          any(MessageKey.class),
          any(Locale.class),
          any(Object[].class)
        )
      )
        .thenReturn(mockErrorResponse);

      // When
      ResponseEntity<Object> response = handler.handleAuth(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isEqualTo(mockErrorResponse);
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void shouldHandleAccessDeniedException() {
      // Given
      AccessDeniedException exception = new AccessDeniedException(
        "Access denied"
      );

      when(
        factory.build(
          eq(HttpStatus.FORBIDDEN),
          eq(ApiErrorType.FORBIDDEN),
          eq(MessageKey.of("error.business.security.forbidden")),
          any(Locale.class),
          eq(new Object[0])
        )
      )
        .thenReturn(mockErrorResponse);

      // When
      ResponseEntity<Object> response = handler.handleAccessDenied(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
      assertThat(response.getBody()).isEqualTo(mockErrorResponse);
    }
  }

  @Nested
  @DisplayName("Generic Exception Handling")
  class GenericExceptionHandling {

    @Test
    @DisplayName("Should handle unhandled exceptions")
    void shouldHandleUnhandledException() {
      // Given
      RuntimeException exception = new RuntimeException("Unexpected error");

      when(
        factory.build(
          eq(HttpStatus.INTERNAL_SERVER_ERROR),
          eq(ApiErrorType.SYSTEM_ERROR),
          eq(MessageKey.of("error.infrastructure.default")),
          any(Locale.class),
          eq(new Object[0])
        )
      )
        .thenReturn(mockErrorResponse);

      // When
      ResponseEntity<Object> response = handler.handleUnhandled(
        exception,
        webRequest
      );

      // Then
      assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(mockErrorResponse);
    }
  }
}
