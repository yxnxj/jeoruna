package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class AlreadyGroupUserException extends BadStateException{
    public AlreadyGroupUserException(BaseResponseStatus status) {
        super(status);
    }
    public AlreadyGroupUserException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
