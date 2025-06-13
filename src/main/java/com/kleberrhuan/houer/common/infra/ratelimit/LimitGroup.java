/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.ratelimit;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

public enum LimitGroup {
  AUTH("/auth/**"),
  GENERAL("/**");

  private final String patternText;
  private final PathPattern pattern;

  LimitGroup(String patternText) {
    this.patternText = patternText;
    this.pattern = PathPatternParser.defaultInstance.parse(patternText);
  }

  public boolean matches(String uri) {
    return pattern.matches(PathContainer.parsePath(uri));
  }
}
