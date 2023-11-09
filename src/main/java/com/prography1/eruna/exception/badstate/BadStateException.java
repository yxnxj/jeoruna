package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.exception.BadRequestException;
import com.prography1.eruna.response.BaseResponseStatus;

public class BadStateException extends BadRequestException {
    public BadStateException(BaseResponseStatus status) {
        super(status);
    }
}
