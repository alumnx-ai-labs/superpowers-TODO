# To Do App Restyle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle the existing To Do app's frontend to match a reference UI's layout (persistent sidebar + column board, colored column headers with counts, styled task cards), using our own color palette with light/dark theme support, while keeping drag-and-drop as the only column-move mechanism.

**Architecture:** Add a small set of CSS custom-property design tokens to the global stylesheet (light values at `:root`, dark overrides under `@media (prefers-color-scheme: dark)`), then restyle each existing standalone component (`BoardComponent`, `ColumnComponent`, `TaskCardComponent`, `TaskFormComponent`, `AppComponent`) to consume those tokens. No new business logic, no new backend calls, no new interaction model — this is a template/CSS pass over components that already work, plus one small pure helper function for column accent-color assignment.

**Tech Stack:** Angular 18 standalone components (inline `styles: []` arrays), `@angular/cdk/drag-drop`, plain CSS custom properties (no CSS framework).

**Spec:** `docs/superpowers/specs/2026-09-04-todo-app-design.md` (original app spec — this plan restyles it, it does not change the spec's functional requirements)

## Global Constraints

- Drag-and-drop remains the only way to move a task between columns — no new arrow/button move controls are added (per this restyle's design discussion, confirmed with the user).
- No "workspace" branding, avatar, or motivational headline — those don't fit a single-user local app (per spec Scope: single-user, no login) and were explicitly dropped from the reference UI's layout.
- Colors are our own palette, defined as CSS custom properties, never hardcoded hex values copied from the reference screenshot.
- Theme is light/dark based on `prefers-color-scheme`, not a manual toggle (no toggle UI was requested).
- Column accent colors are assigned deterministically by column position (index into a fixed palette) — they carry no semantic meaning (columns are user-named, not fixed states).
- **Pure-CSS/visual-only tasks are not TDD test-first cycles.** A task that only changes a component's `styles` array or static template markup (no new `@Input`/`@Output`/method) introduces no new assertable JS behavior — Jasmine/Karma can't meaningfully assert on rendered color values. Those tasks are verified by `ng build` succeeding and the existing test suite staying green, not by a new failing test. This mirrors accepted precedent in this codebase (the original implementation plan's CSS-only task had no dedicated test). Tasks that add a new `@Input`, computed value, or conditional template branch (e.g. the sidebar placeholder, the accent-color function) DO get a full TDD cycle — the exception is narrow.
- `@angular/cdk/drag-drop`'s preview/placeholder elements (`.cdk-drag-preview`, `.cdk-drag-placeholder`) are rendered by CDK outside each component's own template into a global overlay. Angular's view encapsulation only scopes a component's *own* `styles` array — it does not prevent a plain global selector in `frontend/src/styles.css` from matching them. Drag-state styling therefore belongs in the global stylesheet, not in `ColumnComponent`'s or `TaskCardComponent`'s own `styles` array.

---

## File Structure

```
frontend/src/
  styles.css                                  Design tokens (light/dark), global body style,
                                               global CDK drag-preview/placeholder styling
  app/
    app.component.ts                          Simple header ("To Do"), no branding
    app.component.html                        DELETE (dead — component uses inline template)
    app.component.css                         DELETE (dead — component uses inline styles)
    app.component.spec.ts                     + header test
    board/
      board.component.ts                      Sidebar + board two-pane layout, sidebar
                                               placeholder, columnAccentColor() helper
      board.component.spec.ts                 + placeholder tests, + columnAccentColor tests
    column/
      column.component.ts                     accentColor @Input, dot + count header, restyle
      column.component.spec.ts                + accentColor/count test
    task-card/
      task-card.component.ts                  Card restyle only (no new @Input/@Output)
    task-form/
      task-form.component.ts                  Sidebar-panel restyle only (no new @Input/@Output)
```

No backend files are touched by this plan.

---

## Task 1: Design tokens in the global stylesheet

**Files:**
- Modify: `frontend/src/styles.css`

**Interfaces:**
- Produces: CSS custom properties consumed by every later task — `--color-bg`, `--color-surface`, `--color-surface-alt`, `--color-text`, `--color-text-muted`, `--color-border`, `--color-accent`, `--color-danger`, `--priority-low-bg`, `--priority-low-text`, `--priority-medium-bg`, `--priority-medium-text`, `--priority-high-bg`, `--priority-high-text`, `--priority-urgent-bg`, `--priority-urgent-text`, `--column-accent-1` through `--column-accent-4`.

This is a pure-CSS task (see Global Constraints) — no failing test first, verified by `ng build`.

- [ ] **Step 1: Write the design tokens**

Replace the entire contents of `frontend/src/styles.css` with:

```css
:root {
  --color-bg: #f5f6f8;
  --color-surface: #ffffff;
  --color-surface-alt: #eef0f3;
  --color-text: #1a1a1a;
  --color-text-muted: #6b7280;
  --color-border: #e2e4e9;
  --color-accent: #4f6df5;
  --color-danger: #dc3545;

  --priority-low-bg: #d4edda;
  --priority-low-text: #155724;
  --priority-medium-bg: #fff3cd;
  --priority-medium-text: #856404;
  --priority-high-bg: #f8d7da;
  --priority-high-text: #721c24;
  --priority-urgent-bg: #dc3545;
  --priority-urgent-text: #ffffff;

  --column-accent-1: #4f6df5;
  --column-accent-2: #22c55e;
  --column-accent-3: #f59e0b;
  --column-accent-4: #ec4899;
}

@media (prefers-color-scheme: dark) {
  :root {
    --color-bg: #14151a;
    --color-surface: #1f2128;
    --color-surface-alt: #262933;
    --color-text: #e5e7eb;
    --color-text-muted: #9ca3af;
    --color-border: #33363f;
    --color-accent: #7c93ff;
    --color-danger: #ef4444;

    --priority-low-bg: #10321f;
    --priority-low-text: #86efac;
    --priority-medium-bg: #3a2f0a;
    --priority-medium-text: #fde68a;
    --priority-high-bg: #3a1519;
    --priority-high-text: #fca5a5;
    --priority-urgent-bg: #ef4444;
    --priority-urgent-text: #1a1a1a;

    --column-accent-1: #7c93ff;
    --column-accent-2: #4ade80;
    --column-accent-3: #fbbf24;
    --column-accent-4: #f472b6;
  }
}

body {
  background: var(--color-bg);
  color: var(--color-text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  margin: 0;
}
```

- [ ] **Step 2: Verify the build succeeds**

Run: `cd frontend && npx ng build`
Expected: `Application bundle generation complete.` with no errors.

- [ ] **Step 3: Run the full frontend suite to confirm no regressions**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless`
Expected: all existing tests still pass (this task changed no component logic).

- [ ] **Step 4: Commit**

```bash
git add frontend/src/styles.css
git commit -m "style: add light/dark design tokens to global stylesheet"
```

---

## Task 2: BoardComponent — sidebar layout, placeholder, and column accent colors

**Files:**
- Modify: `frontend/src/app/board/board.component.ts`
- Test: `frontend/src/app/board/board.component.spec.ts`

**Interfaces:**
- Consumes: design tokens from Task 1 (`var(--color-surface)`, `var(--color-border)`, `var(--color-text-muted)`, `var(--column-accent-1..4)`).
- Produces: exported `columnAccentColor(index: number): string` (consumed by Task 3's `ColumnComponent` via a new `accentColor` input, bound from `BoardComponent`'s template).

- [ ] **Step 1: Write the failing tests**

Append to `frontend/src/app/board/board.component.spec.ts`, inside the top `describe('BoardComponent', ...)` block (the one using `mockBoard`, which has one column with id `1`) — add these two `it` blocks right after the existing `it('renders the fetched column and task', ...)`:

```typescript
  it('shows a placeholder message in the sidebar when no task is being created or edited', () => {
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Select a column');
  });

  it('hides the sidebar placeholder once a task form is open', () => {
    fixture.componentInstance.onAddTaskClicked(1);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).not.toContain('Select a column');
  });
```

Then add a new top-level `describe` block at the end of the file (after the last existing `describe`), for the pure helper function:

```typescript
describe('columnAccentColor', () => {
  it('cycles through the accent palette by index', () => {
    expect(columnAccentColor(0)).toBe('var(--column-accent-1)');
    expect(columnAccentColor(1)).toBe('var(--column-accent-2)');
    expect(columnAccentColor(2)).toBe('var(--column-accent-3)');
    expect(columnAccentColor(3)).toBe('var(--column-accent-4)');
  });

  it('wraps back to the first color once the palette is exhausted', () => {
    expect(columnAccentColor(4)).toBe('var(--column-accent-1)');
    expect(columnAccentColor(5)).toBe('var(--column-accent-2)');
  });
});
```

Add `columnAccentColor` to the existing import from `./board.component` at the top of the file:

```typescript
import { BoardComponent, columnAccentColor } from './board.component';
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/board.component.spec.ts'`
Expected: FAIL — `columnAccentColor` is not exported yet (compile error), and the placeholder text doesn't exist yet.

- [ ] **Step 3: Add the helper function and restructure the template**

Modify `frontend/src/app/board/board.component.ts` — add this exported function above the `@Component` decorator:

```typescript
const COLUMN_ACCENT_PALETTE = [
  'var(--column-accent-1)',
  'var(--column-accent-2)',
  'var(--column-accent-3)',
  'var(--column-accent-4)',
];

export function columnAccentColor(index: number): string {
  return COLUMN_ACCENT_PALETTE[index % COLUMN_ACCENT_PALETTE.length];
}
```

Replace the component's `template` and `styles` with:

```typescript
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <app-task-form *ngIf="activeColumnIdForNewTask !== null || editingTask !== null"
                        [columnId]="(editingTask?.columnId ?? activeColumnIdForNewTask)!"
                        [editingTask]="editingTask"
                        [existingTasks]="allTasks()" [serverConflict]="lastConflict" (save)="onTaskSaved($event)"
                        (cancel)="onCancelForm()" (deleteTask)="onDeleteTask($event)"></app-task-form>
        <p class="sidebar-placeholder" *ngIf="activeColumnIdForNewTask === null && editingTask === null">
          Select a column and click "+ Add task", or click "Edit" on a task, to get started.
        </p>
      </aside>
      <div class="board" cdkDropListGroup>
        <app-column *ngFor="let column of board?.columns; let i = index" [column]="column"
                     [accentColor]="columnAccentColor(i)"
                     (rename)="onRenameColumn($event)" (delete)="onDeleteColumn($event)"
                     (drop)="onColumnDropEvent($event)" (editTask)="onEditTask($event)"
                     (addTask)="onAddTaskClicked($event)"></app-column>
        <input #newColumnName class="new-column-input" placeholder="New column name"
               (keyup.enter)="addColumn(newColumnName.value); newColumnName.value = ''" />
      </div>
    </div>
  `,
  styles: [`
    .app-shell { display: flex; gap: 24px; padding: 24px; align-items: flex-start; }
    .sidebar {
      width: 280px;
      flex-shrink: 0;
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
    }
    .sidebar-placeholder { color: var(--color-text-muted); font-size: 0.9em; margin: 0; }
    .board { display: flex; gap: 16px; align-items: flex-start; overflow-x: auto; }
    .new-column-input {
      background: var(--color-surface);
      border: 1px dashed var(--color-border);
      border-radius: 8px;
      padding: 10px 12px;
      min-width: 200px;
      color: var(--color-text);
    }
  `],
```

Note: `columnAccentColor` must also be callable from the template — since it's a module-level function (not a class method), add a class field alias so the template can call it:

```typescript
  readonly columnAccentColor = columnAccentColor;
```

Add this field inside the `BoardComponent` class, alongside the other fields (`board`, `activeColumnIdForNewTask`, etc.).

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/board.component.spec.ts'`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add frontend/src/app/board/board.component.ts frontend/src/app/board/board.component.spec.ts
git commit -m "style: restructure board into sidebar + board layout with column accent colors"
```

---

## Task 3: ColumnComponent — accent dot, task count, and drag-state styling

**Files:**
- Modify: `frontend/src/app/column/column.component.ts`
- Modify: `frontend/src/styles.css` (global CDK drag-state rules — see Global Constraints)
- Test: `frontend/src/app/column/column.component.spec.ts`

**Interfaces:**
- Consumes: `columnAccentColor` output shape from Task 2 (a CSS `var(...)` string).
- Produces: `ColumnComponent.@Input() accentColor: string` (defaults to `'var(--column-accent-1)'` so existing tests that don't set it still render correctly).

- [ ] **Step 1: Write the failing test**

Add to `frontend/src/app/column/column.component.spec.ts`, after the existing `'forwards a task card's edit click...'` test:

```typescript
  it('renders the accent color as the dot\'s background and shows the task count', () => {
    fixture.componentInstance.column = columnWithTask;
    fixture.componentInstance.accentColor = 'var(--column-accent-2)';
    fixture.detectChanges();

    const dot: HTMLElement = fixture.nativeElement.querySelector('.column-dot');
    expect(dot).not.toBeNull();
    expect(dot.style.background).toContain('var(--column-accent-2)');

    const count: HTMLElement = fixture.nativeElement.querySelector('.column-count');
    expect(count).not.toBeNull();
    expect(count.textContent?.trim()).toBe('1');
  });
```

(`columnWithTask` already exists in this file from a prior task — it's a `ColumnWithTasks` with one task.)

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/column.component.spec.ts'`
Expected: FAIL — `.column-dot` / `.column-count` don't exist yet.

- [ ] **Step 3: Add the input and restyle the template**

Modify `frontend/src/app/column/column.component.ts` — add the new input:

```typescript
  @Input() accentColor = 'var(--column-accent-1)';
```

Replace the component's `template` and `styles` with:

```typescript
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
```

- [ ] **Step 4: Add global CDK drag-state styling**

Modify `frontend/src/styles.css` — append after the `body { ... }` rule from Task 1:

```css
.cdk-drag-preview {
  box-sizing: border-box;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
  background: var(--color-surface);
}

.cdk-drag-placeholder {
  opacity: 0.3;
}

.cdk-drag-animating {
  transition: transform 200ms cubic-bezier(0, 0, 0.2, 1);
}

.cdk-drop-list-dragging .task-card:not(.cdk-drag-placeholder) {
  transition: transform 200ms cubic-bezier(0, 0, 0.2, 1);
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/column.component.spec.ts'`
Expected: PASS

- [ ] **Step 6: Run the full suite and build to confirm no regressions**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless && npx ng build`
Expected: all tests pass, build succeeds.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/app/column/column.component.ts frontend/src/app/column/column.component.spec.ts frontend/src/styles.css
git commit -m "style: add column accent dot, task count, and drag-state styling"
```

---

## Task 4: TaskCardComponent — card restyle

**Files:**
- Modify: `frontend/src/app/task-card/task-card.component.ts`

**Interfaces:**
- No interface changes — `@Input() task` and `@Output() edit` are unchanged. This is a pure-CSS task (see Global Constraints) — no new test.

- [ ] **Step 1: Restyle the template and styles**

Modify `frontend/src/app/task-card/task-card.component.ts` — replace the `template` and `styles`:

```typescript
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
```

(This moves the schedule line above the title, matching the reference's card layout, and switches `date:'short'` to `date:'shortTime'` since the card is narrow — only the time matters visually, the date is implicit from the board's context.)

- [ ] **Step 2: Run the full suite to confirm no regressions**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/task-card.component.spec.ts'`
Expected: all 5 existing tests still pass — none of them assert on `date:'short'` vs `date:'shortTime'` formatting text, only on the `.schedule` element's presence/absence, so this change is safe.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/app/task-card/task-card.component.ts
git commit -m "style: restyle task card to match new design (schedule accent, priority/tag tokens)"
```

---

## Task 5: TaskFormComponent — sidebar-panel restyle

**Files:**
- Modify: `frontend/src/app/task-form/task-form.component.ts`

**Interfaces:**
- No interface changes — all `@Input()`/`@Output()` are unchanged. This is a pure-CSS task (see Global Constraints) — no new test.

- [ ] **Step 1: Restyle the template and styles**

Modify `frontend/src/app/task-form/task-form.component.ts` — replace only the `template` and `styles` properties (do not change the class body, methods, or the exported `findClientSideOverlap` function):

```typescript
  template: `
    <form class="task-form" (ngSubmit)="submit()">
      <label class="field-label">Title
        <input class="field-input" [(ngModel)]="title" name="title" placeholder="What needs doing?" />
      </label>
      <label class="field-label">Description
        <textarea class="field-input" [(ngModel)]="description" name="description" placeholder="Details"></textarea>
      </label>
      <label class="field-label">Priority
        <select class="field-input" [(ngModel)]="priority" name="priority">
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="URGENT">Urgent</option>
        </select>
      </label>
      <label class="field-label">Tags
        <input class="field-input" [(ngModel)]="tagsText" name="tags" placeholder="Tags (comma-separated)" />
      </label>
      <div class="time-row">
        <label class="field-label">Starts
          <input class="field-input" [(ngModel)]="startTime" name="startTime" type="datetime-local" />
        </label>
        <label class="field-label">Ends
          <input class="field-input" [(ngModel)]="endTime" name="endTime" type="datetime-local" />
        </label>
      </div>
      <div class="error" *ngFor="let err of errors">{{ err }}</div>
      <div class="conflict" *ngIf="conflict || serverConflict">Overlaps with "{{ (conflict || serverConflict)?.title }}"</div>
      <div class="form-actions">
        <button type="submit" class="primary-button">Save</button>
        <button type="button" class="text-button" (click)="cancel.emit()">Cancel</button>
      </div>
      <button type="button" class="danger-button" *ngIf="editingTask" (click)="requestDelete()">Delete task</button>
    </form>
  `,
  styles: [`
    .task-form { display: flex; flex-direction: column; gap: 12px; }
    .field-label { display: flex; flex-direction: column; gap: 4px; font-size: 0.8em; color: var(--color-text-muted); }
    .field-input {
      font-size: 0.95em;
      padding: 8px 10px;
      border: 1px solid var(--color-border);
      border-radius: 6px;
      background: var(--color-surface);
      color: var(--color-text);
      font-family: inherit;
    }
    .time-row { display: flex; gap: 8px; }
    .time-row .field-label { flex: 1; }
    .error { color: var(--color-danger); font-size: 0.85em; }
    .conflict { background: var(--priority-high-bg); color: var(--priority-high-text); padding: 6px 10px; border-radius: 4px; }
    .form-actions { display: flex; gap: 8px; }
    .primary-button {
      background: var(--color-accent);
      color: white;
      border: none;
      border-radius: 6px;
      padding: 8px 16px;
      cursor: pointer;
      font-weight: 600;
    }
    .text-button { background: none; border: 1px solid var(--color-border); border-radius: 6px; padding: 8px 16px; cursor: pointer; color: var(--color-text); }
    .danger-button { background: none; border: 1px solid var(--color-danger); color: var(--color-danger); border-radius: 6px; padding: 6px 12px; cursor: pointer; align-self: flex-start; }
  `],
```

- [ ] **Step 2: Run the full suite to confirm no regressions**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/task-form.component.spec.ts'`
Expected: all 18 existing tests still pass — none assert on CSS classes/markup structure beyond `.error`/`.conflict` presence and button clicks by class (`.edit-btn` is in `TaskCardComponent`, not here), all of which are preserved.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/app/task-form/task-form.component.ts
git commit -m "style: restyle task form as a sidebar panel matching the new design"
```

---

## Task 6: AppComponent — simple header, drop dead files

**Files:**
- Modify: `frontend/src/app/app.component.ts`
- Delete: `frontend/src/app/app.component.html`
- Delete: `frontend/src/app/app.component.css`
- Test: `frontend/src/app/app.component.spec.ts`

**Interfaces:**
- No changes consumed by other components — `AppComponent` is the root, nothing depends on it.

- [ ] **Step 1: Write the failing test**

Modify `frontend/src/app/app.component.spec.ts` — add after the existing `'should render the board'` test:

```typescript
  it('renders the app header with the app name', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('To Do');
  });
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/app.component.spec.ts'`
Expected: FAIL — no `<h1>` exists yet.

- [ ] **Step 3: Add the header, remove the unused title field, delete dead files**

Modify `frontend/src/app/app.component.ts` in full:

```typescript
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BoardComponent } from './board/board.component';
import { ToastService } from './services/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, BoardComponent],
  template: `
    <div class="toast" *ngIf="toastService.message()">{{ toastService.message() }}</div>
    <header class="app-header"><h1>To Do</h1></header>
    <app-board></app-board>
  `,
  styles: [`
    .toast {
      position: fixed;
      top: 16px;
      right: 16px;
      background: var(--color-surface);
      color: var(--color-text);
      border: 1px solid var(--color-border);
      padding: 10px 16px;
      border-radius: 6px;
      z-index: 1000;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    .app-header { padding: 20px 24px 0; }
    .app-header h1 { margin: 0; font-size: 1.3em; color: var(--color-text); }
  `],
})
export class AppComponent {
  protected readonly toastService = inject(ToastService);
}
```

(This removes the unused `title = 'frontend'` field left over from Angular CLI scaffolding, and switches the toast's hardcoded colors to design tokens.)

Delete the two dead files (the component has used an inline `template`/`styles` since Task 11 of the original implementation plan — these files have not been referenced since):

```bash
rm frontend/src/app/app.component.html frontend/src/app/app.component.css
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless --include='**/app.component.spec.ts'`
Expected: PASS

- [ ] **Step 5: Run the full suite and build to confirm no regressions**

Run: `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless && npx ng build`
Expected: all tests pass, build succeeds (confirms deleting the two dead files broke nothing).

- [ ] **Step 6: Commit**

```bash
git add frontend/src/app/app.component.ts frontend/src/app/app.component.spec.ts
git rm frontend/src/app/app.component.html frontend/src/app/app.component.css
git commit -m "style: add simple app header, remove dead template/style files"
```

---

## Manual Verification (after Task 6)

1. `cd backend && mvn spring-boot:run`
2. `cd frontend && npx ng serve`
3. Open `http://localhost:4200`. Confirm: columns render side-by-side with colored dots and task counts in the header; the sidebar shows the placeholder text until you click "+ Add task" or "Edit" on a card; creating and editing a task both work through the sidebar panel; dragging a card shows a shadowed preview and a faded placeholder gap at the drop target; switching the OS to dark mode (System Settings → Appearance, or your browser's dev tools "Emulate CSS media feature prefers-color-scheme") flips the whole UI to the dark palette without a page reload.
