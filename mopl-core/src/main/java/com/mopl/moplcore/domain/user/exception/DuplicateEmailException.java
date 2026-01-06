package com.mopl.moplcore.domain.user.exception;

import com.mopl.moplcore.global.exception.BaseException;
import com.mopl.moplcore.global.exception.ErrorCode;

public class DuplicateEmailException extends BaseException {

  public DuplicateEmailException() {
    super(ErrorCode.DUPLICATE_EMAIL);
  }

  public static DuplicateEmailException withEmail(String email) {
    DuplicateEmailException exception = new DuplicateEmailException();
    exception.addDetail("email", email);
    return exception;
  }
}
