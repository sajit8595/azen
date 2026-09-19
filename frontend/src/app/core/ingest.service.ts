import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { ApiResponse, IngestResult } from './models';

export type IngestKind = 'customers' | 'accounts' | 'transactions';

@Injectable({ providedIn: 'root' })
export class IngestService {
  private http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/ingest`;

  upload(kind: IngestKind, file: File): Observable<IngestResult> {
    const form = new FormData();
    form.append('file', file);
    return this.http
      .post<ApiResponse<IngestResult>>(`${this.base}/${kind}`, form)
      .pipe(map(unwrap));
  }
}
