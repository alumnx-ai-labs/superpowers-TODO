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
class TaskCreateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;

    @Test
    void createsTaskWithDefaultsAndAppendsPosition() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"columnId\":" + column.getId() + ",\"title\":\"Write spec\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write spec"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.position").value(0))
                .andExpect(jsonPath("$.tags").isEmpty());
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"columnId\":" + column.getId() + ",\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404ForMissingColumn() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"columnId\":9999,\"title\":\"Orphan\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsStartTimeWithoutEndTime() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"columnId\":" + column.getId()
                                + ",\"title\":\"Half scheduled\",\"startTime\":\"2026-09-05T09:00:00Z\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingColumnId() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"title\":\"Orphan\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateTagsAreDeduplicated() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(post("/api/tasks")
                        .contentType("application/json")
                        .content("{\"columnId\":" + column.getId()
                                + ",\"title\":\"Tagged\",\"tags\":[\"urgent\",\"urgent\",\"home\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.tags", org.hamcrest.Matchers.containsInAnyOrder("urgent", "home")));
    }
}
