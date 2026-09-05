package com.todoapp.backend.common;

import com.todoapp.backend.common.dto.ErrorResponse;
import com.todoapp.backend.common.exception.NotFoundException;
import com.todoapp.backend.common.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), ex.getFields()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(com.todoapp.backend.common.exception.OverlapException.class)
    public ResponseEntity<com.todoapp.backend.common.dto.OverlapErrorResponse> handleOverlap(
            com.todoapp.backend.common.exception.OverlapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new com.todoapp.backend.common.dto.OverlapErrorResponse(
                        new com.todoapp.backend.common.dto.ConflictingTaskDto(ex.getConflictingTask())));
    }

    @ExceptionHandler(com.todoapp.backend.common.exception.ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(com.todoapp.backend.common.exception.ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage(), null));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        java.util.Map<String, String> fields = new java.util.HashMap<>();
        for (org.springframework.validation.FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Validation failed", fields));
    }
}
