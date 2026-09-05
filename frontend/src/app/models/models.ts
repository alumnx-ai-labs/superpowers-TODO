export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface WorkflowColumn {
  id: number;
  name: string;
  position: number;
  createdAt: string;
}

export interface Task {
  id: number;
  columnId: number;
  position: number;
  title: string;
  description: string | null;
  priority: Priority;
  tags: string[];
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ColumnWithTasks extends WorkflowColumn {
  tasks: Task[];
}

export interface Board {
  columns: ColumnWithTasks[];
}

export interface CreateTaskRequest {
  columnId: number;
  title: string;
  description?: string;
  priority?: Priority;
  tags?: string[];
  startTime?: string;
  endTime?: string;
}

export interface UpdateTaskRequest {
  title: string;
  description: string | null;
  priority: Priority;
  tags: string[];
  startTime: string | null;
  endTime: string | null;
}

export interface MoveTaskRequest {
  targetColumnId: number;
  targetPosition: number;
}

export interface ApiErrorBody {
  error: string;
  fields?: Record<string, string>;
}

export interface OverlapErrorBody {
  error: 'SCHEDULE_OVERLAP';
  conflictingTask: { id: number; title: string; startTime: string; endTime: string };
}
