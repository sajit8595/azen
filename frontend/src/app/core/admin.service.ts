import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { ApiResponse, AuditLog, RuleConfig, RuleConfigUpdate } from './models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly base = `${environment.apiBaseUrl}/admin`;

  constructor(private http: HttpClient) {}

  rules(): Observable<RuleConfig[]> {
    return this.http
      .get<ApiResponse<RuleConfig[]>>(`${this.base}/rules`)
      .pipe(map(unwrap));
  }

  updateRule(code: string, patch: RuleConfigUpdate): Observable<RuleConfig> {
    return this.http
      .patch<ApiResponse<RuleConfig>>(`${this.base}/rules/${code}`, patch)
      .pipe(map(unwrap));
  }

  audit(entityType?: string, entityId?: string): Observable<AuditLog[]> {
    let params = new HttpParams();
    if (entityType) params = params.set('entityType', entityType);
    if (entityId) params = params.set('entityId', entityId);
    return this.http
      .get<ApiResponse<AuditLog[]>>(`${environment.apiBaseUrl}/audit`, { params })
      .pipe(map(unwrap));
  }
}
