package com.todoapp.backend.column;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkflowColumnUpdateDeleteTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WorkflowColumnRepository repository;

    @Test
    void renamesColumn() throws Exception {
        WorkflowColumn column = repository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(patch("/api/columns/{id}", column.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "To Do"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("To Do"));
    }

    @Test
    void reordersColumn() throws Exception {
        WorkflowColumn a = repository.save(new WorkflowColumn("A", 0));
        repository.save(new WorkflowColumn("B", 1));
        repository.save(new WorkflowColumn("C", 2));
        repository.save(new WorkflowColumn("D", 3));

        mockMvc.perform(patch("/api/columns/{id}", a.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("position", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(3));
    }

    @Test
    void returns404WhenUpdatingMissingColumn() throws Exception {
        mockMvc.perform(patch("/api/columns/{id}", 9999L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("name", "X"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesEmptyColumn() throws Exception {
        WorkflowColumn column = repository.save(new WorkflowColumn("Backlog", 0));

        mockMvc.perform(delete("/api/columns/{id}", column.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void returns404WhenDeletingMissingColumn() throws Exception {
        mockMvc.perform(delete("/api/columns/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void reorderClampsNegativePositionToZero() throws Exception {
        WorkflowColumn a = repository.save(new WorkflowColumn("A", 0));
        repository.save(new WorkflowColumn("B", 1));
        repository.save(new WorkflowColumn("C", 2));

        mockMvc.perform(patch("/api/columns/{id}", a.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("position", -5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(0));
    }

    @Test
    void reorderClampsPositionBeyondSizeToMax() throws Exception {
        WorkflowColumn a = repository.save(new WorkflowColumn("A", 0));
        repository.save(new WorkflowColumn("B", 1));
        repository.save(new WorkflowColumn("C", 2));

        mockMvc.perform(patch("/api/columns/{id}", a.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("position", 999))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(2));
    }
}
