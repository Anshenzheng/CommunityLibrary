import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookService, Book, Category, PageResponse } from '../../services/book.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-book-list',
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css'
})
export class BookListComponent implements OnInit {
  books: Book[] = [];
  categories: Category[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 12;
  
  searchKeyword = '';
  selectedCategory: number | null = null;
  selectedStatus: string = '';

  constructor(
    private bookService: BookService,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    
    this.route.queryParams.subscribe(params => {
      if (params['keyword']) {
        this.searchKeyword = params['keyword'];
      }
      if (params['category']) {
        this.selectedCategory = +params['category'];
      }
      this.searchBooks();
    });
  }

  loadCategories(): void {
    this.bookService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('加载分类失败:', error);
      }
    });
  }

  searchBooks(): void {
    this.loading = true;
    
    this.bookService.searchBooks(
      this.searchKeyword || undefined,
      this.selectedCategory || undefined,
      this.selectedStatus || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.books = data.content || [];
        this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
        this.currentPage = data.pageNumber || 0;
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载图书列表失败');
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.searchBooks();
  }

  onCategoryChange(): void {
    this.currentPage = 0;
    this.searchBooks();
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.searchBooks();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.searchBooks();
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedCategory = null;
    this.selectedStatus = '';
    this.currentPage = 0;
    this.searchBooks();
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-success';
      case 'BORROWED':
        return 'bg-warning';
      case 'MAINTENANCE':
        return 'bg-secondary';
      default:
        return 'bg-info';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'AVAILABLE':
        return '可借';
      case 'BORROWED':
        return '已借出';
      case 'MAINTENANCE':
        return '维护中';
      default:
        return status;
    }
  }
}
