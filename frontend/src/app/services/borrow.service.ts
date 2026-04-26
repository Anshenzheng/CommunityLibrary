import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PageResponse } from './book.service';

export interface BorrowRecord {
  id: number;
  userId: number;
  username: string;
  userRealName: string;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;
  bookIsbn: string;
  borrowDate: string;
  dueDate: string;
  returnDate?: string;
  status: 'PENDING' | 'APPROVED' | 'BORROWED' | 'RETURNED' | 'REJECTED' | 'OVERDUE';
  adminId?: number;
  adminName?: string;
  rejectReason?: string;
  fineAmount: number;
  finePaid: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class BorrowService {

  constructor(private http: HttpClient) { }

  createBorrowRequest(bookId: number): Observable<any> {
    let params = new HttpParams().set('bookId', bookId.toString());
    return this.http.post(`${environment.apiUrl}/borrow/request`, null, { params });
  }

  getMyBorrows(status?: string, page: number = 0, size: number = 10): Observable<PageResponse<BorrowRecord>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<PageResponse<BorrowRecord>>(`${environment.apiUrl}/borrow/my`, { params });
  }

  returnBook(recordId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/borrow/return/${recordId}`, null);
  }

  getBorrowRecord(id: number): Observable<BorrowRecord> {
    return this.http.get<BorrowRecord>(`${environment.apiUrl}/borrow/${id}`);
  }
}
