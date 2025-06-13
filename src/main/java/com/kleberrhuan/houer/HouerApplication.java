/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan("com.kleberrhuan.houer.common.infra.properties")
@SpringBootApplication
public class HouerApplication {

  public static void main(String[] args) {
    SpringApplication.run(HouerApplication.class, args);
  }
}
