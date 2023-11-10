package com.prography1.eruna.exception;

import com.prography1.eruna.response.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SchedulerException extends RuntimeException {
    private final BaseResponseStatus status;
}
