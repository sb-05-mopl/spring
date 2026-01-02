package com.mopl.moplwebsocketsse.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.mopl.moplwebsocketsse.security.jwt.JwtChannelInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtChannelInterceptor jwtChannelInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.withSockJS()
			.setHeartbeatTime(10000);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub")
			.setHeartbeatValue(new long[]{10000L,10000L});
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
	    registration.interceptors(jwtChannelInterceptor);
	}
}