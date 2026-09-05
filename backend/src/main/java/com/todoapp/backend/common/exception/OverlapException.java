package com.todoapp.backend.common.exception;

import com.todoapp.backend.task.Task;

public class OverlapException extends RuntimeException {
    private final Task conflictingTask;

    public OverlapException(Task conflictingTask) {
        super("Schedule overlaps with task " + conflictingTask.getId());
        this.conflictingTask = conflictingTask;
    }

    public Task getConflictingTask() {
        return conflictingTask;
    }
}
