package com.example.animalworld.exception;

import com.example.animalworld.model.dto.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Shamrikova Tatiana
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WorldNotFoundByIdException.class)
    public ResponseEntity<ApiErrorDto> handleWorldNotFoundById(
            WorldNotFoundByIdException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({
            SpeciesNotFoundByIdException.class,
            PlantSpeciesNotFoundByIdException.class
    })
    public ResponseEntity<ApiErrorDto> handleReferenceNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String message = details.isEmpty() ? "Validation failed" : details.toString();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                extractMostRelevantMessage(exception, "Request violates database constraints")
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            BadSqlGrammarException.class
    })
    public ResponseEntity<ApiErrorDto> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private ResponseEntity<ApiErrorDto> buildErrorResponse(
            HttpStatus status,
            String message
    ) {
        ApiErrorDto body = new ApiErrorDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        return ResponseEntity.status(status).body(body);
    }

    private String extractMostRelevantMessage(Throwable throwable, String fallback) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException.getMessage();
            }
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                fallback = current.getMessage();
            }
            current = current.getCause();
        }
        return fallback;
    }
}
