package com.todoapp.backend.board.dto;

import java.util.List;

public class BoardResponse {
    private List<ColumnWithTasksResponse> columns;

    public BoardResponse(List<ColumnWithTasksResponse> columns) {
        this.columns = columns;
    }

    public List<ColumnWithTasksResponse> getColumns() { return columns; }
}
