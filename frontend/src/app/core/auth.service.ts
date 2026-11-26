import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, User } from './models';

const TOKEN_KEY = 'fintrack.token';
const USER_KEY = 'fintrack.user';
const EXPIRES_KEY = 'fintrack.expires';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _user = signal<User | null>(this.restoreUser());
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null && this.token() !== null);

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password })
      .pipe(tap(res => this.store(res)));
  }

  register(name: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', { name, email, password })
      .pipe(tap(res => this.store(res)));
  }

  logout(): void {
    for (const key of [TOKEN_KEY, USER_KEY, EXPIRES_KEY]) {
      safeStorage()?.removeItem(key);
    }
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    const storage = safeStorage();
    const token = storage?.getItem(TOKEN_KEY) ?? null;
    const expires = Number(storage?.getItem(EXPIRES_KEY) ?? 0);
    if (!token || Date.now() > expires) {
      return null;
    }
    return token;
  }

  private store(res: AuthResponse): void {
    const storage = safeStorage();
    storage?.setItem(TOKEN_KEY, res.token);
    storage?.setItem(USER_KEY, JSON.stringify(res.user));
    storage?.setItem(EXPIRES_KEY, String(Date.now() + res.expiresIn * 1000));
    this._user.set(res.user);
  }

  private restoreUser(): User | null {
    try {
      const raw = safeStorage()?.getItem(USER_KEY);
      return raw && this.token() ? JSON.parse(raw) as User : null;
    } catch {
      return null;
    }
  }
}

function safeStorage(): Storage | null {
  try {
    return window.localStorage;
  } catch {
    return null;
  }
}
