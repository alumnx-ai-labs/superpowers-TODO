package com.todoapp.backend.column;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkflowColumnRepositoryTest {

    @Autowired
    private WorkflowColumnRepository repository;

    @Test
    void savesAndFindsColumnsOrderedByPosition() {
        repository.save(new WorkflowColumn("Done", 1));
        repository.save(new WorkflowColumn("Backlog", 0));

        List<WorkflowColumn> columns = repository.findAllByOrderByPositionAsc();

        assertThat(columns).hasSize(2);
        assertThat(columns.get(0).getName()).isEqualTo("Backlog");
        assertThat(columns.get(1).getName()).isEqualTo("Done");
    }
}
