/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.logging;

import static org.assertj.core.api.Assertions.*;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConciseThrowableConverter Tests")
class ConciseThrowableConverterTest {

  private ConciseThrowableConverter converter;

  @BeforeEach
  void setUp() {
    converter = new ConciseThrowableConverter();
  }

  @Test
  @DisplayName("Deve limitar stack trace a número configurado de linhas")
  void shouldLimitStackTraceLines() {
    // Given
    converter.setOptionList(java.util.List.of("3"));
    Exception exception = createException();
    IThrowableProxy proxy = new ThrowableProxy(exception);

    // When
    String result = converter.throwableProxyToString(proxy);

    // Then
    assertThat(result).contains("RuntimeException: Test exception");

    // Count stack trace lines (excluding the exception message line)
    String[] lines = result.split("\n");
    long stackTraceLines = java.util.Arrays
      .stream(lines)
      .filter(line -> line.trim().startsWith("at "))
      .count();

    assertThat(stackTraceLines).isLessThanOrEqualTo(3);
  }

  @Test
  @DisplayName("Deve filtrar pacotes excluídos")
  void shouldFilterExcludedPackages() {
    // Given
    Exception exception = createExceptionWithSpringFrameworkStack();
    IThrowableProxy proxy = new ThrowableProxy(exception);

    // When
    String result = converter.throwableProxyToString(proxy);

    // Then
    assertThat(result).doesNotContain("org.springframework.web.servlet");
    assertThat(result).doesNotContain("sun.reflect");
  }

  @Test
  @DisplayName("Deve incluir causa da exceção de forma resumida")
  void shouldIncludeCauseInConciseWay() {
    // Given
    Exception cause = new IllegalArgumentException("Invalid argument");
    Exception exception = new RuntimeException("Wrapper exception", cause);
    IThrowableProxy proxy = new ThrowableProxy(exception);

    // When
    String result = converter.throwableProxyToString(proxy);

    // Then
    assertThat(result).contains("RuntimeException: Wrapper exception");
    assertThat(result)
      .contains(
        "Caused by: java.lang.IllegalArgumentException: Invalid argument"
      );
  }

  @Test
  @DisplayName("Deve retornar string vazia para proxy null")
  void shouldReturnEmptyStringForNullProxy() {
    // When
    String result = converter.throwableProxyToString(null);

    // Then
    assertThat(result).isEmpty();
  }

  private Exception createException() {
    return new RuntimeException("Test exception");
  }

  private Exception createExceptionWithSpringFrameworkStack() {
    // Create an exception that would typically have Spring framework classes in
    // stack
    try {
      throw new RuntimeException("Test with framework stack");
    } catch (RuntimeException e) {
      return e;
    }
  }
}
