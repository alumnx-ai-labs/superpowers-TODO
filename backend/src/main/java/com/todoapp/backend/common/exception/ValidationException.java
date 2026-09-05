package com.todoapp.backend.common.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> fields;

    public ValidationException(String message, Map<String, String> fields) {
        super(message);
        this.fields = fields;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
