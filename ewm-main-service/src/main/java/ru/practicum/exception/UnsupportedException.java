package ru.practicum.exception;

public class UnsupportedException extends RuntimeException {
    public UnsupportedException(String message) {
        super(message);
    }
}