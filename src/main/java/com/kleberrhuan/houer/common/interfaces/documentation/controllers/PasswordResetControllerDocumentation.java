/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.controllers;

import com.kleberrhuan.houer.auth.interfaces.dto.request.ForgotPasswordRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ResetPasswordRequest;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ErrorResponseSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ForgotPasswordRequestSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ResetPasswordRequestSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/** Interface de documentação para endpoints de redefinição de senha. */
@Tag(
  name = "Redefinição de Senha",
  description = "Endpoints para redefinição de senha via e-mail"
)
public interface PasswordResetControllerDocumentation {
  @Operation(
    summary = "Solicitar redefinição de senha",
    description = """
      Envia um e-mail com link para redefinir senha para o endereço informado.\n\n          • Sempre retorna HTTP 202 (Accepted), independente do e-mail existir ou não, para evitar enumeração de usuários.\n          • Se o e-mail existir, um token de redefinição é gerado e enviado por e-mail.\n          • O token tem validade configurável (padrão: 15 minutos) e pode ser usado apenas uma vez.\n          • Rate limiting aplicado para prevenir abuso.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "202",
        description = "Solicitação processada com sucesso (sempre retorna este código)"
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(
        responseCode = "429",
        description = "Muitas tentativas - rate limit excedido",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<Void> forgotPassword(
    @RequestBody(
      description = "E-mail para envio do link de redefinição",
      required = true,
      content = @Content(
        schema = @Schema(implementation = ForgotPasswordRequestSchema.class),
        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
          name = "Esqueci Senha Exemplo",
          summary = "Exemplo de payload para solicitar redefinição",
          value = "{\n  \"email\": \"usuario@example.com\"\n}"
        )
      )
    ) @Valid ForgotPasswordRequest request
  );

  @Operation(
    summary = "Redefinir senha",
    description = """
      Redefine a senha do usuário utilizando o token recebido por e-mail.\n\n          • O token deve ser válido, não expirado e não utilizado anteriormente.\n          • Após o uso, o token é marcado como utilizado e não pode ser reutilizado.\n          • A nova senha deve atender aos critérios de segurança (mínimo 8 caracteres).\n          • Rate limiting aplicado para prevenir ataques de força bruta.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "204",
        description = "Senha redefinida com sucesso"
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(
        responseCode = "410",
        description = "Token inválido, expirado ou já utilizado",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(
        responseCode = "429",
        description = "Muitas tentativas - rate limit excedido",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<Void> resetPassword(
    @RequestBody(
      description = "Token e nova senha para redefinição",
      required = true,
      content = @Content(
        schema = @Schema(implementation = ResetPasswordRequestSchema.class),
        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
          name = "Reset Senha Exemplo",
          summary = "Exemplo de payload para redefinir senha",
          value = "{\n  \"token\": \"abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890abcdef123\",\n  \"newPassword\": \"NovaSenha123!\"\n}"
        )
      )
    ) @Valid ResetPasswordRequest request
  );

  @Operation(
    summary = "Validar token de redefinição",
    description = """
      Verifica se um token de redefinição de senha é válido e ainda pode ser utilizado.\n\n          • Útil para validação na interface do usuário antes de exibir o formulário de nova senha.\n          • Retorna HTTP 200 se o token for válido.\n          • Retorna HTTP 410 se o token for inválido, expirado ou já utilizado.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Token válido e pode ser utilizado"
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(
        responseCode = "410",
        description = "Token inválido, expirado ou já utilizado",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<Void> validateResetToken(
    @Parameter(
      description = "Token de redefinição recebido por e-mail",
      required = true,
      example = "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890abcdef123"
    ) @RequestParam String token
  );
}
