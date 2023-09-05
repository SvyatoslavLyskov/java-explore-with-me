package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError methodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder messageBuilder = new StringBuilder();
        if (e.hasFieldErrors()) {
            for (FieldError fieldError : e.getFieldErrors()) {
                messageBuilder.append("Field: {}, Error: {}, Value: {}. ")
                        .append(fieldError.getField())
                        .append(fieldError.getDefaultMessage())
                        .append(fieldError.getRejectedValue());
            }
        } else {
            messageBuilder.append(e.getMessage());
        }

        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.", messageBuilder.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseApiError notFoundException(NotFoundException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.NOT_FOUND,
                "The required object was not found.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError constraintViolationException(ConstraintViolationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.BAD_REQUEST,
                "Integrity constraint has been violated.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError unsupportedStateException(UnsupportedException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseApiError conflictException(ConflictException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseApiError dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.CONFLICT,
                "Integrity constraint has been violated.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseApiError throwableException(Throwable e) {
        log.error("{}: {}", e.getStackTrace()[0].getMethodName(), e.getMessage(), e);
        return createResponseApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An internal server error has occurred.", e.getMessage());
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
