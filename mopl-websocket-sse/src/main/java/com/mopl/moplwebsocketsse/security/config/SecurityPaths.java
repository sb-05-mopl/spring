package com.mopl.moplwebsocketsse.security.config;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

public class SecurityPaths {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private static final String[] STATIC_RESOURCES = {
		"/",
		"/index.html",
		"/*.html",
		"/favicon.ico",
		"/assets/**",
		"/vite.svg"
	};

	private static final String[] API_DOCS = {
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/v3/api-docs/**"
	};

	private static final String[] SOCKET_ENDPOINTS = {
		"/ws/**"
	};


	private static final String[] OTHER_PUBLIC = {
		"/actuator/**",
		"/error",
		"/uploads/**",
		"/.well-known/**"
	};

	public static final String[] PUBLIC_PATHS = combineArrays(
		STATIC_RESOURCES,
		API_DOCS,
		SOCKET_ENDPOINTS,
		OTHER_PUBLIC
	);

	public static boolean isPublicPath(String requestPath) {
		for (String pattern : PUBLIC_PATHS) {
			if (PATH_MATCHER.match(pattern, requestPath)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPublicPath(String requestPath, String method) {
		return isPublicPath(requestPath);
	}

	private static String[] combineArrays(String[]... arrays) {
		int totalLength = 0;
		for (String[] array : arrays) {
			totalLength += array.length;
		}

		String[] result = new String[totalLength];
		int currentIndex = 0;

		for (String[] array : arrays) {
			System.arraycopy(array, 0, result, currentIndex, array.length);
			currentIndex += array.length;
		}
		return result;
	}

	private SecurityPaths() {
	}
}