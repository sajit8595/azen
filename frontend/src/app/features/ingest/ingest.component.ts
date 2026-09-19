import { Component, inject, signal } from '@angular/core';
import { IngestKind, IngestService } from '../../core/ingest.service';
import { IngestResult } from '../../core/models';

interface UploadState {
  file: File | null;
  loading: boolean;
  result: IngestResult | null;
  error: string | null;
}

@Component({
  selector: 'app-ingest',
  imports: [],
  templateUrl: './ingest.component.html',
})
export class Ingest {
  private ingest = inject(IngestService);

  readonly steps: { kind: IngestKind; title: string; hint: string }[] = [
    { kind: 'customers', title: '1. Customers', hint: 'KYC records — upload first' },
    { kind: 'accounts', title: '2. Accounts', hint: 'Linked to customers' },
    { kind: 'transactions', title: '3. Transactions', hint: 'Runs detection on upload' },
  ];

  state = signal<Record<IngestKind, UploadState>>({
    customers: this.blank(),
    accounts: this.blank(),
    transactions: this.blank(),
  });

  private blank(): UploadState {
    return { file: null, loading: false, result: null, error: null };
  }

  onFile(kind: IngestKind, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.patch(kind, { file, result: null, error: null });
  }

  upload(kind: IngestKind): void {
    const current = this.state()[kind];
    if (!current.file) return;
    this.patch(kind, { loading: true, error: null, result: null });
    this.ingest.upload(kind, current.file).subscribe({
      next: (result) => this.patch(kind, { loading: false, result }),
      error: (err) =>
        this.patch(kind, {
          loading: false,
          error: err?.error?.errors?.[0]?.message ?? 'Upload failed. Check the file and try again.',
        }),
    });
  }

  private patch(kind: IngestKind, changes: Partial<UploadState>): void {
    this.state.update((s) => ({ ...s, [kind]: { ...s[kind], ...changes } }));
  }
}
