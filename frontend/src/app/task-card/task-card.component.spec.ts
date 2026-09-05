import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskCardComponent } from './task-card.component';
import { Task } from '../models/models';

describe('TaskCardComponent', () => {
  let fixture: ComponentFixture<TaskCardComponent>;
  const task: Task = {
    id: 1, columnId: 1, position: 0, title: 'Write spec', description: null,
    priority: 'HIGH', tags: ['docs'], startTime: null, endTime: null,
    createdAt: '', updatedAt: '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TaskCardComponent] }).compileComponents();
    fixture = TestBed.createComponent(TaskCardComponent);
    fixture.componentInstance.task = task;
    fixture.detectChanges();
  });

  it('emits edit with the task when the Edit button is clicked', () => {
    const emitted: Task[] = [];
    fixture.componentInstance.edit.subscribe((t: Task) => emitted.push(t));

    const editButton: HTMLButtonElement = fixture.nativeElement.querySelector('.edit-btn');
    expect(editButton).not.toBeNull();
    editButton.click();

    expect(emitted).toEqual([task]);
  });

  it('does not emit edit when clicking the card body outside the Edit button (regression: drag/click conflict)', () => {
    const emitted: Task[] = [];
    fixture.componentInstance.edit.subscribe((t: Task) => emitted.push(t));

    const cardBody: HTMLElement = fixture.nativeElement.querySelector('.task-card');
    cardBody.click();

    expect(emitted).toEqual([]);
  });

  it('renders the priority badge with a class matching the task priority', () => {
    const badge: HTMLElement = fixture.nativeElement.querySelector('.priority-badge');
    expect(badge.classList).toContain('priority-high');
    expect(badge.textContent).toContain('HIGH');
  });

  it('does not render a schedule line when the task has no start/end time', () => {
    expect(fixture.nativeElement.querySelector('.schedule')).toBeNull();
  });

  it('renders a schedule line when both start and end time are set', () => {
    fixture.componentInstance.task = { ...task, startTime: '2026-09-05T09:00:00Z', endTime: '2026-09-05T10:00:00Z' };
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.schedule')).not.toBeNull();
  });
});
