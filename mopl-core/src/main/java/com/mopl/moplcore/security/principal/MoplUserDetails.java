package com.mopl.moplcore.security.principal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.descriptor.jdbc.OracleJsonBlobJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.mopl.moplcore.domain.user.dto.UserDto;
import com.mopl.moplcore.domain.user.entity.AuthProvider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "userDto")
@Getter
@AllArgsConstructor
public class MoplUserDetails implements UserDetails, OAuth2User, OidcUser {

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

	@Override
	public Map<String, Object> getAttributes() {
		return Map.of();
	}

	@Override
	public String getName() {
		return String.valueOf(userDto.getName());
	}

	@Override
	public Map<String, Object> getClaims() {
		return Map.of();
	}

	@Override
	public OidcUserInfo getUserInfo() {
		return null;
	}

	@Override
	public OidcIdToken getIdToken() {
		return null;
	}
}
