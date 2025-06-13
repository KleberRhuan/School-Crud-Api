/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.properties;

import com.kleberrhuan.houer.common.interfaces.dto.error.ApiErrorType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.houer")
@Getter
@Setter
@Validated
public class HouerProperties {

  @NotBlank
  private String url;

  @PostConstruct
  public void init() {
    ApiErrorType.setBaseUrl(url);
  }
}
