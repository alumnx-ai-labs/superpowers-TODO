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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskMoveTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WorkflowColumnRepository columnRepository;
    @Autowired private TaskRepository taskRepository;

    @Test
    void reordersWithinSameColumn() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task first = taskRepository.save(new Task(column, 0, "First"));
        Task second = taskRepository.save(new Task(column, 1, "Second"));
        Task third = taskRepository.save(new Task(column, 2, "Third"));

        mockMvc.perform(post("/api/tasks/{id}/move", first.getId())
                        .contentType("application/json")
                        .content("{\"targetColumnId\":" + column.getId() + ",\"targetPosition\":2}"))
                .andExpect(status().isOk());

        var ordered = taskRepository.findByColumnIdOrderByPositionAsc(column.getId());
        assertThat(ordered).extracting(Task::getTitle).containsExactly("Second", "Third", "First");
        assertThat(ordered).extracting(Task::getPosition).containsExactly(0, 1, 2);
    }

    @Test
    void movesAcrossColumnsAndRenumbersBoth() throws Exception {
        WorkflowColumn source = columnRepository.save(new WorkflowColumn("Backlog", 0));
        WorkflowColumn target = columnRepository.save(new WorkflowColumn("Done", 1));
        Task a = taskRepository.save(new Task(source, 0, "A"));
        Task b = taskRepository.save(new Task(source, 1, "B"));
        Task existingInTarget = taskRepository.save(new Task(target, 0, "Existing"));

        mockMvc.perform(post("/api/tasks/{id}/move", a.getId())
                        .contentType("application/json")
                        .content("{\"targetColumnId\":" + target.getId() + ",\"targetPosition\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columnId").value(target.getId()))
                .andExpect(jsonPath("$.position").value(0));

        var sourceOrdered = taskRepository.findByColumnIdOrderByPositionAsc(source.getId());
        assertThat(sourceOrdered).extracting(Task::getTitle).containsExactly("B");
        assertThat(sourceOrdered).extracting(Task::getPosition).containsExactly(0);

        var targetOrdered = taskRepository.findByColumnIdOrderByPositionAsc(target.getId());
        assertThat(targetOrdered).extracting(Task::getTitle).containsExactly("A", "Existing");
        assertThat(targetOrdered).extracting(Task::getPosition).containsExactly(0, 1);
    }

    @Test
    void returns404ForMissingTargetColumn() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = taskRepository.save(new Task(column, 0, "First"));

        mockMvc.perform(post("/api/tasks/{id}/move", task.getId())
                        .contentType("application/json")
                        .content("{\"targetColumnId\":9999,\"targetPosition\":0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clampsNegativeTargetPositionToZero() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task first = taskRepository.save(new Task(column, 0, "First"));
        Task second = taskRepository.save(new Task(column, 1, "Second"));

        mockMvc.perform(post("/api/tasks/{id}/move", second.getId())
                        .contentType("application/json")
                        .content("{\"targetColumnId\":" + column.getId() + ",\"targetPosition\":-5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(0));

        var ordered = taskRepository.findByColumnIdOrderByPositionAsc(column.getId());
        assertThat(ordered).extracting(Task::getTitle).containsExactly("Second", "First");
    }

    @Test
    void rejectsMoveMissingTargetColumnId() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task task = taskRepository.save(new Task(column, 0, "First"));

        mockMvc.perform(post("/api/tasks/{id}/move", task.getId())
                        .contentType("application/json")
                        .content("{\"targetPosition\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clampsTargetPositionBeyondSizeToMax() throws Exception {
        WorkflowColumn column = columnRepository.save(new WorkflowColumn("Backlog", 0));
        Task first = taskRepository.save(new Task(column, 0, "First"));
        Task second = taskRepository.save(new Task(column, 1, "Second"));

        mockMvc.perform(post("/api/tasks/{id}/move", first.getId())
                        .contentType("application/json")
                        .content("{\"targetColumnId\":" + column.getId() + ",\"targetPosition\":999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(1));

        var ordered = taskRepository.findByColumnIdOrderByPositionAsc(column.getId());
        assertThat(ordered).extracting(Task::getTitle).containsExactly("Second", "First");
    }
}
