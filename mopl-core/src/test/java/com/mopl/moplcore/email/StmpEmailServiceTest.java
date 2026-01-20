package com.mopl.moplcore.email;

import java.time.Instant;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.mopl.moplcore.global.service.SmtpEmailService;

public class StmpEmailServiceTest {

	@Test
	void sendTemporaryPasswordTest() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("halogiju123@gmail.com");
		mailSender.setPassword("");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.connectiontimeout", "5000");
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.writetimeout", "5000");

		SmtpEmailService emailService = new SmtpEmailService(mailSender);

		String testEmail = "halogiju123@gmail.com";
		String tempPassword = "Temp1234!@#$";
		Instant baseDate = Instant.now();

		// emailService.sendTemporaryPassword(testEmail, tempPassword, baseDate);
		// emailService.sendTemporaryPassword(testEmail, tempPassword, baseDate);

		System.out.println("이메일 전송 완료!");
	}
}