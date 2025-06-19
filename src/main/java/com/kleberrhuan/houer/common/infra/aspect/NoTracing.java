/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para excluir um serviço do tracing automático. Útil para serviços chamados com muita frequência e podem
 * causar lag no logging.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoTracing {
}
