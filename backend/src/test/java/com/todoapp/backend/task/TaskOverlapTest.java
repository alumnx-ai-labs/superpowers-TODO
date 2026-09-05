package com.todoapp.backend.task;

import com.todoapp.backend.column.WorkflowColumn;
import com.todoapp.backend.column.WorkflowColumnRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskOverlapTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;

    private String taskJson(Long columnId, String title, String start, String end) {
        return "{\"columnId\":" + columnId + ",\"title\":\"" + title + "\","
                + "\"startTime\":\"" + start + "\",\"endTime\":\"" + end + "\"}";
    }

    @Test
    void rejectsPartialOverlap() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Standup", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Overlapping", "2026-09-05T09:30:00Z", "2026-09-05T11:00:00Z")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SCHEDULE_OVERLAP"))
                .andExpect(jsonPath("$.conflictingTask.title").value("Standup"));
    }

    @Test
    void rejectsFullContainment() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Long", "2026-09-05T09:00:00Z", "2026-09-05T12:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Inside", "2026-09-05T10:00:00Z", "2026-09-05T11:00:00Z")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsIdenticalRange() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "First", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Second", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isConflict());
    }

    @Test
    void allowsBackToBackTouchingBoundary() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "First", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "BackToBack", "2026-09-05T10:00:00Z", "2026-09-05T11:00:00Z")))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsReverseContainment() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Small", "2026-09-05T10:00:00Z", "2026-09-05T11:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Larger", "2026-09-05T09:00:00Z", "2026-09-05T12:00:00Z")))
                .andExpect(status().isConflict());
    }

    @Test
    void allowsNonOverlappingSeparateTimes() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Morning", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Afternoon", "2026-09-05T14:00:00Z", "2026-09-05T15:00:00Z")))
                .andExpect(status().isCreated());
    }

    @Test
    void allowsNewTaskEndingExactlyWhenExistingStarts() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Second", "2026-09-05T10:00:00Z", "2026-09-05T11:00:00Z")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "First", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsEndTimeEqualToStartTime() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "ZeroLength", "2026-09-05T09:00:00Z", "2026-09-05T09:00:00Z")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEndTimeBeforeStartTime() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Backwards", "2026-09-05T10:00:00Z", "2026-09-05T09:00:00Z")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ignoresTasksWithoutASchedule() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content("{\"columnId\":" + column.getId() + ",\"title\":\"Unscheduled\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks").contentType("application/json")
                        .content(taskJson(column.getId(), "Scheduled", "2026-09-05T09:00:00Z", "2026-09-05T10:00:00Z")))
                .andExpect(status().isCreated());
    }
}
