package com.mopl.moplwebsocketsse.domain.directMessage.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class ConversationAlreadyExistsException extends BaseException {

	public ConversationAlreadyExistsException() {
		super(ErrorCode.CONVERSATION_ALREADY_EXISTS);
	}
}