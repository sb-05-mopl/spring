package com.mopl.moplcore.domain.user.listener;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mopl.moplcore.domain.user.event.RemoveTempPasswordEvent;
import com.mopl.moplcore.domain.user.event.SendEmailPasswordEvent;
import com.mopl.moplcore.domain.user.registry.UserRegistry;
import com.mopl.moplcore.global.service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserListener {

	private final UserRegistry userRegistry;
	private final EmailService emailService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void sendEmail(SendEmailPasswordEvent event){
		emailService.sendTemporaryPassword(event.getToEmail(), event.getTempPassword(), event.getBaseTime());
	}

	@TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
	public void removeTempPassword(RemoveTempPasswordEvent event){
		userRegistry.removeTempPassword(event.getUserId());
	}
}
