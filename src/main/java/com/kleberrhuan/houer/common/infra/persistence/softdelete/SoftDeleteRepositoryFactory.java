/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.persistence.softdelete;

import com.kleberrhuan.houer.common.infra.persistence.SoftDeletableAuditable;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

public class SoftDeleteRepositoryFactory extends JpaRepositoryFactory {

  SoftDeleteRepositoryFactory(EntityManager em) {
    super(em);
  }

  @NotNull
  @Override
  protected JpaRepositoryImplementation<?, ?> getTargetRepository(
    RepositoryInformation information,
    @NotNull EntityManager em
  ) {
    Class<?> domain = information.getDomainType();
    JpaEntityInformation entityInfo = getEntityInformation(domain);

    if (SoftDeletableAuditable.class.isAssignableFrom(domain)) {
      return new SoftDeleteRepositoryImpl<>(entityInfo, em);
    }
    return super.getTargetRepository(information, em);
  }

  @NotNull
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
