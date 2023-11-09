package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class DuplicationNicknameException extends BadStateException {
    public DuplicationNicknameException(BaseResponseStatus status) {
        super(status);
    }

    public DuplicationNicknameException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
