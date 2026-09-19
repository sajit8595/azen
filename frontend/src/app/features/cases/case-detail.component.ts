import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CasesService } from '../../core/cases.service';
import { AmlCase, Disposition } from '../../core/models';
import { severityBadge, statusBadge, dispositionBadge } from '../../core/badges';

@Component({
  selector: 'app-case-detail',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './case-detail.component.html',
})
export class CaseDetailComponent {
  private cases = inject(CasesService);
  private route = inject(ActivatedRoute);

  case = signal<AmlCase | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);
  saving = signal(false);
  saveError = signal<string | null>(null);
  saved = signal(false);

  disposition: Disposition | '' = '';
  reason = '';

  sevBadge = severityBadge;
  statBadge = statusBadge;
  dispBadge = dispositionBadge;

  private id = Number(this.route.snapshot.paramMap.get('id'));

  constructor() {
    this.load();
  }

  label(v?: string): string {
    return (v ?? '').replace(/_/g, ' ');
  }

  get isDisposed(): boolean {
    return this.case()?.status === 'DISPOSED';
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.cases.get(this.id).subscribe({
      next: (c) => {
        this.case.set(c);
        this.disposition = c.disposition ?? '';
        this.reason = c.dispositionReason ?? '';
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load case. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    this.saveError.set(null);
    this.saved.set(false);
    if (!this.disposition) {
      this.saveError.set('Select a disposition.');
      return;
    }
    if (!this.reason.trim()) {
      this.saveError.set('A reason is required.');
      return;
    }
    this.saving.set(true);
    this.cases
      .disposition(this.id, { disposition: this.disposition, reason: this.reason.trim() })
      .subscribe({
        next: (c) => {
          this.case.set(c);
          this.saving.set(false);
          this.saved.set(true);
        },
        error: () => {
          this.saving.set(false);
          this.saveError.set('Could not save disposition.');
        },
      });
  }
}
