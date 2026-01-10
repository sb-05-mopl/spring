package com.mopl.moplcore.global.service;

import java.time.Instant;

public interface EmailService {
	void sendTemporaryPassword(String to, String temporaryPassword, Instant baseDate);
}
