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
