package br.edu.utfpr.tac.cognito.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {

        Map<String, Object> body = Map.of(
                "error", true,
                "message", ex.getMessage(),
                "status", ex.getStatus().value(),
                "timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {

        Map<String, Object> body = Map.of(
                "error", true,
                "message", ex.getMessage(),
                "status", 401,
                "timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {

        Map<String, Object> body = Map.of(
                "error", true,
                "message", "Erro interno",
                "status", 500,
                "timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}