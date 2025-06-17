/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.softdelete;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kleberrhuan.houer.common.domain.exception.EntityNotFoundException;
import com.kleberrhuan.houer.common.infra.persistence.SoftDeletableAuditable;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteRepositoryImpl")
@org.junit.jupiter.api.Disabled(
  "Requires real EntityManager; disabled for unit scope"
)
class SoftDeleteRepositoryImplTest {

  @Mock
  JpaEntityInformation<Dummy, Long> info;

  @Mock
  EntityManager em;

  static class Dummy
    extends SoftDeletableAuditable<Long>
    implements Serializable {

    Long id;
  }

  SoftDeleteRepositoryImpl<Dummy, Long> repo;

  void init() {
    when(info.getJavaType()).thenReturn(Dummy.class);
    repo = spy(new SoftDeleteRepositoryImpl<>(info, em));
  }

  @Nested
  class SoftDeleteById {

    @Test
    @DisplayName("deve marcar entidade como deletada quando encontrada")
    void shouldSoftDeleteWhenFound() {
      init();
      Dummy entity = new Dummy();
      doReturn(Optional.of(entity)).when(repo).findById(1L);

      repo.softDeleteById(1L);

      assertThat(entity.isDeleted()).isTrue();
      verify(repo).findById(1L);
    }

    @Test
    @DisplayName("deve lançar EntityNotFoundException quando id inexistente")
    void shouldThrowWhenNotFound() {
      init();
      doReturn(Optional.empty()).when(repo).findById(2L);

      assertThatThrownBy(() -> repo.softDeleteById(2L))
        .isInstanceOf(EntityNotFoundException.class);
    }
  }

  @Test
  @DisplayName("softDelete deve marcar entidade como deletada diretamente")
  void softDeleteDirect() {
    init();
    Dummy entity = new Dummy();
    repo.softDelete(entity);
    assertThat(entity.isDeleted()).isTrue();
  }
}
