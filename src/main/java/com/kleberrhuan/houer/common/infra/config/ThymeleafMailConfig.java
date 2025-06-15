/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafMailConfig {

  @Bean
  public SpringResourceTemplateResolver mailTemplateResolver() {
    SpringResourceTemplateResolver r = new SpringResourceTemplateResolver();
    r.setPrefix("classpath:/templates/email/");
    r.setSuffix(".html");
    r.setTemplateMode(TemplateMode.HTML);
    r.setCharacterEncoding("UTF-8");
    r.setCacheable(true);
    return r;
  }

  @Bean
  public SpringTemplateEngine mailTemplateEngine(
    SpringResourceTemplateResolver mailTemplateResolver
  ) {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(mailTemplateResolver);
    return engine;
  }
}
