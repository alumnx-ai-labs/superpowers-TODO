import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Task } from '../models/models';

@Component({
  selector: 'app-task-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="task-card">
      <div class="schedule" *ngIf="task.startTime && task.endTime">
        {{ task.startTime | date:'shortTime' }} – {{ task.endTime | date:'shortTime' }}
      </div>
      <div class="task-title">
        {{ task.title }}
        <button type="button" class="edit-btn" (click)="edit.emit(task)">Edit</button>
      </div>
      <span class="priority-badge" [class]="'priority-' + task.priority.toLowerCase()">{{ task.priority }}</span>
      <div class="tags">
        <span class="tag" *ngFor="let tag of task.tags">{{ tag }}</span>
      </div>
    </div>
  `,
  styles: [`
    .task-card {
      padding: 12px;
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: 8px;
      margin-bottom: 8px;
      cursor: grab;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
    }
    .schedule { color: var(--color-accent); font-size: 0.75em; font-weight: 600; margin-bottom: 4px; }
    .task-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: var(--color-text);
    }
    .edit-btn {
      font-size: 0.75em;
      cursor: pointer;
      background: none;
      border: 1px solid var(--color-border);
      border-radius: 4px;
      padding: 2px 6px;
      color: var(--color-text-muted);
    }
    .priority-badge {
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.75em;
      display: inline-block;
      margin-top: 6px;
    }
    .priority-low { background: var(--priority-low-bg); color: var(--priority-low-text); }
    .priority-medium { background: var(--priority-medium-bg); color: var(--priority-medium-text); }
    .priority-high { background: var(--priority-high-bg); color: var(--priority-high-text); }
    .priority-urgent { background: var(--priority-urgent-bg); color: var(--priority-urgent-text); }
    .tags { margin-top: 6px; display: flex; gap: 4px; flex-wrap: wrap; }
    .tag {
      background: var(--color-surface-alt);
      border: 1px solid var(--color-border);
      border-radius: 10px;
      padding: 1px 8px;
      font-size: 0.7em;
      color: var(--color-text-muted);
    }
  `],
})
export class TaskCardComponent {
  @Input({ required: true }) task!: Task;
  @Output() edit = new EventEmitter<Task>();
}
