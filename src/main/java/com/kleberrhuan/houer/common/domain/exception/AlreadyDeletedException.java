/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando há tentativa de manipular um recurso que já foi excluído.
 *
 * <p>Esta exceção ocorre quando um usuário ou processo tenta realizar uma operação (como editar, excluir ou acessar) em
 * um recurso que já foi marcado como excluído no sistema, seja por exclusão lógica (soft delete) ou física.
 *
 * <p>O uso desta exceção ajuda a distinguir entre entidades que não existem (tratadas por
 * {@link EntityNotFoundException}) e entidades que existiam, mas foram excluídas (tratadas por esta exceção),
 * permitindo feedback mais preciso ao usuário.
 *
 * <p>Cenários comuns de uso:
 *
 * <ul>
 *   <li>Tentativa de atualizar um registro já excluído
 *   <li>Tentativa de excluir novamente um recurso já excluído
 *   <li>Tentativa de associar entidades a recursos excluídos
 * </ul>
 *
 * <p>A exceção retorna um status HTTP 409 (Conflict) porque representa um conflito com o estado atual do recurso no
 * servidor.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see BusinessException
 * @see EntityNotFoundException
 */
public class AlreadyDeletedException extends BusinessException {

  /**
   * Constrói uma nova exceção de recurso já excluído.
   *
   * <p>Este construtor configura a exceção com status HTTP 409 (Conflict) e tipo de erro ALREADY_DELETED. A mensagem
   * de erro será construída usando a chave "entity.alreadyDeleted" e os argumentos fornecidos, que incluem o tipo de
   * item e seu identificador.
   *
   * @param item O tipo de recurso ou entidade que já foi excluído (ex: "Usuário", "Produto")
   * @param id O identificador do recurso excluído
   */
  public AlreadyDeletedException(String item, String id) {
    super(
      HttpStatus.CONFLICT,
      ApiErrorType.ALREADY_DELETED,
      MessageKey.of("entity.alreadyDeleted"),
      item,
      id
    );
  }
}
