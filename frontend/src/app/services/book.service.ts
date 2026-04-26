import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Book {
  id: number;
  isbn?: string;
  title: string;
  author?: string;
  publisher?: string;
  publishDate?: string;
  categoryId?: number;
  categoryName?: string;
  description?: string;
  coverImage?: string;
  totalQuantity: number;
  availableQuantity: number;
  location?: string;
  status: 'AVAILABLE' | 'BORROWED' | 'MAINTENANCE';
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class BookService {

  constructor(private http: HttpClient) { }

  searchBooks(
    keyword?: string,
    categoryId?: number,
    status?: string,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<Book>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    if (categoryId) {
      params = params.set('categoryId', categoryId.toString());
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<PageResponse<Book>>(`${environment.apiUrl}/books/search`, { params });
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${environment.apiUrl}/books/${id}`);
  }

  createBook(book: Partial<Book>): Observable<Book> {
    return this.http.post<Book>(`${environment.apiUrl}/books`, book);
  }

  updateBook(id: number, book: Partial<Book>): Observable<Book> {
    return this.http.put<Book>(`${environment.apiUrl}/books/${id}`, book);
  }

  deleteBook(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/books/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }

  createCategory(name: string, description?: string): Observable<Category> {
    return this.http.post<Category>(`${environment.apiUrl}/categories`, { name, description });
  }

  updateCategory(id: number, name: string, description?: string): Observable<Category> {
    return this.http.put<Category>(`${environment.apiUrl}/categories/${id}`, { name, description });
  }

  deleteCategory(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/categories/${id}`);
  }
}
