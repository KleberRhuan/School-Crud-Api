/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.interfaces.web;

import com.kleberrhuan.houer.auth.application.service.PasswordResetService;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ForgotPasswordRequest;
import com.kleberrhuan.houer.auth.interfaces.dto.request.ResetPasswordRequest;
import com.kleberrhuan.houer.common.interfaces.documentation.controllers.PasswordResetControllerDocumentation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController
  implements PasswordResetControllerDocumentation {

  private final PasswordResetService passwordResetService;

  @PostMapping("/forgot")
  public ResponseEntity<Void> forgotPassword(
    @Valid @RequestBody ForgotPasswordRequest request
  ) {
    passwordResetService.requestReset(request);
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/reset")
  public ResponseEntity<Void> resetPassword(
    @Valid @RequestBody ResetPasswordRequest request
  ) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reset/token")
  public ResponseEntity<Void> validateResetToken(@RequestParam String token) {
    boolean isValid = passwordResetService.isTokenValid(token);
    return isValid
      ? ResponseEntity.ok().build()
      : ResponseEntity.status(HttpStatus.GONE).build();
  }
}
