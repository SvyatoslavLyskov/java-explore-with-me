package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError validationException(ValidationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage());
        return createResponseApiError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage());
    }

    private ResponseApiError createResponseApiError(HttpStatus status, String errorTitle, String errorMessage) {
        return new ResponseApiError(
                status,
                errorTitle,
                errorMessage,
                LocalDateTime.now()
        );
    }
}
