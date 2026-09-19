import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { AmlCase, ApiResponse, CreateCaseRequest, DispositionRequest } from './models';

@Injectable({ providedIn: 'root' })
export class CasesService {
  private readonly base = `${environment.apiBaseUrl}/cases`;

  constructor(private http: HttpClient) {}

  list(status?: string): Observable<AmlCase[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http
      .get<ApiResponse<AmlCase[]>>(this.base, { params })
      .pipe(map(unwrap));
  }

  get(id: number): Observable<AmlCase> {
    return this.http
      .get<ApiResponse<AmlCase>>(`${this.base}/${id}`)
      .pipe(map(unwrap));
  }

  create(body: CreateCaseRequest): Observable<AmlCase> {
    return this.http
      .post<ApiResponse<AmlCase>>(this.base, body)
      .pipe(map(unwrap));
  }

  disposition(id: number, body: DispositionRequest): Observable<AmlCase> {
    return this.http
      .patch<ApiResponse<AmlCase>>(`${this.base}/${id}/disposition`, body)
      .pipe(map(unwrap));
  }
}
