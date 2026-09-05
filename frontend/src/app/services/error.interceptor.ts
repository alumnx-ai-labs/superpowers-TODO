import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from './toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isOverlapOnTaskEndpoint = error.status === 409 && req.url.includes('/api/tasks');
      if (!isOverlapOnTaskEndpoint) {
        const message = error.error?.error ?? 'Something went wrong. Please try again.';
        toast.show(message);
      }
      return throwError(() => error);
    }),
  );
};
