/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

class MailTemplateServiceTest {

  private static MailTemplateService service;

  @BeforeAll
  static void setup() {
    StringTemplateResolver resolver = new StringTemplateResolver();
    resolver.setTemplateMode("HTML");
    resolver.setCacheable(false);
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    service = new MailTemplateService(engine);
  }

  @Test
  @DisplayName("deve substituir placeholders no template")
  void shouldRenderTemplate() {
    String tpl = "Olá [[${name}]]! Seu código é [[${code}]].";
    String result = service.render(tpl, Map.of("name", "João", "code", 1234));

    assertThat(result).isEqualTo("Olá João! Seu código é 1234.");
  }

  @Test
  @DisplayName("deve lidar com variáveis ausentes retornando vazio")
  void shouldHandleMissingVariables() {
    String tpl = "Oi [[${name}]]";
    String result = service.render(tpl, Map.of());
    assertThat(result).isEqualTo("Oi ");
  }
}
