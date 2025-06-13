/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception;

import com.ludilens.esdrasresearch.common.infra.exception.ApiException;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção base para representar erros de regras de negócio da aplicação.
 *
 * <p>Esta classe estende {@link ApiException} e serve como classe base para todas as exceções específicas de negócio da
 * aplicação. Ela encapsula informações como o código HTTP de resposta, o tipo de erro e a chave da mensagem para
 * internacionalização.
 *
 * <p>Características principais:
 *
 * <ul>
 *   <li>Representa erros de validação de regras de negócio
 *   <li>Permite classificação e categorização uniforme de erros
 *   <li>Suporta internacionalização de mensagens de erro
 *   <li>Facilita a criação de respostas de erro consistentes
 * </ul>
 *
 * <p>Diferente das exceções técnicas (como falhas de infraestrutura ou erros de sistema), as exceções de negócio
 * representam condições que são resultado direto das regras do domínio e são geralmente esperadas e tratadas de forma
 * específica.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see ApiException
 * @see EntityNotFoundException
 * @see AlreadyDeletedException
 * @see ForbiddenException
 */
public class BusinessException extends ApiException {

  /**
   * Constrói uma nova exceção de negócio com os parâmetros especificados.
   *
   * <p>Este construtor permite que subclasses especifiquem detalhes específicos sobre o erro de negócio, incluindo o
   * status HTTP a ser retornado, o tipo de erro categorizado e a chave da mensagem para internacionalização.
   *
   * @param status O código de status HTTP a ser retornado na resposta
   * @param errorType O tipo de erro da API, para categorização
   * @param key A chave de mensagem para internacionalização
   * @param args Argumentos variáveis para substituição na mensagem final
   */
  public BusinessException(
    HttpStatus status,
    ApiErrorType errorType,
    MessageKey key,
    Object... args
  ) {
    super(status, errorType, key, args);
  }
}
