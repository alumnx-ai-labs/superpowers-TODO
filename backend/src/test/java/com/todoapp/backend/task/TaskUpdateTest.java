package com.todoapp.backend.task;

import com.todoapp.backend.column.WorkflowColumn;
import com.todoapp.backend.column.WorkflowColumnRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskUpdateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;
    @Autowired private TaskRepository taskRepository;

    @Test
    void updatesTitleAndDescription() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = taskRepository.save(new Task(column, 0, "Old title"));

        mockMvc.perform(patch("/api/tasks/{id}", task.getId())
                        .contentType("application/json")
                        .content("{\"title\":\"New title\",\"description\":\"Details\",\"priority\":\"HIGH\",\"tags\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void reschedulingToOwnUnchangedTimeDoesNotConflictWithItself() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = new Task(column, 0, "Standup");
        task.setStartTime(java.time.Instant.parse("2026-09-05T09:00:00Z"));
        task.setEndTime(java.time.Instant.parse("2026-09-05T10:00:00Z"));
        task = taskRepository.save(task);

        mockMvc.perform(patch("/api/tasks/{id}", task.getId())
                        .contentType("application/json")
                        .content("{\"title\":\"Standup\",\"priority\":\"MEDIUM\",\"tags\":[],\"startTime\":\"2026-09-05T09:00:00Z\",\"endTime\":\"2026-09-05T10:00:00Z\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void reschedulingIntoAnotherTasksSlotIsRejected() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task other = new Task(column, 0, "Standup");
        other.setStartTime(java.time.Instant.parse("2026-09-05T09:00:00Z"));
        other.setEndTime(java.time.Instant.parse("2026-09-05T10:00:00Z"));
        taskRepository.save(other);
        Task movable = taskRepository.save(new Task(column, 1, "Free task"));

        mockMvc.perform(patch("/api/tasks/{id}", movable.getId())
                        .contentType("application/json")
                        .content("{\"title\":\"Free task\",\"priority\":\"MEDIUM\",\"tags\":[],\"startTime\":\"2026-09-05T09:30:00Z\",\"endTime\":\"2026-09-05T10:30:00Z\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void returns404ForMissingTask() throws Exception {
        mockMvc.perform(patch("/api/tasks/{id}", 9999L)
                        .contentType("application/json")
                        .content("{\"title\":\"X\",\"priority\":\"MEDIUM\",\"tags\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUpdateMissingPriority() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = taskRepository.save(new Task(column, 0, "Title"));

        mockMvc.perform(patch("/api/tasks/{id}", task.getId())
                        .contentType("application/json")
                        .content("{\"title\":\"Title\",\"tags\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUpdateMissingTags() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = taskRepository.save(new Task(column, 0, "Title"));

        mockMvc.perform(patch("/api/tasks/{id}", task.getId())
                        .contentType("application/json")
                        .content("{\"title\":\"Title\",\"priority\":\"MEDIUM\"}"))
                .andExpect(status().isBadRequest());
    }
}
