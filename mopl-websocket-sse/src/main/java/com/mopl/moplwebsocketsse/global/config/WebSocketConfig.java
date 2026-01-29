package com.mopl.moplwebsocketsse.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import com.mopl.moplwebsocketsse.domain.watch.interceptor.WatchingSessionHeartbeatInterceptor;
import com.mopl.moplwebsocketsse.domain.watch.registry.WebSocketSessionDecorator;
import com.mopl.moplwebsocketsse.domain.watch.registry.WebSocketSessionHolder;
import com.mopl.moplwebsocketsse.security.jwt.JwtChannelInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtChannelInterceptor jwtChannelInterceptor;
	private final ThreadPoolTaskScheduler wsHeartbeatScheduler;
	private final WatchingSessionHeartbeatInterceptor watchingSessionHeartbeatInterceptor;
	private final WebSocketSessionHolder webSocketSessionHolder;

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
			.setHeartbeatValue(new long[] {10000L, 10000L})
			.setTaskScheduler(wsHeartbeatScheduler);
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(
			jwtChannelInterceptor,
			watchingSessionHeartbeatInterceptor
		);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(
			handler -> new WebSocketSessionDecorator(handler, webSocketSessionHolder)
		);
	}
}