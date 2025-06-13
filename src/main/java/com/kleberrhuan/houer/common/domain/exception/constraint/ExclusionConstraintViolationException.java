/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando ocorre uma violação de restrição de exclusão no banco de dados.
 *
 * <p>Esta exceção é lançada quando uma operação viola uma restrição de exclusão, que é um tipo de restrição mais
 * avançado disponível em bancos como PostgresSQL. Restrições de exclusão garantem que, se duas linhas são comparadas em
 * certas colunas usando operadores específicos, pelo menos uma dessas comparações retornará falso ou nulo.
 *
 * <p>Casos de uso comuns para restrições de exclusão:
 *
 * <ul>
 *   <li>Impedir sobreposição de intervalos de tempo (ex: reservas de salas)
 *   <li>Garantir exclusividade com base em critérios complexos
 *   <li>Implementar restrições espaciais com tipos geométricos (via GiST)
 *   <li>Qualquer cenário onde a unicidade simples não é suficiente
 * </ul>
 *
 * <p>A exceção retorna um status HTTP 409 (Conflict) pois representa um conflito de dados que impede a operação
 * solicitada.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ConstraintViolationException
 * @see ConstraintType#EXCLUSION
 */
public class ExclusionConstraintViolationException
  extends ConstraintViolationException {

  /**
   * Constrói uma nova exceção de violação de restrição de exclusão.
   *
   * @param constraint O nome da restrição de exclusão violada ou uma descrição amigável da condição de exclusão
   */
  public ExclusionConstraintViolationException(String constraint) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.CONSTRAINT_VIOLATION,
      MessageKey.of("constraint.exclusion"),
      constraint
    );
  }
}
