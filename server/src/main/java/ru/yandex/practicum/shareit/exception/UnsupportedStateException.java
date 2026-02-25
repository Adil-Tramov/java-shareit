package ru.yandex.practicum.shareit.exception;

public class UnsupportedStateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnsupportedStateException(String message) {
        super(message);
    }
}