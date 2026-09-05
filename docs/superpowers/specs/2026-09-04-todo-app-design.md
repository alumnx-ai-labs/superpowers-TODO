# To Do — Visual Task Management App — Design Spec

Date: 2026-09-04

## Overview

A production-quality, single-user, local visual task management web app called "To Do." Users manage tasks on a single board organized into user-defined workflow columns (Kanban-style), with drag-and-drop for moving and reordering tasks, optional task scheduling with hard overlap prevention, priorities, and freeform tags.

## Scope

- Single board, single user, no authentication.
- Local persistence via SQLite.
- Frontend: Angular (standalone components, Angular CDK Drag & Drop).
- Backend: Java Spring Boot REST API.

Out of scope (explicitly not building): multi-user accounts/login, multiple boards, real-time multi-client sync (websockets), managed/colored tag entities, recurring tasks, notifications/reminders.

## Architecture

- **Frontend**: Angular app (standalone components), using `@angular/cdk/drag-drop` for drag-and-drop. Runs via `ng serve` on port 4200 in development.
- **Backend**: Spring Boot REST API, layered as Controller → Service → Repository (Spring Data JPA). Runs on port 8080.
- **Database**: SQLite file, accessed via JDBC (`org.xerial:sqlite-jdbc`) with a Hibernate SQLite dialect. The exact dialect dependency will be pinned during implementation setup (options: `community.dialects` Hibernate Community Dialects module, or a maintained third-party SQLite dialect — implementation plan should verify current best-maintained option against the Spring Boot version chosen).
- **Dev proxy**: Angular dev server proxies `/api/*` requests to `http://localhost:8080` via `proxy.conf.json`, so the frontend always calls relative `/api/...` paths.
- **Production**: Spring Boot serves the built Angular static assets from its resources, single deployable jar (noted for later; not required for initial implementation).
- **Data flow**: Angular fetches the full board (`GET /api/board`) once on load and keeps it in memory as the source of truth for rendering; every user action (create, edit, move, delete) calls a REST endpoint and updates local state from the response (or optimistically, per the drag-drop flow below). No websockets — single user, no concurrent-client sync needed.

## Data Model

### `workflow_column`
| Field | Type | Notes |
|---|---|---|
| id | Long (PK) | auto-generated |
| name | String | required, non-blank |
| position | Integer | order among columns, 0-based, renumbered on reorder |
| createdAt | DateTime | set on creation |

### `task`
| Field | Type | Notes |
|---|---|---|
| id | Long (PK) | auto-generated |
| columnId | Long (FK → workflow_column) | required |
| position | Integer | order within column, 0-based, renumbered on move/reorder |
| title | String | required, non-blank |
| description | String (text) | optional |
| priority | Enum: LOW, MEDIUM, HIGH, URGENT | required, defaults to MEDIUM if not specified |
| startTime | DateTime | nullable |
| endTime | DateTime | nullable — must be null iff startTime is null; must be strictly after startTime when set |
| createdAt | DateTime | set on creation |
| updatedAt | DateTime | updated on every edit |

### `task_tag`
| Field | Type | Notes |
|---|---|---|
| taskId | Long (FK → task) | |
| tag | String | freeform text, no separate tag entity/CRUD |

Composite key (taskId, tag); a task may have zero or more tags.

**Status** is not a stored field — a task's status is implicitly the workflow column it belongs to.

## Overlap Prevention Rule

- Applies only to tasks where both `startTime` and `endTime` are set (scheduling is optional).
- On task create or update where a schedule is being set/changed, the service layer checks **all** other tasks on the board (regardless of column) with a non-null schedule for any time-range overlap using standard half-open interval overlap: `existing.startTime < new.endTime AND new.startTime < existing.endTime`.
- If any overlap is found, the request is rejected with `409 Conflict`, and the response body identifies the conflicting task (id, title, startTime, endTime) so the frontend can show a precise message.
- This check is backend-authoritative. The frontend additionally performs the same check client-side against its in-memory task list when the user is picking a time, to give instant feedback before the round-trip — but the backend check is what's actually enforced, and is re-verified on every create/update server-side (never trust client-side validation alone).

