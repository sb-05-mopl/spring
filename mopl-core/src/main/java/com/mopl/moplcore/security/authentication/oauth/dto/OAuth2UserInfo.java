package com.mopl.moplcore.security.authentication.oauth.dto;

public interface OAuth2UserInfo {
	String getProviderId();

	String getEmail();

	String getName();

	String getPicture();
}
