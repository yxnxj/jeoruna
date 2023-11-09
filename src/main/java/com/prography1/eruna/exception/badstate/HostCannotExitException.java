package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class HostCannotExitException extends BadStateException{
    public HostCannotExitException(BaseResponseStatus status) {
        super(status);
    }

    public HostCannotExitException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
