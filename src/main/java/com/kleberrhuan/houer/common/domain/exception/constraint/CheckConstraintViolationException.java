/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma violação de restrição CHECK no banco de dados.
 *
 * <p>Esta exceção é lançada quando uma operação tenta inserir ou atualizar um registro com valores que não satisfazem
 * uma condição boolean definida na restrição CHECK. Restrições CHECK são usadas para impor regras de integridade
 * específicas do domínio diretamente no esquema do banco de dados.
 *
 * <p>Exemplos de restrições CHECK:
 *
 * <ul>
 *   <li>Garantir que um preço seja sempre positivo
 *   <li>Garantir que uma data de término seja posterior à data de início
 *   <li>Limitar valores a um conjunto específico ou intervalo
 *   <li>Implementar lógicas condicionais complexas entre múltiplas colunas
 * </ul>
 *
 * <p>A exceção retorna um status HTTP 400 (Bad Request) pois representa um erro na validação dos dados fornecidos pelo
 * cliente.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see ConstraintType#CHECK
 */
public class CheckConstraintViolationException
  extends ConstraintViolationException {

  /**
   * Constrói uma nova exceção de violação de restrição CHECK.
   *
   * @param constraint O nome da restrição CHECK violada ou uma descrição amigável da condição que falhou
   */
  public CheckConstraintViolationException(String constraint) {
    super(
      HttpStatus.BAD_REQUEST,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("constraint.check"),
      constraint
    );
  }
}
