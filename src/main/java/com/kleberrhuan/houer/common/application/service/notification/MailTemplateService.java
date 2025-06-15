/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.service.notification;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class MailTemplateService {

  private final SpringTemplateEngine mailTemplateEngine;

  public String render(String name, Map<String, Object> model) {
    Context ctx = new Context(Locale.getDefault(), model);
    return mailTemplateEngine.process(name, ctx);
  }
}
