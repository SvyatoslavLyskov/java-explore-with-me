package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static ru.practicum.utils.DateTimeFormat.DATE_TIME_FORMATTER;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = getFieldErrorMessage(e);
        logError(e);
        return ResponseEntity.badRequest().body(createResponseApiError(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResponseApiError> handleNotFoundException(NotFoundException e) {
        logError(e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createResponseApiError(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseApiError> handleConstraintViolationException(ConstraintViolationException e) {
        logError(e);
        return ResponseEntity.badRequest()
                .body(createResponseApiError(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(UnsupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseApiError> handleUnsupportedException(UnsupportedException e) {
        logError(e);
        return ResponseEntity.badRequest()
                .body(createResponseApiError(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseApiError> handleConflictException(ConflictException e) {
        logError(e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createResponseApiError(HttpStatus.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseApiError> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logError(e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createResponseApiError(HttpStatus.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseApiError> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        logError(e);
        return ResponseEntity.badRequest()
                .body(createResponseApiError(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseApiError> handleThrowable(Throwable e) {
        logError((Exception) e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    private String getFieldErrorMessage(MethodArgumentNotValidException e) {
        if (e.hasFieldErrors()) {
            return e.getFieldErrors().stream()
                    .map(fieldError ->
                            "Field: " + fieldError.getField() +
                                    ". Error: " + fieldError.getDefaultMessage() +
                                    ". Value: " + fieldError.getRejectedValue())
                    .collect(Collectors.joining(". "));
        }
        return e.getMessage();
    }

    private void logError(Exception e) {
        log.error("Exception occurred: {}", e.getMessage(), e);
    }

    private ResponseApiError createResponseApiError(HttpStatus status, String message) {
        return new ResponseApiError(status, "An error occurred.", message, LocalDateTime.now().format(formatter));
    }
}