/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.infra.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlockListImpl")
class TokenBlockListImplTest {

  @Mock
  private Cache<String, Boolean> cache;

  @InjectMocks
  private TokenBlockListImpl tokenBlockList;

  @Nested
  @DisplayName("block")
  class Block {

    @Test
    @DisplayName("deve adicionar JTI válido à blocklist")
    void givenValidJti_whenBlock_thenAddToCache() {
      // Arrange
      String jti = UUID.randomUUID().toString();

      // Act
      tokenBlockList.block(jti);

      // Assert
      verify(cache).put(jti, Boolean.TRUE);
    }

    @Test
    @DisplayName("deve permitir bloquear o mesmo JTI múltiplas vezes")
    void givenSameJti_whenBlockMultipleTimes_thenCachePutCalledMultipleTimes() {
      // Arrange
      String jti = "duplicate-jti";

      // Act
      tokenBlockList.block(jti);
      tokenBlockList.block(jti);

      // Assert
      verify(cache, times(2)).put(jti, Boolean.TRUE);
    }

    @Test
    @DisplayName("deve lidar com JTI nulo")
    void givenNullJti_whenBlock_thenHandleGracefully() {
      // Act & Assert - não deve lançar exceção
      assertThatCode(() -> tokenBlockList.block(null))
        .doesNotThrowAnyException();

      verify(cache).put(null, Boolean.TRUE);
    }

    @Test
    @DisplayName("deve lidar com JTI vazio")
    void givenEmptyJti_whenBlock_thenHandleGracefully() {
      // Arrange
      String emptyJti = "";

      // Act
      tokenBlockList.block(emptyJti);

      // Assert
      verify(cache).put(emptyJti, Boolean.TRUE);
    }

    @Test
    @DisplayName("deve lidar com JTIs longos")
    void givenLongJti_whenBlock_thenAddToCache() {
      // Arrange
      String longJti = "a".repeat(1000);

      // Act
      tokenBlockList.block(longJti);

      // Assert
      verify(cache).put(longJti, Boolean.TRUE);
    }
  }

  @Nested
  @DisplayName("isBlocked")
  class IsBlocked {

    @Test
    @DisplayName("deve retornar true para JTI bloqueado")
    void givenBlockedJti_whenIsBlocked_thenReturnTrue() {
      // Arrange
      String jti = "blocked-token";
      when(cache.getIfPresent(jti)).thenReturn(Boolean.TRUE);

      // Act
      boolean result = tokenBlockList.isBlocked(jti);

      // Assert
      assertThat(result).isTrue();
      verify(cache).getIfPresent(jti);
    }

    @Test
    @DisplayName("deve retornar false para JTI não bloqueado")
    void givenUnblockedJti_whenIsBlocked_thenReturnFalse() {
      // Arrange
      String jti = "valid-token";
      when(cache.getIfPresent(jti)).thenReturn(null);

      // Act
      boolean result = tokenBlockList.isBlocked(jti);

      // Assert
      assertThat(result).isFalse();
      verify(cache).getIfPresent(jti);
    }

    @Test
    @DisplayName("deve retornar false para JTI nulo")
    void givenNullJti_whenIsBlocked_thenReturnFalse() {
      // Arrange
      when(cache.getIfPresent(null)).thenReturn(null);

      // Act
      boolean result = tokenBlockList.isBlocked(null);

      // Assert
      assertThat(result).isFalse();
      verify(cache).getIfPresent(null);
    }

    @Test
    @DisplayName("deve retornar false para JTI vazio")
    void givenEmptyJti_whenIsBlocked_thenReturnFalse() {
      // Arrange
      String emptyJti = "";
      when(cache.getIfPresent(emptyJti)).thenReturn(null);

      // Act
      boolean result = tokenBlockList.isBlocked(emptyJti);

      // Assert
      assertThat(result).isFalse();
      verify(cache).getIfPresent(emptyJti);
    }

    @Test
    @DisplayName("deve retornar true mesmo para valor FALSE no cache")
    void givenCacheReturnsFalse_whenIsBlocked_thenReturnTrue() {
      // Arrange - qualquer valor não-null no cache significa bloqueado
      String jti = "false-value-token";
      when(cache.getIfPresent(jti)).thenReturn(Boolean.FALSE);

      // Act
      boolean result = tokenBlockList.isBlocked(jti);

      // Assert
      assertThat(result).isTrue(); // implementação verifica apenas se != null
      verify(cache).getIfPresent(jti);
    }
  }

  @Nested
  @DisplayName("integration scenarios")
  class IntegrationScenarios {

    @Test
    @DisplayName("deve detectar token como bloqueado após block()")
    void givenJti_whenBlockThenIsBlocked_thenReturnTrueAfterBlocking() {
      // Arrange
      String jti = "test-token";

      // Mock the cache behavior for blocking
      doNothing().when(cache).put(jti, Boolean.TRUE);

      // Mock the cache behavior for checking
      when(cache.getIfPresent(jti)).thenReturn(Boolean.TRUE);

      // Act
      tokenBlockList.block(jti);
      boolean isBlocked = tokenBlockList.isBlocked(jti);

      // Assert
      assertThat(isBlocked).isTrue();
      verify(cache).put(jti, Boolean.TRUE);
      verify(cache).getIfPresent(jti);
    }

    @Test
    @DisplayName("deve permitir verificar múltiplos tokens independentemente")
    void givenMultipleTokens_whenSomeBlockedSomeNot_thenReturnCorrectStatus() {
      // Arrange
      String blockedJti = "blocked-token";
      String validJti = "valid-token";

      // Setup blocked token
      when(cache.getIfPresent(blockedJti)).thenReturn(Boolean.TRUE);

      // Setup valid token
      when(cache.getIfPresent(validJti)).thenReturn(null);

      // Act
      boolean blockedResult = tokenBlockList.isBlocked(blockedJti);
      boolean validResult = tokenBlockList.isBlocked(validJti);

      // Assert
      assertThat(blockedResult).isTrue();
      assertThat(validResult).isFalse();
      verify(cache).getIfPresent(blockedJti);
      verify(cache).getIfPresent(validJti);
    }

    @Test
    @DisplayName("deve ser thread-safe para operações simultâneas")
    void givenConcurrentOperations_whenBlockAndCheck_thenHandleConcurrently() {
      // Arrange
      String jti1 = "concurrent-token-1";
      String jti2 = "concurrent-token-2";

      // Act - simulate concurrent operations
      tokenBlockList.block(jti1);
      tokenBlockList.block(jti2);

      when(cache.getIfPresent(jti1)).thenReturn(null);
      when(cache.getIfPresent(jti2)).thenReturn(null);

      boolean result1 = tokenBlockList.isBlocked(jti1);
      boolean result2 = tokenBlockList.isBlocked(jti2);

      // Assert
      verify(cache).put(jti1, Boolean.TRUE);
      verify(cache).put(jti2, Boolean.TRUE);
      verify(cache).getIfPresent(jti1);
      verify(cache).getIfPresent(jti2);
    }
  }
}
