package com.mopl.moplcore.security.principal;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.AuthProvider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "userDto")
@Getter
@AllArgsConstructor
public class MoplUserDetails implements UserDetails {

	private UserDto userDto;
	private String password;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.getRole().name()));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return userDto.getName();
	}

	@Override
	public boolean isAccountNonLocked() {
		return !userDto.isLocked();
	}

	@Override
	public boolean isEnabled() {
		return !userDto.isLocked();
	}

	public void setPasswordEnable() {
		this.password = null;
	}

	public AuthProvider getAuthProvider(){
		return this.userDto.getProvider();
	}

}
