/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.controller;

import com.kleberrhuan.houer.auth.application.job.TokenCleanupJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(
  name = "Admin - Token Management",
  description = "Endpoints administrativos para gerenciamento de tokens"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AdminTokenController {

  private final TokenCleanupJob tokenCleanupJob;

  @PostMapping("/cleanup")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
    summary = "Executar limpeza manual de tokens expirados",
    description = "Executa manualmente a limpeza de todos os tokens expirados (password reset, refresh tokens, verification tokens)"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Limpeza executada com sucesso"
  )
  @ApiResponse(
    responseCode = "403",
    description = "Acesso negado - requer privilégios de administrador"
  )
  @ApiResponse(
    responseCode = "500",
    description = "Erro interno durante a limpeza"
  )
  public ResponseEntity<String> manualCleanup() {
    log.info("Limpeza manual de tokens solicitada por administrador");

    try {
      tokenCleanupJob.cleanupExpiredTokens();
      return ResponseEntity.ok("Limpeza de tokens executada com sucesso");
    } catch (Exception e) {
      log.error("Erro durante limpeza manual de tokens", e);
      return ResponseEntity
        .internalServerError()
        .body("Erro durante a limpeza: " + e.getMessage());
    }
  }
}
