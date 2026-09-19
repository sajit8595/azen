import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { unwrap } from './api';
import { ApiResponse, LoginRequest, LoginResponse, Role } from './models';

const TOKEN_KEY = 'sentinel.token';
const ROLE_KEY = 'sentinel.role';
const USER_KEY = 'sentinel.username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = environment.apiBaseUrl;

  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly _role = signal<Role | null>(localStorage.getItem(ROLE_KEY) as Role | null);
  private readonly _username = signal<string | null>(localStorage.getItem(USER_KEY));

  readonly token = this._token.asReadonly();
  readonly role = this._role.asReadonly();
  readonly username = this._username.asReadonly();
  readonly isAuthenticated = computed(() => !!this._token());
  readonly isAdmin = computed(() => this._role() === 'ADMIN');

  constructor(private http: HttpClient) {}

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.base}/auth/login`, body).pipe(
      map(unwrap),
      tap((res) => this.persist(res)),
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(USER_KEY);
    this._token.set(null);
    this._role.set(null);
    this._username.set(null);
  }

  getToken(): string | null {
    return this._token();
  }

  private persist(res: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(ROLE_KEY, res.role);
    localStorage.setItem(USER_KEY, res.username);
    this._token.set(res.token);
    this._role.set(res.role);
    this._username.set(res.username);
  }
}
