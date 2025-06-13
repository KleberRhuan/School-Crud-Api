/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma violação de restrição de chave estrangeira.
 *
 * <p>Esta exceção é lançada quando uma operação tenta:
 *
 * <ul>
 *   <li>Inserir um registro com referência a uma entidade que não existe
 *   <li>Excluir um registro referenciado por outros registros (dependendo da política de deleção)
 *   <li>Atualizar uma chave que quebraria a integridade referencial
 * </ul>
 *
 * <p>Situações comuns:
 *
 * <ul>
 *   <li>Tentativa de associar um item a uma categoria inexistente
 *   <li>Tentativa de excluir um usuário que possui registros associados
 *   <li>Tentativa de alterar um ID referenciado por outras tabelas
 * </ul>
 *
 * <p>A exceção retorna um status HTTP 409 (Conflict) por se tratar de um conflito de integridade referencial nos dados.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see ConstraintType#FOREIGN_KEY
 */
public class ForeignKeyConstraintViolationException
  extends ConstraintViolationException {

  /**
   * Constrói uma nova exceção de violação de chave estrangeira.
   *
   * @param constraint O nome da restrição de chave estrangeira violada ou uma descrição amigável da relação
   */
  public ForeignKeyConstraintViolationException(String constraint) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("constraint.foreignKey"),
      constraint
    );
  }
}
