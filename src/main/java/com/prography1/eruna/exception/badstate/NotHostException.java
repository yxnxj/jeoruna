package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class NotHostException extends BadStateException{
    public NotHostException(BaseResponseStatus status) {
        super(status);
    }

    public NotHostException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
