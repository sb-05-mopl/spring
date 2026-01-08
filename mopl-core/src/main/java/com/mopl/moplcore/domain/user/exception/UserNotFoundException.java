package com.mopl.moplcore.domain.user.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;
import java.util.UUID;

public class UserNotFoundException extends BaseException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public static UserNotFoundException withUserId(UUID userId) {
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("userId", userId);
    return exception;
  }

  public static UserNotFoundException withEmail(String email){
    UserNotFoundException exception = new UserNotFoundException();
    exception.addDetail("email", email);
    exception.addDetail("message", "invalid email");
    return exception;
  }

}
