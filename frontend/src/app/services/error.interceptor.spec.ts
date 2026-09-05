import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from './error.interceptor';
import { ToastService } from './toast.service';

describe('errorInterceptor', () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let toastSpy: jasmine.Spy;

  beforeEach(() => {
    toastSpy = jasmine.createSpy('show');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: ToastService, useValue: { show: toastSpy } },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('shows a toast for a generic 404 error', () => {
    http.get('/api/columns/9999').subscribe({ error: () => {} });
    httpMock.expectOne('/api/columns/9999').flush({ error: 'not found' }, { status: 404, statusText: 'Not Found' });
    expect(toastSpy).toHaveBeenCalled();
  });

  it('does not toast a 409 on a task endpoint, letting the caller handle it', () => {
    http.patch('/api/tasks/1', {}).subscribe({ error: () => {} });
    httpMock.expectOne('/api/tasks/1').flush(
      { error: 'SCHEDULE_OVERLAP', conflictingTask: {} }, { status: 409, statusText: 'Conflict' },
    );
    expect(toastSpy).not.toHaveBeenCalled();
  });
});
