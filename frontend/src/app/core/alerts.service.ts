import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { Alert, AlertStats, ApiResponse, Page } from './models';

export interface AlertQuery {
  page?: number;
  size?: number;
  status?: string;
  minRiskScore?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class AlertsService {
  private readonly base = `${environment.apiBaseUrl}/alerts`;

  constructor(private http: HttpClient) {}

  list(q: AlertQuery = {}): Observable<Page<Alert>> {
    let params = new HttpParams()
      .set('page', String(q.page ?? 0))
      .set('size', String(q.size ?? 20))
      .set('sort', q.sort ?? 'riskScore,desc');
    if (q.status) params = params.set('status', q.status);
    if (q.minRiskScore != null) params = params.set('minRiskScore', String(q.minRiskScore));
    return this.http
      .get<ApiResponse<Page<Alert>>>(this.base, { params })
      .pipe(map(unwrap));
  }

  get(id: number): Observable<Alert> {
    return this.http
      .get<ApiResponse<Alert>>(`${this.base}/${id}`)
      .pipe(map(unwrap));
  }

  stats(): Observable<AlertStats> {
    return this.http
      .get<ApiResponse<AlertStats>>(`${this.base}/stats`)
      .pipe(map(unwrap));
  }
}
