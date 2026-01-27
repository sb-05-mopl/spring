package com.mopl.moplcore.security.authentication.oauth.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return findCookie(request);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {
		if (authorizationRequest == null) {
			removeCookie(response);
			return;
		}

		String serialized = serialize(authorizationRequest);
		if (serialized == null) {
			return;
		}

		ResponseCookie cookie = ResponseCookie.from(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(COOKIE_EXPIRE_SECONDS)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
		removeCookie(response);
		return authorizationRequest;
	}

	private void removeCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(OAUTH2_AUTH_REQUEST_COOKIE_NAME, "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.maxAge(0)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private OAuth2AuthorizationRequest findCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (OAUTH2_AUTH_REQUEST_COOKIE_NAME.equals(cookie.getName())) {
				return deserialize(cookie.getValue());
			}
		}
		return null;
	}

	private String serialize(OAuth2AuthorizationRequest request) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(request);
			oos.flush();
			return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
		} catch (Exception e) {
			log.error("Failed to serialize OAuth2AuthorizationRequest", e);
			return null;
		}
	}

	private OAuth2AuthorizationRequest deserialize(String value) {
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(value);
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decoded))) {
				return (OAuth2AuthorizationRequest)ois.readObject();
			}
		} catch (Exception e) {
			log.error("Failed to deserialize OAuth2AuthorizationRequest", e);
			return null;
		}
	}
}
