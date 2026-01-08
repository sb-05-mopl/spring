package com.mopl.moplwebsocketsse.domain.directMessage.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class NotConversationParticipantException extends BaseException {
	public NotConversationParticipantException() {
		super(ErrorCode.NOT_CONVERSATION_PARTICIPANT);
	}
}
