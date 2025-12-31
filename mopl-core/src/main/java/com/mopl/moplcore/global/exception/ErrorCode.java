package com.mopl.moplcore.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ErrorCode(HttpStatus, message)
 * http와 관련 없으면 500번대 코드 사용
 * 커스텀 코드를 사용할 지는 결정해야함, 프론트 구조가 어떤 지 모르겠음
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

	// Review 관련 에러 코드
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
	INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1.0 ~ 5.0 사이여야 합니다"),
	FORBIDDEN_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정/삭제할 수 있습니다"),
	INVALID_REVIEW_TEXT(HttpStatus.BAD_REQUEST, "리뷰 내용은 공백일 수 없습니다"),
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 콘텐츠에 리뷰를 작성하셨습니다"),

  PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다"),
  PLAYLIST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "플레이리스트 생성 한도를 초과했습니다"),


  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),

  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 유효하지 않습니다."),

  WEBSOCKET_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WebSocket 오류");


  private final HttpStatus httpStatus;
  private final String message;
}
