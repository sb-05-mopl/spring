package com.mopl.moplwebsocketsse.domain.directMessage.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class SelfConversationNotAllowedException extends BaseException {
	public SelfConversationNotAllowedException() {
		super(ErrorCode.SELF_CONVERSATION_NOT_ALLOWED);
	}
}
