package com.mopl.moplcore.global.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Deprecated
@Profile("prod")
@Service
@RequiredArgsConstructor
public class AwsSesEmailService implements EmailService{

	private final AmazonSimpleEmailService amazonSES;

	private static final String FROM_EMAIL = "halogiju123@gmail.com";
	public static final long EXPIRE_TIME_MS = 180000;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
		.ofPattern("yyyy년 MM월 dd일 HH:mm:ss")
		.withZone(ZoneId.of("Asia/Seoul"));


	@Override
	public void sendTemporaryPassword(String to, String temporaryPassword, Instant baseDate) {

		Instant expiresAt = baseDate.plusMillis(EXPIRE_TIME_MS);
		String subject = "[Mopl] 임시 비밀번호가 발급되었습니다";
		String htmlContent = createTemporaryPasswordHtml(temporaryPassword, expiresAt);

		sendEmail(to, subject, htmlContent);
		log.info("Temporary password email sent to: {}, expires at: {}", to, expiresAt);
	}

	private void sendEmail(String to, String subject, String htmlContent) {
		try {
			SendEmailRequest request = new SendEmailRequest()
				.withDestination(new Destination().withToAddresses(to))
				.withMessage(new Message()
					.withBody(new Body()
						.withHtml(new Content()
							.withCharset("UTF-8")
							.withData(htmlContent)))
					.withSubject(new Content()
						.withCharset("UTF-8")
						.withData(subject)))
				.withSource(FROM_EMAIL);

			amazonSES.sendEmail(request);
			log.info("Email sent successfully to: {}", to);

		} catch (Exception e) {
			log.error("Failed to send email to: {}", to, e);
			throw new RuntimeException("Failed to send email", e);
		}
	}


	private String createTemporaryPasswordHtml(String temporaryPassword, Instant expiresAt) {
		String formattedExpiresAt = FORMATTER.format(expiresAt);
		long minutesRemaining = java.time.Duration.between(Instant.now(), expiresAt).toMinutes();

		return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .container {
                        background-color: #f9f9f9;
                        border: 1px solid #ddd;
                        border-radius: 8px;
                        padding: 30px;
                    }
                    h2 {
                        color: #2c3e50;
                        margin-top: 0;
                    }
                    .password-box {
                        background-color: #fff;
                        border: 2px solid #3498db;
                        border-radius: 5px;
                        padding: 20px;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .password {
                        font-size: 24px;
                        font-weight: bold;
                        color: #2c3e50;
                        letter-spacing: 2px;
                        font-family: 'Courier New', monospace;
                    }
                    .info-box {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .expires-info {
                        color: #856404;
                        font-weight: bold;
                    }
                    .warning {
                        color: #d9534f;
                        font-size: 14px;
                        margin-top: 20px;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #777;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>임시 비밀번호 발급</h2>
                    <p>안녕하세요,</p>
                    <p>요청하신 임시 비밀번호가 발급되었습니다.</p>
                    
                    <div class="password-box">
                        <p style="margin: 0; font-size: 14px; color: #777;">임시 비밀번호</p>
                        <p class="password">%s</p>
                    </div>
                    
                    <div class="info-box">
                        <p class="expires-info">⏰ 만료 시간: %s</p>
                        <p style="margin: 5px 0 0 0;">(%d분 후 만료)</p>
                    </div>
                    
                    <h3>사용 방법</h3>
                    <ol>
                        <li>로그인 페이지로 이동하세요</li>
                        <li>위의 임시 비밀번호로 로그인하세요</li>
                        <li>로그인 후 반드시 비밀번호를 변경해주세요</li>
                    </ol>
                    
                    <div class="warning">
                         보안 주의사항:
                        <ul style="margin: 10px 0;">
                            <li>이 비밀번호는 일회성입니다</li>
                            <li>만료 시간 내에만 사용 가능합니다</li>
                            <li>타인에게 절대 공유하지 마세요</li>
                            <li>로그인 후 즉시 비밀번호를 변경하세요</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                        <p>&copy; 2026 Mopl. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
			temporaryPassword,
			formattedExpiresAt,
			minutesRemaining
		);
	}
}