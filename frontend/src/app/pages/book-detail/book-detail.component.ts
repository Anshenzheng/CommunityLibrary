import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService, Book } from '../../services/book.service';
import { BorrowService } from '../../services/borrow.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-book-detail',
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.css'
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  loading = true;
  borrowing = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private borrowService: BorrowService,
    private authService: AuthService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    const bookId = this.route.snapshot.paramMap.get('id');
    if (bookId) {
      this.loadBook(+bookId);
    }
  }

  loadBook(id: number): void {
    this.loading = true;
    
    this.bookService.getBookById(id).subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.book = data;
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载图书详情失败');
        this.loading = false;
      }
    });
  }

  borrowBook(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: this.router.url }
      });
      return;
    }
    
    if (!this.book) return;
    
    this.borrowing = true;
    
    this.borrowService.createBorrowRequest(this.book.id).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('借阅申请已提交，请等待管理员审核');
          this.router.navigate(['/my-borrows']);
        } else {
          this.toastService.showError(response.message || '借阅申请失败');
        }
        this.borrowing = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '借阅申请失败';
        this.toastService.showError(errorMsg);
        this.borrowing = false;
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

  canBorrow(): boolean {
    return this.book?.status === 'AVAILABLE' && (this.book?.availableQuantity || 0) > 0;
  }

  goBack(): void {
    this.router.navigate(['/books']);
  }
}
