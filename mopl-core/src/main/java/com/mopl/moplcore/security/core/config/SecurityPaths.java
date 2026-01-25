package com.mopl.moplcore.security.core.config;

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

	private static final String[] AUTH_ENDPOINTS = {
		"/api/auth/**",
		"/oauth2/**",
		"/login/**"
	};

	private static final String[] OTHER_PUBLIC = {
		"/error",
		"/uploads/**",
		"/.well-known/**",
		"/default-ui.css",
		"/actuator/**",
		"/api/health"
	};

	public static class MethodSpecific {
		public static final String[] POST_ONLY = {
			"/api/users",
			"/api/auth/refresh"
		};
	}

	public static final String[] PUBLIC_PATHS = combineArrays(
		STATIC_RESOURCES,
		API_DOCS,
		AUTH_ENDPOINTS,
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
		if (isPublicPath(requestPath)) {
			return true;
		}

		if (HttpMethod.POST.matches(method)) {
			for (String pattern : MethodSpecific.POST_ONLY) {
				if (PATH_MATCHER.match(pattern, requestPath)) {
					return true;
				}
			}
		}

		return false;
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