package com.todoapp.backend.task.dto;

import jakarta.validation.constraints.NotNull;

public class MoveTaskRequest {
    @NotNull(message = "targetColumnId is required")
    private Long targetColumnId;

    @NotNull(message = "targetPosition is required")
    private Integer targetPosition;

    public Long getTargetColumnId() { return targetColumnId; }
    public void setTargetColumnId(Long targetColumnId) { this.targetColumnId = targetColumnId; }
    public Integer getTargetPosition() { return targetPosition; }
    public void setTargetPosition(Integer targetPosition) { this.targetPosition = targetPosition; }
}
