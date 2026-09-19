import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CasesService } from '../../core/cases.service';
import { AmlCase } from '../../core/models';
import { statusBadge, dispositionBadge } from '../../core/badges';

@Component({
  selector: 'app-case-list',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './case-list.component.html',
})
export class CaseListComponent {
  private cases = inject(CasesService);

  items = signal<AmlCase[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  status = '';

  statBadge = statusBadge;
  dispBadge = dispositionBadge;

  constructor() {
    this.load();
  }

  label(v?: string): string {
    return (v ?? '').replace(/_/g, ' ');
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.cases.list(this.status || undefined).subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load cases. Is the backend running?');
        this.loading.set(false);
      },
    });
  }
}
