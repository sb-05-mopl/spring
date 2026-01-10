package com.mopl.moplcore.security.auth.dto;

public interface OAuth2UserInfo {
	String getProviderId();
	String getEmail();
	String getName();
	String getPicture();
}
