/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.infra.persistence;

import com.ludilens.esdrasresearch.common.domain.exception.EntityNotFoundException;
import com.ludilens.esdrasresearch.common.domain.repository.SoftDeleteRepository;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class SoftDeleteRepositoryImpl<
  T extends SoftDeletableAuditable<ID>, ID extends Serializable
>
  extends SimpleJpaRepository<T, ID>
  implements SoftDeleteRepository<T, ID> {

  public SoftDeleteRepositoryImpl(
    JpaEntityInformation<T, ID> entityInformation,
    EntityManager em
  ) {
    super(entityInformation, em);
  }

  @Override
  @Transactional
  public void softDeleteById(ID id) {
    String entityName = getDomainClass().getSimpleName();
    T entity = findById(id)
      .orElseThrow(() -> new EntityNotFoundException(entityName, id));
    entity.delete();
  }

  @Override
  @Transactional
  public void softDelete(T entity) {
    entity.delete();
  }
}
