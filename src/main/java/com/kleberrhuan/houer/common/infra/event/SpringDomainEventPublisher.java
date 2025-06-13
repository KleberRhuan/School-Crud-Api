/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.event;

import com.kleberrhuan.houer.common.application.port.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher springPublisher;

  @Override
  public <T> void publish(T event) {
    springPublisher.publishEvent(event);
  }
}
