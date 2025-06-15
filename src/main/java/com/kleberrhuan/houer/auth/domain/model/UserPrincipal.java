/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kleberrhuan.houer.user.domain.model.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(
  Long id,
  String username,
  @JsonIgnore String password,
  Set<GrantedAuthority> authorities,
  boolean enabled,
  boolean accountNonLocked
)
  implements UserDetails, Serializable {
  public static UserPrincipal from(User u) {
    Set<GrantedAuthority> auth = u
      .getRoles()
      .stream()
      .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
      .collect(Collectors.toUnmodifiableSet());

    return new UserPrincipal(
      u.getId(),
      u.getEmail(),
      u.getPasswordHash(),
      auth,
      u.isEnabled(),
      !u.isDeleted()
    );
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  /* UserDetails default impls */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}
