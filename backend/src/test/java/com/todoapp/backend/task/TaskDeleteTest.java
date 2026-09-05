package com.todoapp.backend.task;

import com.todoapp.backend.column.WorkflowColumn;
import com.todoapp.backend.column.WorkflowColumnRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskDeleteTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;
    @Autowired private TaskRepository taskRepository;

    @Test
    void deletesTaskAndRenumbersSiblings() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task first = taskRepository.save(new Task(column, 0, "First"));
        Task second = taskRepository.save(new Task(column, 1, "Second"));
        Task third = taskRepository.save(new Task(column, 2, "Third"));

        mockMvc.perform(delete("/api/tasks/{id}", second.getId()))
                .andExpect(status().isNoContent());

        var remaining = taskRepository.findByColumnIdOrderByPositionAsc(column.getId());
        assertThat(remaining).extracting(Task::getTitle).containsExactly("First", "Third");
        assertThat(remaining).extracting(Task::getPosition).containsExactly(0, 1);
    }

    @Test
    void returns404ForMissingTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}
