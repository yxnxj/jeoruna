package com.prography1.eruna.exception.notfound;

import com.prography1.eruna.response.BaseResponseStatus;

public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(BaseResponseStatus status) {
        super(status);
    }

    public GroupNotFoundException(BaseResponseStatus status, String message) {
        super(status);
        status.setMessage(message);
    }
}
