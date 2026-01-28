package com.mopl.moplwebsocketsse.domain.watch.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mopl.moplwebsocketsse.domain.watch.repository.WatchingSessionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionGhostCleaner {

	private final WatchingSessionRepository repository;

	@PostConstruct
	@Scheduled(cron = "0 0 2 * * *")
	public void cleanup() {
		try {
			int removed = repository.cleanupGhostEntries();
			if (removed > 0) {
				log.info("[GhostCleaner] Removed {} ghost entries", removed);
			}
		} catch (Exception e) {
			log.error("[GhostCleaner] Failed to cleanup ghost entries", e);
		}
	}
}