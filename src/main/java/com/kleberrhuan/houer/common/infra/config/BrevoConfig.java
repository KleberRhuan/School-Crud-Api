/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.kleberrhuan.houer.common.infra.adapter.notification.client.brevo.BrevoApi;
import com.kleberrhuan.houer.common.infra.properties.BrevoProps;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Validated
public class BrevoConfig {

  @Bean(name = "brevoRestClient")
  RestClient brevoRestClient(
    RestClient.Builder builder,
    @Valid BrevoProps props
  ) {
    HttpComponentsClientHttpRequestFactory factory =
      new HttpComponentsClientHttpRequestFactory();

    factory.setConnectTimeout(Duration.ofSeconds(props.connectTimeout()));
    factory.setReadTimeout(Duration.ofSeconds(props.readTimeout()));

    return builder
      .baseUrl(props.url())
      .defaultHeader("api-key", props.apiKey())
      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .requestFactory(factory)
      .build();
  }

  @Bean(name = "brevoProxyFactory")
  HttpServiceProxyFactory brevoProxyFactory(RestClient client) {
    return HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(client))
      .build();
  }

  @Bean
  BrevoApi brevoApi(HttpServiceProxyFactory factory) {
    return factory.createClient(BrevoApi.class);
  }
}
