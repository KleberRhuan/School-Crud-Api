/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.application.port.dispatcher;

@FunctionalInterface
public interface DispatcherService<T> {
  void dispatch(T event);
}
