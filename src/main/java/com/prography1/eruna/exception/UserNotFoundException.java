package com.prography1.eruna.exception;

import com.prography1.eruna.response.BaseResponseStatus;

public class UserNotFoundException extends ValidationException{
    public UserNotFoundException(BaseResponseStatus status) {
        super(status);
    }

    public UserNotFoundException(BaseResponseStatus status, String message) {
        super(status, message);
    }
}
