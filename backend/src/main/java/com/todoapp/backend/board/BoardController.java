package com.todoapp.backend.board;

import com.todoapp.backend.board.dto.BoardResponse;
import com.todoapp.backend.board.dto.ColumnWithTasksResponse;
import com.todoapp.backend.column.WorkflowColumnRepository;
import com.todoapp.backend.task.TaskRepository;
import com.todoapp.backend.task.dto.TaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final WorkflowColumnRepository columnRepository;
    private final TaskRepository taskRepository;

    public BoardController(WorkflowColumnRepository columnRepository, TaskRepository taskRepository) {
        this.columnRepository = columnRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public BoardResponse getBoard() {
        var columns = columnRepository.findAllByOrderByPositionAsc().stream()
                .map(column -> new ColumnWithTasksResponse(
                        column,
                        taskRepository.findByColumnIdOrderByPositionAsc(column.getId()).stream()
                                .map(TaskResponse::new)
                                .toList()))
                .toList();
        return new BoardResponse(columns);
    }
}
