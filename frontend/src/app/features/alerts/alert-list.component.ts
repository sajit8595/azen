import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AlertsService } from '../../core/alerts.service';
import { Alert, Page } from '../../core/models';
import { maskName } from '../../core/mask';
import { severityBadge, statusBadge } from '../../core/badges';

@Component({
  selector: 'app-alert-list',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './alert-list.component.html',
})
export class AlertList {
  private alerts = inject(AlertsService);
  private route = inject(ActivatedRoute);

  page = signal<Page<Alert> | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  status = '';
  minRiskScore: number | null = null;
  sort = 'riskScore,desc';
  pageIndex = 0;
  size = 20;

  mask = maskName;
  sevBadge = severityBadge;
  statBadge = statusBadge;

  constructor() {
    const qp = this.route.snapshot.queryParamMap.get('status');
    if (qp) this.status = qp;
    this.load();
  }

  reload(): void {
    this.pageIndex = 0;
    this.load();
  }

  go(index: number): void {
    this.pageIndex = index;
    this.load();
  }

  label(status: string): string {
    return status.replace('_', ' ');
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.alerts
      .list({
        page: this.pageIndex,
        size: this.size,
        status: this.status || undefined,
        minRiskScore: this.minRiskScore ?? undefined,
        sort: this.sort,
      })
      .subscribe({
        next: (p) => {
          this.page.set(p);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load alerts. Is the backend running?');
          this.loading.set(false);
        },
      });
  }
}
