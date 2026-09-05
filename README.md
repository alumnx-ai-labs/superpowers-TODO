# To Do

A visual, single-user task management web app: one Kanban-style board with user-defined
workflow columns, drag-and-drop task movement/reordering, optional task scheduling with
hard overlap prevention, priorities, and freeform tags.

- **Backend:** Java 21, Spring Boot 3.3.4, Spring Data JPA, SQLite (`org.xerial:sqlite-jdbc`)
- **Frontend:** Angular 18 (standalone components), Angular CDK Drag & Drop

See [docs/superpowers/specs/2026-09-04-todo-app-design.md](docs/superpowers/specs/2026-09-04-todo-app-design.md)
for the full design spec and [docs/superpowers/plans/2026-09-04-todo-app.md](docs/superpowers/plans/2026-09-04-todo-app.md)
for the implementation plan.

## Prerequisites

- Java 21+
- Maven (or use the included wrapper, if present)
- Node.js 18+ and npm

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. A SQLite file (`backend/todo.db`) is created
automatically on first run — no separate database setup is needed.

To run the backend test suite:

```bash
cd backend
mvn test
```

## Running the frontend

```bash
cd frontend
npm install
npx ng serve
```

The app is served on `http://localhost:4200` and proxies API calls to the backend on port
8080 (see `frontend/proxy.conf.json`). Start the backend first.

To run the frontend test suite:

```bash
cd frontend
npx ng test --watch=false --browsers=ChromeHeadless
```

## Running both together

Start the backend and frontend in two separate terminals as described above, then open
`http://localhost:4200` in a browser.
