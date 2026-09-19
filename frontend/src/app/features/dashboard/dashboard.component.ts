import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AlertsService } from '../../core/alerts.service';
import { AlertStats } from '../../core/models';
import { severityBadge } from '../../core/badges';

interface Bar {
  key: string;
  value: number;
  pct: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
})
export class Dashboard {
  private alerts = inject(AlertsService);

  stats = signal<AlertStats | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  total = computed(() => {
    const s = this.stats();
    return s ? s.open + s.inReview + s.closed : 0;
  });

  sevBadge = severityBadge;

  private readonly severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

  severityBars = computed<Bar[]>(() => {
    const s = this.stats();
    if (!s?.bySeverity) return [];
    return this.toBars(s.bySeverity, this.severityOrder);
  });

  ruleBars = computed<Bar[]>(() => {
    const s = this.stats();
    if (!s?.byRule) return [];
    return this.toBars(s.byRule);
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.alerts.stats().subscribe({
      next: (s) => {
        this.stats.set(s);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load stats. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  severityColor(key: string): string {
    switch (key) {
      case 'CRITICAL':
        return 'bg-danger';
      case 'HIGH':
        return 'bg-warning';
      case 'MEDIUM':
        return 'bg-info';
      default:
        return 'bg-success';
    }
  }

  private toBars(map: Record<string, number>, order?: string[]): Bar[] {
    const entries = Object.entries(map);
    const max = Math.max(1, ...entries.map(([, v]) => v));
    let bars = entries.map(([key, value]) => ({ key, value, pct: (value / max) * 100 }));
    if (order) {
      bars = bars.sort((a, b) => order.indexOf(a.key) - order.indexOf(b.key));
    } else {
      bars = bars.sort((a, b) => b.value - a.value);
    }
    return bars;
  }
}
