/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma violação de restrição de unicidade no banco de dados.
 *
 * <p>Esta exceção é lançada quando uma operação tenta inserir ou atualizar um registro com um valor que já existe em
 * uma coluna ou conjunto de colunas com restrição de unicidade (UNIQUE) ou chave primária (PRIMARY KEY).
 *
 * <p>Tipicamente, essa exceção ocorre em cenários como:
 *
 * <ul>
 *   <li>Tentativa de cadastro de usuário com email já existente
 *   <li>Tentativa de criação de entidade com código ou identificador duplicado
 *   <li>Violação de qualquer outra restrição de unicidade de negócio
 * </ul>
 *
 * <p>A exceção carrega informações sobre a coluna violada e o valor duplicado, permitindo mensagens de erro
 * contextualizadas para o usuário.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see ConstraintType#UNIQUE
 */
public class UniqueConstraintViolationException
  extends ConstraintViolationException {

  /**
   * Constrói uma nova exceção de violação de restrição de unicidade.
   *
   * <p>Esta exceção é configurada para retornar o status HTTP 409 (Conflict), que é o código mais apropriado para
   * conflitos de dados.
   *
   * @param column O nome da coluna ou restrição que foi violada
   * @param value O valor duplicado que causou a violação
   */
  public UniqueConstraintViolationException(String column, String value) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("constraint.unique"),
      column,
      value
    );
  }
}
