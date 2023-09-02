package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseApiError unsupportedStateException(ValidationException e) {
        log.error(e.getStackTrace()[0].getMethodName() + ": " + e.getMessage());
        return new ResponseApiError(
                HttpStatus.BAD_REQUEST,
                "Некорректный запрос.",
                e.getMessage(),
                LocalDateTime.now().format(FORMATTER)
        );
    }
}