/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import java.util.Locale;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class LocaleConfig {

  @Bean
  @Primary
  public ReloadableResourceBundleMessageSource messageSource() {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(new ClassPathResource("i18n/messages_pt_BR.yml"));
    Properties props = factory.getObject();

    var ms = new ReloadableResourceBundleMessageSource();
    ms.setCommonMessages(props);
    ms.setBasenames("classpath:i18n/messages");
    ms.setDefaultEncoding("UTF-8");
    ms.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
    ms.setFallbackToSystemLocale(false);
    return ms;
  }
}
