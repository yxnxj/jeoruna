package com.prography1.eruna.web;

import com.prography1.eruna.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseStatusException handleBaseException(BadRequestException e) {
        log.warn(e.getStatus().toString());
        log.warn(e.getStatus().getMessage());
        log.warn(String.valueOf(e.getStatus().getCode()));
//        e.printStackTrace();
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getStatus().getMessage(), e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseStatusException handleRuntimeException(RuntimeException e) {
        log.error(e.toString());
        log.error(e.getMessage());
        e.printStackTrace();
        return new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

}
