import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BoardService } from './board.service';
import { Board } from '../models/models';

describe('BoardService', () => {
  let service: BoardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BoardService],
    });
    service = TestBed.inject(BoardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('fetches the board', () => {
    const mockBoard: Board = { columns: [] };
    service.getBoard().subscribe((board) => expect(board).toEqual(mockBoard));
    const req = httpMock.expectOne('/api/board');
    expect(req.request.method).toBe('GET');
    req.flush(mockBoard);
  });

  it('creates a column', () => {
    service.createColumn('Backlog').subscribe();
    const req = httpMock.expectOne('/api/columns');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Backlog' });
    req.flush({ id: 1, name: 'Backlog', position: 0, createdAt: '' });
  });

  it('moves a task', () => {
    service.moveTask(5, { targetColumnId: 2, targetPosition: 1 }).subscribe();
    const req = httpMock.expectOne('/api/tasks/5/move');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ targetColumnId: 2, targetPosition: 1 });
    req.flush({});
  });

  it('deletes a task', () => {
    service.deleteTask(5).subscribe();
    const req = httpMock.expectOne('/api/tasks/5');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
