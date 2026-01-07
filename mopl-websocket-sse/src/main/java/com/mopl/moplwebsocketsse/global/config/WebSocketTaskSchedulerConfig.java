package com.mopl.moplwebsocketsse.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WebSocketTaskSchedulerConfig {

	@Bean(name = "wsHeartbeatScheduler")
	public ThreadPoolTaskScheduler wsHeartbeatScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("websocket-heartbeat-");
		scheduler.initialize();
		return scheduler;
	}
}