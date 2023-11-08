package com.prography1.eruna.exception;

import com.prography1.eruna.response.BaseException;
import com.prography1.eruna.response.BaseResponseStatus;
import lombok.Setter;

public class ValidationException extends BaseException {
    @Setter
    String message;
//    BaseResponseStatus baseResponseStatus;

    public ValidationException(BaseResponseStatus status) {
        super(status);
        this.message = status.getMessage();
    }

    public ValidationException(BaseResponseStatus status, String message) {
        super(status);
        this.message = message;
    }
}
