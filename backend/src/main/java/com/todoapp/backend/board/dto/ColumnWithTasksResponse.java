package com.todoapp.backend.board.dto;

import com.todoapp.backend.column.WorkflowColumn;
import com.todoapp.backend.task.dto.TaskResponse;

import java.time.Instant;
import java.util.List;

public class ColumnWithTasksResponse {
    private Long id;
    private String name;
    private Integer position;
    private Instant createdAt;
    private List<TaskResponse> tasks;

    public ColumnWithTasksResponse(WorkflowColumn column, List<TaskResponse> tasks) {
        this.id = column.getId();
        this.name = column.getName();
        this.position = column.getPosition();
        this.createdAt = column.getCreatedAt();
        this.tasks = tasks;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
    public List<TaskResponse> getTasks() { return tasks; }
}
