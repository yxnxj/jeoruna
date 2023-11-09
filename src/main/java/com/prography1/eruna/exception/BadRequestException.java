package com.prography1.eruna.exception;

import com.prography1.eruna.response.BaseResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class BadRequestException extends RuntimeException{
    private final BaseResponseStatus status;
}
