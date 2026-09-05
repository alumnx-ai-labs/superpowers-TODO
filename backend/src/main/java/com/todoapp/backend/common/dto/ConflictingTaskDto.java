package com.todoapp.backend.common.dto;

import com.todoapp.backend.task.Task;
import java.time.Instant;

public class ConflictingTaskDto {
    private Long id;
    private String title;
    private Instant startTime;
    private Instant endTime;

    public ConflictingTaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.startTime = task.getStartTime();
        this.endTime = task.getEndTime();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
}
