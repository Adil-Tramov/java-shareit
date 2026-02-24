package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleResponseStatusException(final ResponseStatusException e) {
        log.error("Ошибка {}: {}", e.getStatusCode(), e.getReason());
        return new ResponseEntity<>(
                Map.of("error", e.getReason()),
                e.getStatusCode()
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(
                Map.of("error", errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleThrowable(final Throwable e) {
        log.error("Ошибка 500: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                Map.of("error", "Произошла непредвиденная ошибка"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}