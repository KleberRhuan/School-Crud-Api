/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.user.domain.model;

import com.kleberrhuan.houer.auth.domain.model.Role;
import com.kleberrhuan.houer.common.infra.persistence.SoftDeletableAuditable;
import com.kleberrhuan.houer.common.infra.persistence.converter.LowerCaseConverter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
  name = "users",
  schema = "account",
  uniqueConstraints = @UniqueConstraint(
    name = "uk_users_email",
    columnNames = "email"
  )
)
@Getter
@Setter
@NamedEntityGraph(
  name = "User.roles",
  attributeNodes = @NamedAttributeNode("roles")
)
@SQLRestriction("enabled = true")
public class User extends SoftDeletableAuditable<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Convert(converter = LowerCaseConverter.class)
  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
    name = "user_roles",
    schema = "account",
    joinColumns = @JoinColumn(name = "user_id")
  )
  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  private Set<Role> roles = new HashSet<>();

  private boolean enabled;
}
