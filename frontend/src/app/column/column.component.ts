import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { ColumnWithTasks, Task } from '../models/models';
import { TaskCardComponent } from '../task-card/task-card.component';

@Component({
  selector: 'app-column',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, TaskCardComponent],
  template: `
    <div class="column">
      <div class="column-header">
        <span class="column-dot" [style.background]="accentColor"></span>
        <div *ngIf="!renaming" class="column-title" (dblclick)="startRenaming()">
          <h3>{{ column.name }}</h3>
        </div>
        <input *ngIf="renaming" class="column-rename-input" [(ngModel)]="renameDraft"
               (blur)="confirmRename()" (keyup.enter)="confirmRename()" />
        <span class="column-count">{{ column.tasks.length }}</span>
      </div>
      <div class="column-actions">
        <button type="button" class="text-button" (click)="requestDelete()">Delete column</button>
        <button type="button" class="text-button" (click)="addTask.emit(column.id)">+ Add task</button>
      </div>
      <div cdkDropList [id]="'column-' + column.id" [cdkDropListData]="column.tasks"
           (cdkDropListDropped)="drop.emit($event)" class="task-list">
        <app-task-card *ngFor="let task of column.tasks" cdkDrag [cdkDragData]="task" [task]="task"
                       (edit)="editTask.emit($event)"></app-task-card>
      </div>
    </div>
  `,
  styles: [`
    .column {
      min-width: 260px;
      max-width: 260px;
      background: var(--color-surface-alt);
      border: 1px solid var(--color-border);
      padding: 12px;
      border-radius: 10px;
    }
    .column-header { display: flex; align-items: center; gap: 8px; }
    .column-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .column-title { flex: 1; }
    .column-title h3 { margin: 0; font-size: 1em; color: var(--color-text); }
    .column-count { color: var(--color-text-muted); font-size: 0.85em; }
    .column-rename-input { flex: 1; }
    .column-actions { display: flex; gap: 8px; margin: 8px 0; }
    .text-button {
      background: none;
      border: none;
      color: var(--color-accent);
      font-size: 0.8em;
      cursor: pointer;
      padding: 0;
    }
    .task-list { min-height: 40px; }
  `],
})
export class ColumnComponent {
  @Input({ required: true }) column!: ColumnWithTasks;
  @Input() accentColor = 'var(--column-accent-1)';
  @Output() rename = new EventEmitter<{ id: number; name: string }>();
  @Output() delete = new EventEmitter<number>();
  @Output() drop = new EventEmitter<CdkDragDrop<Task[]>>();
  @Output() editTask = new EventEmitter<Task>();
  @Output() addTask = new EventEmitter<number>();

  renaming = false;
  renameDraft = '';

  startRenaming(): void {
    this.renaming = true;
    this.renameDraft = this.column.name;
  }

  confirmRename(): void {
    if (this.renaming) {
      this.renaming = false;
      this.rename.emit({ id: this.column.id, name: this.renameDraft });
    }
  }

  requestDelete(): void {
    this.delete.emit(this.column.id);
  }
}
