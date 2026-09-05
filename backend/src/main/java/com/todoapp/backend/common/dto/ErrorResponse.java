package com.todoapp.backend.common.dto;

import java.util.Map;

public class ErrorResponse {
    private String error;
    private Map<String, String> fields;

    public ErrorResponse(String error, Map<String, String> fields) {
        this.error = error;
        this.fields = fields;
    }

    public String getError() { return error; }
    public Map<String, String> getFields() { return fields; }
}
