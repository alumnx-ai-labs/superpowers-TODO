package com.todoapp.backend.column.dto;

import com.todoapp.backend.column.WorkflowColumn;
import java.time.Instant;

public class ColumnResponse {
    private Long id;
    private String name;
    private Integer position;
    private Instant createdAt;

    public ColumnResponse(WorkflowColumn column) {
        this.id = column.getId();
        this.name = column.getName();
        this.position = column.getPosition();
        this.createdAt = column.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
}
