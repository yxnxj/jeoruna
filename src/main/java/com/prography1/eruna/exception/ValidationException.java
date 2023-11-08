package com.prography1.eruna.exception;

import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;

public class ValidationException extends BaseException {

    public ValidationException(BaseResponseStatus status) {
        super(status);
    }
}
