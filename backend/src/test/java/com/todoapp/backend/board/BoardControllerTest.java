package com.todoapp.backend.board;

import com.todoapp.backend.column.WorkflowColumn;
import com.todoapp.backend.column.WorkflowColumnRepository;
import com.todoapp.backend.task.Task;
import com.todoapp.backend.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;
    @Autowired private TaskRepository taskRepository;

    @Test
    void returnsColumnsWithNestedTasksInOrder() throws Exception {
        WorkflowColumn backlog = columnRepository.save(new WorkflowColumn("Backlog", 0));
        WorkflowColumn done = columnRepository.save(new WorkflowColumn("Done", 1));
        taskRepository.save(new Task(backlog, 0, "First"));
        taskRepository.save(new Task(backlog, 1, "Second"));
        taskRepository.save(new Task(done, 0, "Shipped"));

        mockMvc.perform(get("/api/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].name").value("Backlog"))
                .andExpect(jsonPath("$.columns[0].tasks[0].title").value("First"))
                .andExpect(jsonPath("$.columns[0].tasks[1].title").value("Second"))
                .andExpect(jsonPath("$.columns[1].name").value("Done"))
                .andExpect(jsonPath("$.columns[1].tasks[0].title").value("Shipped"));
    }
}
