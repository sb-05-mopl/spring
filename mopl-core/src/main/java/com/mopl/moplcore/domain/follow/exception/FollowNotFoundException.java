package com.mopl.moplcore.domain.follow.exception;

import java.util.UUID;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class FollowNotFoundException extends BaseException {
	public FollowNotFoundException() {
		super(ErrorCode.FOLLOW_NOT_FOUND);
	}

	public static FollowNotFoundException withFollowId(UUID followId) {
		return new FollowNotFoundException();
	}
}