package com.mopl.moplwebsocketsse.security.jwt;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.user.entity.Role;
import com.mopl.moplwebsocketsse.security.exception.InValidAccessTokenException;
import com.mopl.moplwebsocketsse.security.registry.JwtRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtRegistry jwtRegistry;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			String header = accessor.getFirstNativeHeader("Authorization");

			if (header == null || !header.startsWith("Bearer ")) {
				log.warn("WebSocket 연결 실패: Authorization 헤더 없음 또는 형식 오류");
				throw new IllegalArgumentException("Missing or invalid Authorization header");
			}

			String token = header.substring("Bearer ".length());

			if (!jwtTokenProvider.validateAccessToken(token)) {
				log.warn("WebSocket 연결 실패: 유효하지 않은 JWT 토큰");
				throw new InValidAccessTokenException("유효하지 않은 액세스 토큰");
			}

			if (!jwtRegistry.hasActiveJwtInformationByAccessToken(token))
				throw new InValidAccessTokenException("유효하지 않은 액세스토큰");

			UUID userId = jwtTokenProvider.getUserId(token);
			Role role = jwtTokenProvider.getRole(token);

			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
				authorities);

			accessor.setUser(authentication);

			log.debug("WebSocket 인증 성공 - UserId: {}, Role: {}", userId, role);
		}

		return message;
	}
}
