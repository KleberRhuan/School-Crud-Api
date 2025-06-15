/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.infra.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class SoftDeleteRepositoryFactoryBean<R extends Repository<T, ID>, T, ID>
  extends JpaRepositoryFactoryBean<R, T, ID> {

  public SoftDeleteRepositoryFactoryBean(Class<? extends R> repoInterface) {
    super(repoInterface);
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
    return new SoftDeleteRepositoryFactory(em);
  }
}
