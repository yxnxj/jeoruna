package com.prography1.eruna.exception.notfound;

import com.prography1.eruna.exception.invalid.ValidationException;
import com.prography1.eruna.response.BaseResponseStatus;

public class UserNotFoundException extends ValidationException {
    public UserNotFoundException(BaseResponseStatus status) {
        super(status);
    }

    public UserNotFoundException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
