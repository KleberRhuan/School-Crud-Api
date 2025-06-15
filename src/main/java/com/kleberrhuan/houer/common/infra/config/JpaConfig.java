/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.common.infra.config;

import com.kleberrhuan.houer.common.infra.persistence.CustomHibernateJpaDialect;
import com.kleberrhuan.houer.common.infra.persistence.softdelete.SoftDeleteRepositoryFactoryBean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaDialect;

@EnableJpaRepositories(
  basePackages = "com.kleberrhuan.houer",
  repositoryFactoryBeanClass = SoftDeleteRepositoryFactoryBean.class
)
@Configuration
@RequiredArgsConstructor
public class JpaConfig {

  private final CustomHibernateJpaDialect hibernateDialect;

  @Bean
  public JpaDialect jpaDialect() {
    return hibernateDialect;
  }
}
