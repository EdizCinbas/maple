package dev.ediz.maple.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound() {
        return "404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError() {
        return "404";
    }
}


