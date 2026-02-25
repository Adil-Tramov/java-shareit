package ru.yandex.practicum.shareit.exception;

public class UnsupportedStateException extends RuntimeException {
    public UnsupportedStateException(String message) {
        super(message);
    }
}