## API Design

Base path: `/api`

| Method | Path | Purpose | Request body | Success | Errors |
|---|---|---|---|---|---|
| GET | `/board` | Fetch entire board state | — | 200, `{columns: [{...column, tasks: [...]}]}` | — |
| POST | `/columns` | Create column | `{name}` | 201, created column | 400 if name blank |
| PATCH | `/columns/{id}` | Rename and/or reorder column | `{name?, position?}` | 200, updated column | 404 if not found |
| DELETE | `/columns/{id}` | Delete column | — | 204 | 409 if column has any tasks |
| POST | `/tasks` | Create task | `{columnId, title, description?, priority?, tags?: string[], startTime?, endTime?}` | 201, created task | 400 validation; 409 overlap |
| PATCH | `/tasks/{id}` | Edit task fields | any subset of task fields | 200, updated task | 400 validation; 404; 409 overlap |
| DELETE | `/tasks/{id}` | Delete task | — | 204 | 404 |
| POST | `/tasks/{id}/move` | Move task between/within columns (drag-and-drop) | `{targetColumnId, targetPosition}` | 200, updated task (+ affected siblings' new positions if the response includes the full board, or just the moved task — implementation detail) | 404 |

- `move` is a dedicated endpoint (rather than overloaded into PATCH) because it is the hot path for drag-and-drop and must, in one transaction, renumber sibling task positions in both the source and target columns.
- All column/task reordering (column reorder via PATCH `position`, task reorder via `move`) renumbers affected siblings to a clean 0..N-1 sequence within the same transaction — no fractional positions.
- Validation errors (400) return a structured error body: `{error: string, fields?: {field: message}}`.
- Overlap errors (409) return: `{error: "SCHEDULE_OVERLAP", conflictingTask: {id, title, startTime, endTime}}`.

## Frontend Structure

- **BoardComponent** (top-level): fetches `/api/board` on init, owns the in-memory board state, hosts `cdkDropListGroup` wrapping all columns.
- **ColumnComponent**: renders one `cdkDropList` of task cards for its column; controls to rename the column, add a task, delete the column (disabled/error-toast if non-empty).
- **TaskCardComponent**: displays title, priority badge (color-coded per level), tag chips, and schedule time range if set.
- **TaskFormComponent** (modal or side panel): create/edit form — title, description, priority select, freeform tag input (chip-style entry), optional date + start/end time pickers. Performs the client-side overlap pre-check described above for instant feedback; submits to backend on save.
- **Drag-and-drop flow**: `cdkDropListDropped` handler computes the new `columnId` + target index, optimistically updates local board state immediately for a responsive feel, then calls `POST /tasks/{id}/move`. On failure, reverts the optimistic update and shows an error toast.
- **Global error handling**: an `HttpInterceptor` catches non-2xx responses and shows a toast/snackbar for general errors; the 409 overlap error on task save is instead surfaced inline within `TaskFormComponent` (near the date/time fields) rather than as a generic toast, since it's actionable there.

## Testing Strategy

- **Backend**: JUnit 5 with `@DataJpaTest`/`@SpringBootTest` covering:
  - Overlap detection edge cases: exact boundary touch (should NOT count as overlap, since half-open), full containment, partial overlap on each side, identical range.
  - Move/reorder logic: moving within a column, moving across columns, resulting position sequences are always a clean 0..N-1 renumbering with no gaps or duplicates.
  - Column deletion blocked when non-empty.
- **Frontend**: Angular CLI's default test runner (Jasmine/Karma, or Jest if selected at project setup) covering:
  - Drag-drop index/column calculation logic.
  - Client-side overlap pre-check logic in the task form.
  - Form validation (required title, endTime after startTime, etc).
- No e2e test framework included in initial scope.

## Non-Goals (Explicit)

- No multi-board support.
- No authentication/authorization.
- No managed tag entities (colors, tag CRUD) — tags are freeform strings.
- No real-time multi-client sync.
- No recurring tasks or notifications/reminders.
