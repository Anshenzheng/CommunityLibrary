import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
  role: 'READER' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE';
  createTime: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  realName: string;
  role: 'READER' | 'ADMIN';
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  email?: string;
  phone?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem(this.USER_KEY);
    if (storedUser) {
      try {
        this.currentUserSubject.next(JSON.parse(storedUser));
      } catch (e) {
        this.logout();
      }
    }
  }
  
  login(credentials: LoginRequest): Observable<{ success: boolean; message: string; data: LoginResponse }> {
    return this.http.post<{ success: boolean; message: string; data: LoginResponse }>(
      `${environment.apiUrl}/auth/login`,
      credentials
    ).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setToken(response.data.token);
          const user: User = {
            id: response.data.id,
            username: response.data.username,
            realName: response.data.realName,
            role: response.data.role,
            status: 'ACTIVE',
            createTime: ''
          };
          this.setCurrentUser(user);
        }
      })
    );
  }
  
  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/auth/register`,
      userData
    );
  }
  
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }
  
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  
  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }
  
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  
  setCurrentUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }
  
  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.getCurrentUser();
  }
  
  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ADMIN';
  }
  
  getCurrentUserProfile(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/users/me`);
  }
  
  updateProfile(data: { realName?: string; email?: string; phone?: string }): Observable<any> {
    return this.http.put(`${environment.apiUrl}/users/me/profile`, data);
  }
  
  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${environment.apiUrl}/users/me/password`, {
      oldPassword,
      newPassword
    });
  }
}
