/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public Module hibernateModule() {
    Hibernate6Module mod = new Hibernate6Module();
    mod.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
    mod.configure(
      Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS,
      true
    );
    mod.configure(
      Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS,
      true
    );
    return mod;
  }
}
