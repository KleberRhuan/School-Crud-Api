/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.user.domain.repository;

import com.kleberrhuan.houer.common.domain.repository.SoftDeleteRepository;
import com.kleberrhuan.houer.user.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UserRepository extends SoftDeleteRepository<User, Long> {
  @EntityGraph(value = "User.roles", type = EntityGraph.EntityGraphType.LOAD)
  Optional<User> findByEmailIgnoreCase(String email);
}
