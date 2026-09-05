package com.todoapp.backend.task.dto;

import com.todoapp.backend.task.Priority;
import com.todoapp.backend.task.Task;

import java.time.Instant;
import java.util.Set;

public class TaskResponse {
    private Long id;
    private Long columnId;
    private Integer position;
    private String title;
    private String description;
    private Priority priority;
    private Set<String> tags;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
    private Instant updatedAt;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.columnId = task.getColumn().getId();
        this.position = task.getPosition();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.tags = task.getTags();
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getColumnId() { return columnId; }
    public Integer getPosition() { return position; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public Set<String> getTags() { return tags; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
