/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {
  void softDeleteById(ID id);

  void softDelete(T entity);
}
