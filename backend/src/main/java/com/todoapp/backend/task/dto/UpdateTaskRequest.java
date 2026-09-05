package com.todoapp.backend.task.dto;

import com.todoapp.backend.task.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

public class UpdateTaskRequest {
    @NotBlank(message = "title must not be blank")
    private String title;

    private String description;

    @NotNull(message = "priority is required")
    private Priority priority;

    @NotNull(message = "tags is required")
    private Set<String> tags;
    private Instant startTime;
    private Instant endTime;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
}
