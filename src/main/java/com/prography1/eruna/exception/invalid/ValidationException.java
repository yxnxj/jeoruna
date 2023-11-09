package com.prography1.eruna.exception.invalid;

import com.prography1.eruna.exception.BadRequestException;
import com.prography1.eruna.response.BaseResponseStatus;

public class ValidationException extends BadRequestException {
    public ValidationException(BaseResponseStatus status) {
        super(status);
    }
}
