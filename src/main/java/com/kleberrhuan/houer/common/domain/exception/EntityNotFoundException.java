/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando uma entidade não é encontrada no sistema.
 *
 * <p>Esta exceção é lançada quando ocorre uma tentativa de acessar ou manipular uma entidade que não existe no sistema,
 * identificada por um ID ou outro identificador único. Ela é mapeada para o código HTTP 404 (Not Found), indicando que
 * o recurso solicitado não pôde ser encontrado.
 *
 * <p>Casos de uso comuns incluem:
 *
 * <ul>
 *   <li>Tentativa de buscar um usuário por ID inexistente
 *   <li>Tentativa de atualizar um registro que foi excluído
 *   <li>Tentativa de acessar um recurso em uma URL com identificador inválido
 * </ul>
 *
 * <p>Esta classe pode ser estendida para criar exceções específicas para entidades particulares, como
 * {@code UserNotFoundException}, {@code ProductNotFoundException}, etc., permitindo tratamento mais granular e
 * mensagens mais específicas.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see BusinessException
 * @see com.ludilens.esdrasresearch.user.domain.exception.UserNotFoundException
 */
public class EntityNotFoundException extends BusinessException {

  /**
   * Constrói uma nova exceção de entidade não encontrada.
   *
   * <p>Este construtor configura a exceção com status HTTP 404 (Not Found) e tipo de erro RESOURCE_NOT_FOUND. A
   * mensagem de erro será construída usando a chave "entity.notFound" e os argumentos fornecidos, que tipicamente
   * incluem o nome da entidade e o ID ou identificador que não foi encontrado.
   *
   * @param entity O nome da entidade que não foi encontrada (ex: "Usuário", "Produto")
   * @param id O identificador da entidade que foi buscado (ex: UUID, código, email)
   */
  public EntityNotFoundException(String entity, Object id) {
    super(
      HttpStatus.NOT_FOUND,
      ApiErrorType.RESOURCE_NOT_FOUND,
      MessageKey.of("entity.notFound"),
      entity,
      id
    );
  }
}
