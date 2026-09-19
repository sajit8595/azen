import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { AuditLog } from '../../core/models';

@Component({
  selector: 'app-audit-trail',
  imports: [FormsModule, DatePipe],
  templateUrl: './audit-trail.component.html',
})
export class AuditTrailComponent {
  private admin = inject(AdminService);

  logs = signal<AuditLog[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);

  entityType = '';
  entityId = '';

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin.audit(this.entityType || undefined, this.entityId || undefined).subscribe({
      next: (l) => {
        this.logs.set(l);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load audit trail. ADMIN role required.');
        this.loading.set(false);
      },
    });
  }

  clear(): void {
    this.entityType = '';
    this.entityId = '';
    this.load();
  }
}
