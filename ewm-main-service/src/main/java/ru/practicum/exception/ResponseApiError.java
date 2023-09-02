package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class ResponseApiError {
    private HttpStatus status;

    private String reason;

    private String message;

    private String timestamp;
}