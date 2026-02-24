package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return Map.of("error", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error("Ошибка 400: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResponseStatusException(final ResponseStatusException e) {
        log.error("Ошибка {}: {}", e.getStatusCode(), e.getReason());
        return Map.of("error", e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(final RuntimeException e) {
        log.error("Ошибка 409: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowable(final Throwable e) {
        log.error("Ошибка 500: {}", e.getMessage(), e);
        return Map.of("error", "Произошла непредвиденная ошибка");
    }
}