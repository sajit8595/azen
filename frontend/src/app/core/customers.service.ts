import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { ApiResponse, CustomerDetail, CustomerSummary, Page, Transaction } from './models';

@Injectable({ providedIn: 'root' })
export class CustomersService {
  private readonly base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  list(page = 0, size = 20, riskRating?: string): Observable<Page<CustomerSummary>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (riskRating) params = params.set('riskRating', riskRating);
    return this.http
      .get<ApiResponse<Page<CustomerSummary>>>(`${this.base}/customers`, { params })
      .pipe(map(unwrap));
  }

  get(id: string): Observable<CustomerDetail> {
    return this.http
      .get<ApiResponse<CustomerDetail>>(`${this.base}/customers/${id}`)
      .pipe(map(unwrap));
  }

  transactions(id: string): Observable<Transaction[]> {
    return this.http
      .get<ApiResponse<Transaction[]>>(`${this.base}/customers/${id}/transactions`)
      .pipe(map(unwrap));
  }

  accountTransactions(accountId: string): Observable<Transaction[]> {
    return this.http
      .get<ApiResponse<Transaction[]>>(`${this.base}/accounts/${accountId}/transactions`)
      .pipe(map(unwrap));
  }
}
