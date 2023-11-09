package com.prography1.eruna.exception.notfound;

import com.prography1.eruna.exception.BadRequestException;
import com.prography1.eruna.response.BaseResponseStatus;


public class NotFoundException extends BadRequestException {
    public NotFoundException(BaseResponseStatus status) {
        super(status);
    }
}
