package com.mopl.moplwebsocketsse.domain.directMessage.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class ConversationNotFoundException extends BaseException {
	public ConversationNotFoundException() {
		super(ErrorCode.CONVERSATION_NOT_FOUND);
	}
}
