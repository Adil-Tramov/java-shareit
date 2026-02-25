package ru.yandex.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.error("Validation error: {}", errors);

        String firstError = errors.values().stream().findFirst().orElse("Ошибка валидации");
        Map<String, String> error = new HashMap<>();
        error.put("error", firstError);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // Обработка HTTP статусов от сервера - ЭТО РЕШАЕТ ПРОБЛЕМУ 403
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, String>> handleHttpStatusCodeException(HttpStatusCodeException ex) {
        log.error("HTTP error from server: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        Map<String, String> error = new HashMap<>();

        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                // Пытаемся извлечь сообщение об ошибке из JSON
                error.put("error", responseBody);
            } else {
                error.put("error", ex.getStatusText());
            }
        } catch (Exception e) {
            error.put("error", ex.getStatusText());
        }

        // Возвращаем ТОТ ЖЕ СТАТУС, который пришел от сервера (404, 400 и т.д.)
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Произошла внутренняя ошибка сервера");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}