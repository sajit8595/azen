import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertsService } from '../../core/alerts.service';
import { CasesService } from '../../core/cases.service';
import { CustomersService } from '../../core/customers.service';
import { Alert, Transaction } from '../../core/models';
import { severityBadge, statusBadge } from '../../core/badges';

@Component({
  selector: 'app-alert-detail',
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './alert-detail.component.html',
})
export class AlertDetailComponent {
  private alerts = inject(AlertsService);
  private cases = inject(CasesService);
  private customers = inject(CustomersService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  alert = signal<Alert | null>(null);
  timeline = signal<Transaction[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  creatingCase = signal(false);
  caseError = signal<string | null>(null);

  sevBadge = severityBadge;
  statBadge = statusBadge;

  private id = Number(this.route.snapshot.paramMap.get('id'));

  constructor() {
    this.load();
  }

  isEvidence(txnId: string): boolean {
    return this.alert()?.evidenceTxnIds?.includes(txnId) ?? false;
  }

  label(status: string): string {
    return status.replace('_', ' ');
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.alerts.get(this.id).subscribe({
      next: (a) => {
        this.alert.set(a);
        this.loading.set(false);
        // Load the customer's transaction timeline; evidence txns are highlighted.
        this.customers.transactions(a.customerId).subscribe({
          next: (txns) => this.timeline.set(txns),
          error: () => {},
        });
      },
      error: () => {
        this.error.set('Could not load alert. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  createCase(): void {
    const a = this.alert();
    if (!a) return;
    this.creatingCase.set(true);
    this.caseError.set(null);
    this.cases.create({ alertId: a.id }).subscribe({
      next: (c) => {
        this.creatingCase.set(false);
        this.router.navigate(['/cases', c.id]);
      },
      error: () => {
        this.creatingCase.set(false);
        this.caseError.set('Could not create case.');
      },
    });
  }
}
