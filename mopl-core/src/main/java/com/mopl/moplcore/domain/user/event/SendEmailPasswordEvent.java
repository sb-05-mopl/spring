package com.mopl.moplcore.domain.user.event;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendEmailPasswordEvent {
	private String toEmail;
	private String tempPassword;
	private Instant baseTime;
}
