/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.interfaces.documentation.controllers;

import com.kleberrhuan.houer.auth.interfaces.dto.request.LoginRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.RegisterRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.response.TokenResponse;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.ErrorResponseSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.LoginRequestSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.RegisterRequestSchema;
import com.kleberrhuan.houer.common.interfaces.documentation.schemas.TokenResponseSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;

/** Interface de documentação para endpoints de autenticação. */
@Tag(
  name = "Autenticação",
  description = "Endpoints para autenticação e gerenciamento de usuários"
)
public interface AuthControllerDocumentation {
  @Operation(
    summary = "Realizar login",
    description = """
      Autentica um usuário utilizando e-mail e senha e emite um par de tokens JWT.\n\n          • accessToken: JWT assinado, deve ser enviado no cabeçalho Authorization como Bearer Token em chamadas protegidas.\n          • refreshToken: armazenado em cookie httpOnly (REFRESH_TOKEN) e utilizado exclusivamente no endpoint de /refresh para renovar o accessToken.\n\n          O tempo de vida dos tokens varia conforme o campo rememberMe: se true, o refreshToken é válido por 30 dias; caso contrário, 24 h.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Login realizado com sucesso",
        content = @Content(
          schema = @Schema(implementation = TokenResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<TokenResponse> login(
    @RequestBody(
      description = "Dados de login do usuário",
      required = true,
      content = @Content(
        schema = @Schema(implementation = LoginRequestSchema.class),
        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
          name = "Login Exemplo",
          summary = "Exemplo de payload para login",
          value = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"P@ssw0rd!\",\n  \"rememberMe\": false\n}"
        )
      )
    ) @Valid LoginRequest dto,
    HttpServletResponse res
  );

  @Operation(
    summary = "Registrar novo usuário",
    description = """
      Cria uma nova conta de usuário e dispara um e-mail de verificação para o endereço informado.\n\n          O link enviado contém um token UUID que deve ser utilizado no endpoint /verify para ativação da conta.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "201",
        description = "Usuário registrado com sucesso. E-mail de verificação enviado."
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(
        responseCode = "409",
        description = "E-mail já está em uso",
        content = @Content(
          schema = @Schema(implementation = ErrorResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  void register(
    @RequestBody(
      description = "Dados de registro do usuário",
      required = true,
      content = @Content(
        schema = @Schema(implementation = RegisterRequestSchema.class),
        examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
          name = "Registro Exemplo",
          summary = "Exemplo de payload para registro",
          value = "{\n  \"name\": \"João da Silva\",\n  \"email\": \"joao.silva@example.com\",\n  \"password\": \"P@ssw0rd!\"\n}"
        )
      )
    ) @Valid RegisterRequest dto,
    HttpServletRequest req
  );

  @Operation(
    summary = "Verificar e-mail",
    description = """
      Ativa a conta do usuário validando o token recebido por e-mail.\n          Em caso de sucesso, o usuário é redirecionado (HTTP 302) para a página de confirmação no front-end.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "302",
        description = "E-mail verificado com sucesso – redirecionamento para o front-end"
      ),
      @ApiResponse(ref = "#/components/responses/BadRequest"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  void verify(
    @Parameter(
      description = "Token de verificação enviado por email",
      required = true,
      example = "123e4567-e89b-12d3-a456-426614174000"
    ) @RequestParam UUID token,
    HttpServletResponse res
  );

  @Operation(
    summary = "Renovar token de acesso",
    description = """
      Gera um novo accessToken a partir do refreshToken armazenado no cookie httpOnly REFRESH_TOKEN.\n          Caso o refreshToken esteja expirado, o cookie é limpo e é retornado HTTP 401.\n          """
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "200",
        description = "Token renovado com sucesso",
        content = @Content(
          schema = @Schema(implementation = TokenResponseSchema.class)
        )
      ),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  ResponseEntity<TokenResponse> refresh(
    @Parameter(
      description = "Refresh token armazenado em cookie httpOnly",
      required = true,
      example = "eyJhbGciOiJIUzI1NiJ9..."
    ) @CookieValue("REFRESH_TOKEN") String refresh,
    HttpServletResponse res
  );

  @Operation(
    summary = "Realizar logout",
    description = """
      Invalida o accessToken atual, remove o refreshToken associado e limpa o cookie REFRESH_TOKEN.\n          """,
    security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(
      name = "BearerAuth"
    )
  )
  @ApiResponses(
    {
      @ApiResponse(
        responseCode = "204",
        description = "Logout realizado com sucesso"
      ),
      @ApiResponse(ref = "#/components/responses/Unauthorized"),
      @ApiResponse(ref = "#/components/responses/InternalServerError"),
    }
  )
  void logout(
    @Parameter(hidden = true) JwtAuthenticationToken jwt,
    HttpServletResponse res
  );
}
