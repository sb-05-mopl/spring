package com.mopl.moplcore.security.oauth;

import java.util.Map;

import com.mopl.moplcore.security.auth.dto.GoogleOAuth2UserInfo;
import com.mopl.moplcore.security.auth.dto.KakaoOAuth2UserInfo;
import com.mopl.moplcore.security.auth.dto.OAuth2UserInfo;
import com.mopl.moplcore.security.exception.UnSupportedOAuthException;

public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes){
		if ("google".equalsIgnoreCase(registrationId)) {
			return new GoogleOAuth2UserInfo(attributes);
		} else if ("kakao".equalsIgnoreCase(registrationId)) {
			return new KakaoOAuth2UserInfo(attributes);
		} else {
			throw UnSupportedOAuthException.withProvider(registrationId);
		}
	}
}
