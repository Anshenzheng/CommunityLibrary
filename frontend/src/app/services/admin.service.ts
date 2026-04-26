import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageResponse } from './book.service';
import { User } from './auth.service';
import { BorrowRecord } from './borrow.service';

export interface DashboardStats {
  totalUsers: number;
  totalReaders: number;
  totalBooks: number;
  availableBooks: number;
  borrowedBooks: number;
  pendingRequests: number;
  overdueCount: number;
  weeklyBorrows: number;
  weeklyReturns: number;
}

export interface Statistics {
  id: number;
  statDate: string;
  totalUsers: number;
  totalBooks: number;
  totalBorrowed: number;
  totalReturned: number;
  totalOverdue: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  constructor(private http: HttpClient) { }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${environment.apiUrl}/admin/dashboard`);
  }

  getUsers(
    keyword?: string,
    role?: string,
    status?: string,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (role) {
      params = params.set('role', role);
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<PageResponse<User>>(`${environment.apiUrl}/admin/users`, { params });
  }

  updateUserStatus(userId: number, status: string): Observable<any> {
    return this.http.put(`${environment.apiUrl}/admin/users/${userId}/status`, { status });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put(`${environment.apiUrl}/admin/users/${userId}/role`, { role });
  }

  getBorrowRecords(
    keyword?: string,
    status?: string,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<BorrowRecord>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<PageResponse<BorrowRecord>>(`${environment.apiUrl}/admin/borrow-records`, { params });
  }

  approveBorrow(recordId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/admin/borrow-records/${recordId}/approve`, null);
  }

  rejectBorrow(recordId: number, reason: string): Observable<any> {
    return this.http.post(`${environment.apiUrl}/admin/borrow-records/${recordId}/reject`, { reason });
  }

  getStatistics(startDate?: string, endDate?: string): Observable<Statistics[]> {
    let params = new HttpParams();
    
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    
    return this.http.get<Statistics[]>(`${environment.apiUrl}/admin/statistics`, { params });
  }

  exportUsers(keyword?: string, role?: string, status?: string): Observable<Blob> {
    let params = new HttpParams();
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (role) {
      params = params.set('role', role);
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get(`${environment.apiUrl}/admin/export/users`, {
      params,
      responseType: 'blob'
    });
  }

  exportBooks(keyword?: string, categoryId?: number): Observable<Blob> {
    let params = new HttpParams();
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (categoryId) {
      params = params.set('categoryId', categoryId.toString());
    }
    
    return this.http.get(`${environment.apiUrl}/admin/export/books`, {
      params,
      responseType: 'blob'
    });
  }

  exportBorrowRecords(keyword?: string, status?: string): Observable<Blob> {
    let params = new HttpParams();
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get(`${environment.apiUrl}/admin/export/borrow-records`, {
      params,
      responseType: 'blob'
    });
  }

  exportStatistics(startDate?: string, endDate?: string): Observable<Blob> {
    let params = new HttpParams();
    
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    
    return this.http.get(`${environment.apiUrl}/admin/export/statistics`, {
      params,
      responseType: 'blob'
    });
  }
}
