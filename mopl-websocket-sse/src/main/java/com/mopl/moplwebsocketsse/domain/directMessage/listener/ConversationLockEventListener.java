package com.mopl.moplwebsocketsse.domain.directMessage.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mopl.moplwebsocketsse.domain.directMessage.event.ConversationCreatedLockEvent;
import com.mopl.moplwebsocketsse.domain.directMessage.service.ConversationLockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationLockEventListener {

	private final ConversationLockService lockService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
	public void onTransactionComplete(ConversationCreatedLockEvent event) {
		lockService.unlock(event.key(), event.value());
		log.debug("[ConversationLockEventListener] Lock released. key={}", event.key());
	}
}