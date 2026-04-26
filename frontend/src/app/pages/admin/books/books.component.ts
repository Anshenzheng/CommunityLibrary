import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { BookService, Book, Category, PageResponse } from '../../../services/book.service';
import { ToastService } from '../../../services/toast.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-admin-books',
  templateUrl: './books.component.html',
  styleUrl: './books.component.css'
})
export class AdminBooksComponent implements OnInit {
  books: Book[] = [];
  categories: Category[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  searchKeyword = '';
  selectedCategory: number | null = null;
  
  bookForm: FormGroup;
  showModal = false;
  editingBook: Book | null = null;
  submitting = false;

  constructor(
    private bookService: BookService,
    private adminService: AdminService,
    private toastService: ToastService,
    private formBuilder: FormBuilder
  ) {
    this.bookForm = this.formBuilder.group({
      title: ['', Validators.required],
      author: [''],
      publisher: [''],
      isbn: [''],
      categoryId: [null],
      totalQuantity: [1, [Validators.required, Validators.min(1)]],
      availableQuantity: [1, [Validators.required, Validators.min(0)]],
      location: [''],
      description: [''],
      status: ['AVAILABLE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadBooks();
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

  loadBooks(): void {
    this.loading = true;
    
    this.bookService.searchBooks(
      this.searchKeyword || undefined,
      this.selectedCategory || undefined,
      undefined,
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
    this.loadBooks();
  }

  onCategoryChange(): void {
    this.currentPage = 0;
    this.loadBooks();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadBooks();
  }

  openAddModal(): void {
    this.editingBook = null;
    this.bookForm.reset({
      title: '',
      author: '',
      publisher: '',
      isbn: '',
      categoryId: null,
      totalQuantity: 1,
      availableQuantity: 1,
      location: '',
      description: '',
      status: 'AVAILABLE'
    });
    this.showModal = true;
  }

  openEditModal(book: Book): void {
    this.editingBook = book;
    this.bookForm.patchValue({
      title: book.title,
      author: book.author || '',
      publisher: book.publisher || '',
      isbn: book.isbn || '',
      categoryId: book.categoryId || null,
      totalQuantity: book.totalQuantity,
      availableQuantity: book.availableQuantity,
      location: book.location || '',
      description: book.description || '',
      status: book.status
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingBook = null;
  }

  submitBook(): void {
    if (this.bookForm.invalid) {
      this.toastService.showWarning('请填写必填项');
      return;
    }
    
    this.submitting = true;
    
    const bookData = this.bookForm.value;
    
    if (this.editingBook) {
      this.bookService.updateBook(this.editingBook.id, bookData).subscribe({
        next: (response: any) => {
          this.toastService.showSuccess('图书更新成功');
          this.closeModal();
          this.loadBooks();
          this.submitting = false;
        },
        error: (error) => {
          const errorMsg = error.error?.message || error.message || '更新失败';
          this.toastService.showError(errorMsg);
          this.submitting = false;
        }
      });
    } else {
      this.bookService.createBook(bookData).subscribe({
        next: (response: any) => {
          this.toastService.showSuccess('图书创建成功');
          this.closeModal();
          this.loadBooks();
          this.submitting = false;
        },
        error: (error) => {
          const errorMsg = error.error?.message || error.message || '创建失败';
          this.toastService.showError(errorMsg);
          this.submitting = false;
        }
      });
    }
  }

  deleteBook(book: Book): void {
    if (!confirm(`确认删除图书「${book.title}」吗？此操作不可恢复。`)) {
      return;
    }
    
    this.bookService.deleteBook(book.id).subscribe({
      next: (response: any) => {
        if (response.success !== false) {
          this.toastService.showSuccess('图书删除成功');
          this.loadBooks();
        } else {
          this.toastService.showError(response.message || '删除失败');
        }
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '删除失败';
        this.toastService.showError(errorMsg);
      }
    });
  }

  exportBooks(): void {
    this.adminService.exportBooks(
      this.searchKeyword || undefined,
      this.selectedCategory || undefined
    ).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `图书列表_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.showSuccess('导出成功');
      },
      error: (error) => {
        this.toastService.showError('导出失败');
      }
    });
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
