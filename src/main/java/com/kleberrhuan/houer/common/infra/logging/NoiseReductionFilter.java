/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filtro para reduzir ruído nos logs, excluindo mensagens debug repetitivas e logs de infraestrutura desnecessários.
 */
public class NoiseReductionFilter extends Filter<ILoggingEvent> {

  private static final String[] NOISY_MESSAGES = {
    "Creating instance of bean",
    "Autowiring by type from bean name",
    "Returning cached instance of singleton bean",
    "Mapped URL path",
    "Mapped to handler",
    "Looking up handler method",
    "Using handler adapter",
    "View name:",
    "Mapped to ResourceHttpRequestHandler",
    "HTTP headers:",
    "Accept:",
    "User-Agent:",
    "Connection:",
    "Host:",
    "Accept-Encoding:",
    "Accept-Language:",
    "Cache-Control:",
    "Cookie:",
    "Referer:",
    "Reading",
    "Writing",
    "Processing request",
    "Processing response",
  };

  private static final String[] NOISY_LOGGERS = {
    "org.springframework.web.servlet.mvc.method.annotation",
    "org.springframework.web.servlet.handler",
    "org.springframework.security.web",
    "org.springframework.batch.core.step.tasklet",
    "org.springframework.batch.repeat",
    "org.springframework.transaction",
    "org.springframework.orm.jpa",
    "org.hibernate.SQL",
    "org.hibernate.type",
    "org.springframework.boot.web.servlet",
    "org.apache.tomcat",
    "org.apache.catalina",
  };

  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
      return FilterReply.NEUTRAL;
    }

    if (
      event.getLevel().isGreaterOrEqual(Level.DEBUG) &&
      event.getLevel().toInt() < Level.INFO.toInt()
    ) {
      String loggerName = event.getLoggerName();
      for (String noisyLogger : NOISY_LOGGERS) {
        if (loggerName.startsWith(noisyLogger)) {
          return FilterReply.DENY;
        }
      }
    }

    if (Level.INFO.equals(event.getLevel())) {
      String message = event.getFormattedMessage();
      if (message != null) {
        for (String noisyMessage : NOISY_MESSAGES) {
          if (message.contains(noisyMessage)) {
            return FilterReply.DENY;
          }
        }
      }
    }

    return FilterReply.NEUTRAL;
  }
}
