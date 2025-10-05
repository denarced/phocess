package com.denarced.phocess;

import com.denarced.phocess.domain.ErrorContainer;
import com.denarced.phocess.domain.StandardError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorContainer handleInvalidRequest(InvalidRequestException e) {
        return new ErrorContainer(new StandardError(e.getMessage()));
    }
}
