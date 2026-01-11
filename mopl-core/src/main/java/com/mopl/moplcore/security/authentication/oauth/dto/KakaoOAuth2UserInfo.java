package com.mopl.moplcore.security.authentication.oauth.dto;

import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

	private Map<String, Object> attributes;

	@Override
	public String getName() {
		Map<String, Object> props = (Map<String, Object>)attributes.get("properties");
		return props == null ? null : String.valueOf(props.get("nickname"));
	}

	@Override
	public String getPicture() {
		Map<String, Object> props = (Map<String, Object>)attributes.get("properties");
		return props == null ? null : String.valueOf(props.get("profile_image"));
	}

	@Override
	public String getProviderId() {
		Object id = attributes.get("id");
		return id == null ? null : String.valueOf(id);
	}

	@Override
	public String getEmail() {
		return getProviderId() + "@kakao.com";
	}

}
