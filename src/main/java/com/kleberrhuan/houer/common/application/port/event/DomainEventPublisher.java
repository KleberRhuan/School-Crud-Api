/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.event;

@FunctionalInterface
public interface DomainEventPublisher {
  <T> void publish(T event);
}
