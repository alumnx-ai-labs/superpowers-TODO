import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { TaskFormComponent, findClientSideOverlap } from './task-form.component';
import { Task } from '../models/models';

describe('findClientSideOverlap', () => {
  const scheduled: Task = {
    id: 1, columnId: 1, position: 0, title: 'Standup', description: null,
    priority: 'MEDIUM', tags: [], startTime: '2026-09-05T09:00:00Z', endTime: '2026-09-05T10:00:00Z',
    createdAt: '', updatedAt: '',
  };

  it('detects a partial overlap', () => {
    const result = findClientSideOverlap('2026-09-05T09:30:00Z', '2026-09-05T11:00:00Z', null, [scheduled]);
    expect(result?.id).toBe(1);
  });

  it('allows back-to-back schedules', () => {
    const result = findClientSideOverlap('2026-09-05T10:00:00Z', '2026-09-05T11:00:00Z', null, [scheduled]);
    expect(result).toBeNull();
  });

  it('excludes the task being edited from its own overlap check', () => {
    const result = findClientSideOverlap('2026-09-05T09:00:00Z', '2026-09-05T10:00:00Z', 1, [scheduled]);
    expect(result).toBeNull();
  });
});

describe('TaskFormComponent', () => {
  let fixture: ComponentFixture<TaskFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TaskFormComponent] }).compileComponents();
    fixture = TestBed.createComponent(TaskFormComponent);
    fixture.componentInstance.columnId = 1;
    fixture.componentInstance.editingTask = null;
    fixture.componentInstance.existingTasks = [];
    fixture.detectChanges();
  });

  it('rejects submission with a blank title', () => {
    fixture.componentInstance.title = '';
    expect(fixture.componentInstance.validate()).toContain('Title is required');
  });

  it('rejects an end time that is not after the start time', () => {
    fixture.componentInstance.title = 'Task';
    fixture.componentInstance.startTime = '2026-09-05T10:00:00';
    fixture.componentInstance.endTime = '2026-09-05T09:00:00';
    expect(fixture.componentInstance.validate()).toContain('End time must be after start time');
  });

  it('emits save with a well-formed CreateTaskRequest when valid', () => {
    const emitted: any[] = [];
    fixture.componentInstance.save.subscribe((e: any) => emitted.push(e));
    fixture.componentInstance.title = 'New task';
    fixture.componentInstance.submit();
    expect(emitted[0].request.title).toBe('New task');
    expect(emitted[0].request.columnId).toBe(1);
  });

  it('rejects a whitespace-only title', () => {
    fixture.componentInstance.title = '   ';
    expect(fixture.componentInstance.validate()).toContain('Title is required');
  });

  it('rejects a start time set without an end time', () => {
    fixture.componentInstance.title = 'Task';
    fixture.componentInstance.startTime = '2026-09-05T09:00:00';
    fixture.componentInstance.endTime = '';
    expect(fixture.componentInstance.validate()).toContain('Start time and end time must both be set or both be empty');
  });

  it('rejects an end time equal to the start time', () => {
    fixture.componentInstance.title = 'Task';
    fixture.componentInstance.startTime = '2026-09-05T09:00:00';
    fixture.componentInstance.endTime = '2026-09-05T09:00:00';
    expect(fixture.componentInstance.validate()).toContain('End time must be after start time');
  });

  it('splits comma-separated tags and drops blank entries', () => {
    const emitted: any[] = [];
    fixture.componentInstance.save.subscribe((e: any) => emitted.push(e));
    fixture.componentInstance.title = 'Task';
    fixture.componentInstance.tagsText = 'urgent, , home ,  ';
    fixture.componentInstance.submit();
    expect(emitted[0].request.tags).toEqual(['urgent', 'home']);
  });

  it('blocks submission and sets conflict when the schedule overlaps an existing task', () => {
    // Existing task's UTC window is derived from the candidate's own local time,
    // so this test is correct regardless of which timezone it runs in.
    const candidateStart = '2026-09-05T09:30:00';
    const candidateEnd = '2026-09-05T10:30:00';
    const existingStartUtc = new Date('2026-09-05T09:00:00').toISOString();
    const existingEndUtc = new Date('2026-09-05T10:00:00').toISOString();

    const emitted: any[] = [];
    fixture.componentInstance.save.subscribe((e: any) => emitted.push(e));
    fixture.componentInstance.existingTasks = [{
      id: 5, columnId: 1, position: 0, title: 'Standup', description: null,
      priority: 'MEDIUM', tags: [], startTime: existingStartUtc, endTime: existingEndUtc,
      createdAt: '', updatedAt: '',
    }];
    fixture.componentInstance.title = 'Conflicting';
    fixture.componentInstance.startTime = candidateStart;
    fixture.componentInstance.endTime = candidateEnd;

    fixture.componentInstance.submit();

    expect(emitted.length).toBe(0);
    expect(fixture.componentInstance.conflict?.id).toBe(5);
  });

  it('excludes the task being edited from its own overlap check when resubmitting its unchanged schedule', () => {
    const scheduleStart = '2026-09-05T09:30:00';
    const scheduleEnd = '2026-09-05T10:30:00';
    const startUtc = new Date(scheduleStart).toISOString();
    const endUtc = new Date(scheduleEnd).toISOString();
    const editing: Task = {
      id: 5, columnId: 1, position: 0, title: 'Standup', description: null,
      priority: 'MEDIUM', tags: [], startTime: startUtc, endTime: endUtc,
      createdAt: '', updatedAt: '',
    };

    const emitted: any[] = [];
    fixture.componentInstance.save.subscribe((e: any) => emitted.push(e));
    fixture.componentInstance.existingTasks = [editing];
    fixture.componentInstance.editingTask = editing;
    fixture.componentInstance.ngOnChanges({ editingTask: new SimpleChange(null, editing, false) });
    fixture.componentInstance.title = 'Standup';

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.conflict).toBeNull();
    expect(emitted.length).toBe(1);
    expect(emitted[0].taskId).toBe(5);
  });

  it('does not emit deleteTask when there is no editingTask', () => {
    const emitted: number[] = [];
    fixture.componentInstance.deleteTask.subscribe((id: number) => emitted.push(id));
    fixture.componentInstance.requestDelete();
    expect(emitted).toEqual([]);
  });

  it('emits deleteTask with the editing task\'s id', () => {
    const emitted: number[] = [];
    fixture.componentInstance.deleteTask.subscribe((id: number) => emitted.push(id));
    fixture.componentInstance.editingTask = {
      id: 7, columnId: 1, position: 0, title: 'Existing', description: null,
      priority: 'MEDIUM', tags: [], startTime: null, endTime: null, createdAt: '', updatedAt: '',
    };
    fixture.componentInstance.requestDelete();
    expect(emitted).toEqual([7]);
  });

  describe('ngOnChanges', () => {
    it('populates all fields when editingTask is set', () => {
      const task: Task = {
        id: 9, columnId: 1, position: 0, title: 'Edit me', description: 'Notes',
        priority: 'HIGH', tags: ['a', 'b'], startTime: '2026-09-05T09:00:00Z', endTime: '2026-09-05T10:00:00Z',
        createdAt: '', updatedAt: '',
      };
      fixture.componentInstance.editingTask = task;
      fixture.componentInstance.ngOnChanges({ editingTask: new SimpleChange(null, task, false) });

      expect(fixture.componentInstance.title).toBe('Edit me');
      expect(fixture.componentInstance.description).toBe('Notes');
      expect(fixture.componentInstance.priority).toBe('HIGH');
      expect(fixture.componentInstance.tagsText).toBe('a, b');
      expect(fixture.componentInstance.startTime).not.toBe('');
      expect(fixture.componentInstance.endTime).not.toBe('');
    });

    it('resets all fields to blank when editingTask changes back to null', () => {
      fixture.componentInstance.title = 'Stale title';
      fixture.componentInstance.editingTask = null;
      fixture.componentInstance.ngOnChanges({ editingTask: new SimpleChange({}, null, false) });

      expect(fixture.componentInstance.title).toBe('');
      expect(fixture.componentInstance.priority).toBe('MEDIUM');
    });

    it('does not re-seed fields when an unrelated input changes (regression: form-reset-on-keystroke bug)', () => {
      fixture.componentInstance.title = 'Typed by user';
      fixture.componentInstance.ngOnChanges({ existingTasks: new SimpleChange([], [], false) });

      expect(fixture.componentInstance.title).toBe('Typed by user');
    });
  });
});
