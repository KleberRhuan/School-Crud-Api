/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.kleberrhuan.houer.user.domain.model.User;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

  private User dummy() {
    User u = new User();
    u.setId(1L);
    u.setEmail("admin@test.com");
    u.setPasswordHash("hash");
    u.setEnabled(true);
    u.getRoles().add(Role.ADMIN);
    u.getRoles().add(Role.CLIENT);
    return u;
  }

  @Test
  @DisplayName("deve mapear User para UserPrincipal com authorities corretas")
  void givenUser_whenFrom_thenMapAuthorities() {
    User u = dummy();

    UserPrincipal principal = UserPrincipal.from(u);

    assertThat(principal.getUsername()).isEqualTo(u.getEmail());
    assertThat(principal.getPassword()).isEqualTo(u.getPasswordHash());
    assertThat(principal.isEnabled()).isTrue();
    assertThat(principal.isAccountNonLocked()).isTrue();

    Set<String> roles = principal
      .getAuthorities()
      .stream()
      .map(Object::toString)
      .collect(java.util.stream.Collectors.toSet());

    assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CLIENT");
  }
}
