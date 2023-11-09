package com.prography1.eruna.exception.invalid;

import com.prography1.eruna.response.BaseResponseStatus;

public class InvalidGroupCodeException extends ValidationException {
    public InvalidGroupCodeException(BaseResponseStatus status) {
        super(status);
    }

    public InvalidGroupCodeException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
