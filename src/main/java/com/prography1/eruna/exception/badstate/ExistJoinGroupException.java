package com.prography1.eruna.exception.badstate;

import com.prography1.eruna.response.BaseResponseStatus;

public class ExistJoinGroupException extends BadStateException{
    public ExistJoinGroupException(BaseResponseStatus status) {
        super(status);
    }

    public ExistJoinGroupException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
