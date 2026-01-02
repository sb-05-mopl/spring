package com.mopl.moplcore.domain.content.exception;

import java.util.UUID;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class ContentNotFoundException extends BaseException {
    public ContentNotFoundException(UUID id) {
        super(ErrorCode.CONTENT_NOT_FOUND);
        addDetail("contentId", id);
    }
}