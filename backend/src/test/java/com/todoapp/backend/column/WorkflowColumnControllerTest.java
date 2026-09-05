package com.todoapp.backend.column;

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
class WorkflowColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndListsColumnsInOrder() throws Exception {
        mockMvc.perform(post("/api/columns")
                        .contentType("application/json")
                        .content("{\"name\":\"Backlog\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Backlog"))
                .andExpect(jsonPath("$.position").value(0));

        mockMvc.perform(post("/api/columns")
                        .contentType("application/json")
                        .content("{\"name\":\"Done\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.position").value(1));

        mockMvc.perform(get("/api/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Backlog"))
                .andExpect(jsonPath("$[1].name").value("Done"));
    }

    @Test
    void rejectsBlankColumnName() throws Exception {
        mockMvc.perform(post("/api/columns")
                        .contentType("application/json")
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
