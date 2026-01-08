package com.mopl.moplcore.security.principal;

import com.mopl.moplcore.domain.user.dto.UserDto;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@EqualsAndHashCode(of = "userDto")
@Getter
@AllArgsConstructor
public class MoplUserDetails implements UserDetails {

  private UserDto userDto;
  private String password;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role().name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return userDto.email();
  }

  @Override
  public boolean isAccountNonLocked() {
    return !userDto.locked();
  }

  @Override
  public boolean isEnabled() {
    return !userDto.locked();
  }

  public void setPasswordEnable(){
    this.password = null;
  }

}
