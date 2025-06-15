/* (C)2025 Ludilens */
package com.ludilens.esdrasresearch.common.infra.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

public class SoftDeleteRepositoryFactory extends JpaRepositoryFactory {

  SoftDeleteRepositoryFactory(EntityManager em) {
    super(em);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected JpaRepositoryImplementation<?, ?> getTargetRepository(
    RepositoryInformation information,
    EntityManager em
  ) {
    Class<?> domain = information.getDomainType();
    JpaEntityInformation entityInfo = getEntityInformation(domain);

    if (SoftDeletableAuditable.class.isAssignableFrom(domain)) {
      return new SoftDeleteRepositoryImpl<>(entityInfo, em);
    }
    return super.getTargetRepository(information, em);
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    if (
      SoftDeletableAuditable.class.isAssignableFrom(metadata.getDomainType())
    ) {
      return SoftDeleteRepositoryImpl.class;
    }
    return super.getRepositoryBaseClass(metadata);
  }
}
