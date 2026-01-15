package com.mopl.moplwebsocketsse.domain.content.exception;

import java.util.UUID;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class ContentNotFoundException extends BaseException {

	public ContentNotFoundException() {
		super(ErrorCode.CONTENT_NOT_FOUND);
	}

	public static ContentNotFoundException withContentId(UUID contentId) {
		ContentNotFoundException exception = new ContentNotFoundException();
		exception.addDetail("contentId", contentId);
		return exception;
	}
}
