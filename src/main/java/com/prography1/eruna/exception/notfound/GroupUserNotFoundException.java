package com.prography1.eruna.exception.notfound;

import com.prography1.eruna.response.BaseResponseStatus;

public class GroupUserNotFoundException extends NotFoundException{
    public GroupUserNotFoundException(BaseResponseStatus status) {
        super(status);
    }

    public GroupUserNotFoundException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
