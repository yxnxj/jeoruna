package com.prography1.eruna.exception.invalid;

import com.prography1.eruna.response.BaseResponseStatus;

public class InvalidFCMTokenException extends ValidationException {
    public InvalidFCMTokenException(BaseResponseStatus status) {
        super(status);
    }

    public InvalidFCMTokenException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
