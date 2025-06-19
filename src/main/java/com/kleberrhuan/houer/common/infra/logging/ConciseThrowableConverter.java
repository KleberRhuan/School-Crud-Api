/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.logging;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * Converter personalizado para tornar stack traces mais concisos. Limita o número de linhas exibidas e filtra pacotes
 * irrelevantes.
 */
public class ConciseThrowableConverter extends ExtendedThrowableProxyConverter {

  private static final int DEFAULT_MAX_LINES = 5;
  private static final String[] EXCLUDED_PACKAGES = {
    "sun.",
    "java.lang.reflect.",
    "org.springframework.cglib.",
    "org.springframework.security.",
    "org.springframework.web.filter.",
    "org.springframework.web.servlet.",
    "org.springframework.batch.core.step.tasklet.",
    "org.springframework.batch.repeat.",
    "org.springframework.transaction.",
    "org.apache.tomcat.",
    "org.apache.catalina.",
    "$Proxy",
  };

  @Override
  protected String throwableProxyToString(IThrowableProxy tp) {
    if (tp == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    sb
      .append(tp.getClassName())
      .append(": ")
      .append(tp.getMessage())
      .append(CoreConstants.LINE_SEPARATOR);

    int maxLines = getMaxLines();

    StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
    if (stepArray != null) {
      int lineCount = 0;
      for (StackTraceElementProxy step : stepArray) {
        if (lineCount >= maxLines) {
          sb
            .append("\t... (")
            .append(stepArray.length - lineCount)
            .append(" more lines)")
            .append(CoreConstants.LINE_SEPARATOR);
          break;
        }

        String stepStr = step.toString();
        if (shouldIncludeStackTraceLine(stepStr)) {
          sb
            .append("\tat ")
            .append(stepStr)
            .append(CoreConstants.LINE_SEPARATOR);
          lineCount++;
        }
      }
    }

    IThrowableProxy nestedTP = tp.getCause();
    if (nestedTP != null) {
      sb.append("Caused by: ");
      sb
        .append(nestedTP.getClassName())
        .append(": ")
        .append(nestedTP.getMessage())
        .append(CoreConstants.LINE_SEPARATOR);

      StackTraceElementProxy[] causeSteps =
        nestedTP.getStackTraceElementProxyArray();
      if (causeSteps != null && causeSteps.length > 0) {
        int causeLinesShown = 0;
        for (StackTraceElementProxy step : causeSteps) {
          if (causeLinesShown >= 2) break;
          String stepStr = step.toString();
          if (shouldIncludeStackTraceLine(stepStr)) {
            sb
              .append("\tat ")
              .append(stepStr)
              .append(CoreConstants.LINE_SEPARATOR);
            causeLinesShown++;
          }
        }
      }
    }

    return sb.toString();
  }

  private int getMaxLines() {
    String option = getFirstOption();
    if (option != null) {
      try {
        return Integer.parseInt(option);
      } catch (NumberFormatException e) {
        // Ignore
      }
    }
    return DEFAULT_MAX_LINES;
  }

  private boolean shouldIncludeStackTraceLine(String line) {
    for (String excludedPackage : EXCLUDED_PACKAGES) {
      if (line.contains(excludedPackage)) {
        return false;
      }
    }
    return true;
  }
}
