package com.todoapp.backend.task;

import com.todoapp.backend.task.dto.CreateTaskRequest;
import com.todoapp.backend.task.dto.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                                @jakarta.validation.Valid @RequestBody com.todoapp.backend.task.dto.UpdateTaskRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/move")
    public TaskResponse move(@PathVariable Long id,
                              @jakarta.validation.Valid @RequestBody com.todoapp.backend.task.dto.MoveTaskRequest request) {
        return service.move(id, request.getTargetColumnId(), request.getTargetPosition());
    }
}
