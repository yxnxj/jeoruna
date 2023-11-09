package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class FullMemberException extends BadStateException{

    public FullMemberException(BaseResponseStatus status) {
        super(status);
    }

    public FullMemberException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
