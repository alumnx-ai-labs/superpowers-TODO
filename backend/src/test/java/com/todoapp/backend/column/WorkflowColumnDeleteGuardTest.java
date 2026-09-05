package com.todoapp.backend.column;

import com.todoapp.backend.task.Task;
import com.todoapp.backend.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkflowColumnDeleteGuardTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;
    @Autowired private TaskRepository taskRepository;

    @Test
    void blocksDeletionWhenColumnHasTasks() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        taskRepository.save(new Task(column, 0, "Something"));

        mockMvc.perform(delete("/api/columns/{id}", column.getId()))
                .andExpect(status().isConflict());
    }
}
