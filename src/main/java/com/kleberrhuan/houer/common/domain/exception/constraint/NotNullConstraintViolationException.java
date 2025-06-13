/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma violação de restrição de não-nulidade no banco de dados.
 *
 * <p>Esta exceção é lançada quando uma operação tenta inserir ou atualizar um registro com valor NULL em uma coluna que
 * possui restrição NOT NULL. Geralmente, isso acontece quando camadas de validação são contornadas ou quando validações
 * automáticas não capturam adequadamente a ausência de valores obrigatórios.
 *
 * <p>Cenários comuns de ocorrência:
 *
 * <ul>
 *   <li>Falha em validações de API ou UI que permitem dados incompletos
 *   <li>Bugs em código de aplicação que não definem valores obrigatórios
 *   <li>Migrações ou operações em massa que não lidam adequadamente com valores nulos
 * </ul>
 *
 * <p>A exceção retorna um status HTTP 400 (Bad Request) e contém informações sobre qual coluna não pode receber valor
 * nulo.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see ConstraintType#NOT_NULL
 */
public class NotNullConstraintViolationException
  extends ConstraintViolationException {

  /**
   * Constrói uma nova exceção de violação de restrição NOT NULL.
   *
   * @param column O nome da coluna que não pode receber valor nulo
   */
  public NotNullConstraintViolationException(String column) {
    super(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("constraint.notNull"),
      column
    );
  }
}
