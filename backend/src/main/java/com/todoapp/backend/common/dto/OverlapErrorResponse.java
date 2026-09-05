package com.todoapp.backend.common.dto;

public class OverlapErrorResponse {
    private final String error = "SCHEDULE_OVERLAP";
    private ConflictingTaskDto conflictingTask;

    public OverlapErrorResponse(ConflictingTaskDto conflictingTask) {
        this.conflictingTask = conflictingTask;
    }

    public String getError() { return error; }
    public ConflictingTaskDto getConflictingTask() { return conflictingTask; }
}
