import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { BoardService } from './services/board.service';
import { Board } from './models/models';

describe('AppComponent', () => {
  beforeEach(async () => {
    const boardServiceStub = { getBoard: () => of<Board>({ columns: [] }) };
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{ provide: BoardService, useValue: boardServiceStub }],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the board', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-board')).toBeTruthy();
  });

  it('renders the app header with the app name', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('To Do');
  });
});
