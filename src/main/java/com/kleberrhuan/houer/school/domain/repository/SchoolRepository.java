/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.school.domain.repository;

import com.kleberrhuan.houer.school.domain.model.School;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repositório para operações relacionadas às escolas. */
@Repository
public interface SchoolRepository
  extends JpaRepository<School, Long>, JpaSpecificationExecutor<School> {
  @EntityGraph("School.withMetrics")
  @NotNull
  Page<School> findAll(Specification<School> spec, @NotNull Pageable pageable);

  @EntityGraph(attributePaths = "schoolMetrics")
  Optional<School> findWithMetricsByCode(Long code);
}
