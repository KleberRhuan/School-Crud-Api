/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception.constraint;

import com.ludilens.esdrasresearch.common.infra.exception.ApiException;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para violações de restrições de integridade no banco de dados.
 *
 * <p>Esta classe estende {@link ApiException} e serve como classe base para todas as exceções específicas de violação
 * de restrições no banco de dados, como chaves únicas, chaves estrangeiras, restrições de não-nulidade, etc. Ela
 * fornece um mecanismo unificado para representar erros de integridade relacional.
 *
 * <p>As exceções derivadas desta classe são tipicamente lançadas:
 *
 * <ul>
 *   <li>Durante operações de persistência que violam restrições de banco de dados
 *   <li>Por interceptadores de exceções do JPA/Hibernate que detectam violações
 *   <li>Por validações de domínio que ocorrem antes da persistência
 * </ul>
 *
 * <p>Cada tipo de violação é representado por uma subclasse específica, permitindo tratamento adequado e mensagens de
 * erro contextualizadas para o usuário.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see UniqueConstraintViolationException
 * @see NotNullConstraintViolationException
 * @see ForeignKeyConstraintViolationException
 * @see CheckConstraintViolationException
 * @see ExclusionConstraintViolationException
 * @see ConstraintType
 */
public class ConstraintViolationException extends ApiException {

  /**
   * Constrói uma nova exceção de violação de restrição.
   *
   * @param status O código de status HTTP a ser retornado na resposta
   * @param errorType O tipo de erro da API, para categorização
   * @param messageKey A chave de mensagem para internacionalização
   * @param args Argumentos variáveis para substituição na mensagem final
   */
  public ConstraintViolationException(
    HttpStatus status,
    ApiErrorType errorType,
    MessageKey messageKey,
    Object... args
  ) {
    super(status, errorType, messageKey, args);
  }
}
