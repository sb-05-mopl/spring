package com.mopl.moplwebsocketsse.domain.directMessage.exception;

import com.mopl.moplwebsocketsse.global.exception.BaseException;
import com.mopl.moplwebsocketsse.global.exception.ErrorCode;

public class ConversationLockAcquisitionFailedException extends BaseException {
	public ConversationLockAcquisitionFailedException() {
		super(ErrorCode.CONVERSATION_LOCK_ACQUISITION_FAILED);
	}
}
