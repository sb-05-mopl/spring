package com.mopl.moplwebsocketsse.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ErrorCode(HttpStatus, message)
 * http와 관련 없으면 500번대 코드 사용
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
	ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "계정이 잠겨있습니다"),

	CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "콘텐츠를 찾을 수 없습니다"),

	PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다"),
	PLAYLIST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "플레이리스트 생성 한도를 초과했습니다"),

	CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "대화를 찾을 수 없습니다"),
	NOT_CONVERSATION_PARTICIPANT(HttpStatus.FORBIDDEN, "대화 참여자가 아닙니다"),
	SELF_CONVERSATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신과 대화할 수 없습니다"),

	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),

	WEBSOCKET_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WebSocket 오류");

	private final HttpStatus httpStatus;
	private final String message;
}
