/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.domain.exception;

import com.ludilens.esdrasresearch.common.interfaces.dto.error.ApiErrorType;
import com.ludilens.esdrasresearch.common.interfaces.dto.error.MessageKey;
import org.springframework.http.HttpStatus;

/**
 * Exceção lançada quando o acesso a um recurso é negado por razões de autorização.
 *
 * <p>Esta exceção é utilizada quando um usuário autenticado tenta acessar um recurso ou executar uma operação para a
 * qual não possui as permissões necessárias. Diferente de problemas de autenticação (401 Unauthorized), esta exceção é
 * utilizada quando o usuário está autenticado, mas não autorizado para a ação específica.
 *
 * <p>Situações comuns onde esta exceção é lançada:
 *
 * <ul>
 *   <li>Tentativa de acessar dados de outro usuário
 *   <li>Tentativa de modificar recursos sem privilégios administrativos
 *   <li>Tentativa de executar operações restritas a certos perfis ou roles
 *   <li>Violação de regras de controle de acesso baseado em atributos (ABAC)
 * </ul>
 *
 * <p>Por razões de segurança, esta exceção utiliza uma mensagem genérica e não revela detalhes específicos sobre as
 * razões da negação de acesso, para evitar vazamento de informações sobre a estrutura de permissões do sistema.
 *
 * @author Kleber Rhuan
 * @company Ludilens
 * @version 1.0
 * @since 1.0
 * @see BusinessException
 * @see com.ludilens.esdrasresearch.common.domain.exception.BusinessException
 */
public class ForbiddenException extends BusinessException {

  /**
   * Constrói uma nova exceção de acesso proibido.
   *
   * <p>Este construtor configura a exceção com status HTTP 403 (Forbidden) e tipo de erro FORBIDDEN. A mensagem de
   * erro será construída usando a chave "error.forbidden" sem argumentos adicionais, resultando em uma mensagem
   * genérica para evitar vazamento de informações de segurança.
   *
   * <p>Note que, intencionalmente, esta exceção não recebe parâmetros específicos sobre o recurso ou permissão
   * negada, para não revelar a estrutura de permissões do sistema a potenciais atacantes.
   */
  public ForbiddenException() {
    super(
      HttpStatus.FORBIDDEN,
      ApiErrorType.FORBIDDEN,
      MessageKey.of("error.forbidden")
    );
  }
}
