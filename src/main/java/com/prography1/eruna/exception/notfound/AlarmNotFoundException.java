package com.prography1.eruna.exception.notfound;

import com.prography1.eruna.response.BaseResponseStatus;

public class AlarmNotFoundException extends NotFoundException{
    public AlarmNotFoundException(BaseResponseStatus status) {
        super(status);
    }

    public AlarmNotFoundException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
