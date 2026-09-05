package com.todoapp.backend.column;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowColumnRepository extends JpaRepository<WorkflowColumn, Long> {
    List<WorkflowColumn> findAllByOrderByPositionAsc();
}
