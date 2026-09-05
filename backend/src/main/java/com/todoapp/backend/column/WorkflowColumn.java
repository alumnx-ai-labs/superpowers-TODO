package com.todoapp.backend.column;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "workflow_column")
public class WorkflowColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected WorkflowColumn() {
    }

    public WorkflowColumn(String name, Integer position) {
        this.name = name;
        this.position = position;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Instant getCreatedAt() { return createdAt; }
}
