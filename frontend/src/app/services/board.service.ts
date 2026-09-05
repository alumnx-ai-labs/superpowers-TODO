import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Board, WorkflowColumn, Task, CreateTaskRequest, UpdateTaskRequest, MoveTaskRequest,
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class BoardService {
  private readonly baseUrl = '/api';

  constructor(private http: HttpClient) {}

  getBoard(): Observable<Board> {
    return this.http.get<Board>(`${this.baseUrl}/board`);
  }

  createColumn(name: string): Observable<WorkflowColumn> {
    return this.http.post<WorkflowColumn>(`${this.baseUrl}/columns`, { name });
  }

  updateColumn(id: number, changes: { name?: string; position?: number }): Observable<WorkflowColumn> {
    return this.http.patch<WorkflowColumn>(`${this.baseUrl}/columns/${id}`, changes);
  }

  deleteColumn(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/columns/${id}`);
  }

  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.baseUrl}/tasks`, request);
  }

  updateTask(id: number, request: UpdateTaskRequest): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/tasks/${id}`, request);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/tasks/${id}`);
  }

  moveTask(id: number, request: MoveTaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.baseUrl}/tasks/${id}/move`, request);
  }
}
