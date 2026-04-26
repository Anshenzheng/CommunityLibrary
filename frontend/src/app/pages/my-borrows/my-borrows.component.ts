import { Component, OnInit } from '@angular/core';
import { BorrowService, BorrowRecord } from '../../services/borrow.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-my-borrows',
  templateUrl: './my-borrows.component.html',
  styleUrl: './my-borrows.component.css'
})
export class MyBorrowsComponent implements OnInit {
  records: BorrowRecord[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  selectedStatus: string = '';
  
  returnLoading: { [key: number]: boolean } = {};

  constructor(
    private borrowService: BorrowService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    
    this.borrowService.getMyBorrows(
      this.selectedStatus || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.records = data.content || [];
        this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
        this.currentPage = data.pageNumber || 0;
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载借阅记录失败');
        this.loading = false;
      }
    });
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadRecords();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadRecords();
  }

  returnBook(record: BorrowRecord): void {
    if (record.status !== 'BORROWED' && record.status !== 'OVERDUE') {
      return;
    }
    
    if (!confirm('确认要归还这本书吗？')) {
      return;
    }
    
    this.returnLoading[record.id] = true;
    
    this.borrowService.returnBook(record.id).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('还书成功！');
          this.loadRecords();
        } else {
          this.toastService.showError(response.message || '还书失败');
        }
        this.returnLoading[record.id] = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '还书失败';
        this.toastService.showError(errorMsg);
        this.returnLoading[record.id] = false;
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-warning';
      case 'APPROVED':
        return 'bg-info';
      case 'BORROWED':
        return 'bg-primary';
      case 'RETURNED':
        return 'bg-success';
      case 'REJECTED':
        return 'bg-danger';
      case 'OVERDUE':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'PENDING':
        return '待审核';
      case 'APPROVED':
        return '已批准';
      case 'BORROWED':
        return '借阅中';
      case 'RETURNED':
        return '已归还';
      case 'REJECTED':
        return '已拒绝';
      case 'OVERDUE':
        return '已逾期';
      default:
        return status;
    }
  }

  canReturn(status: string): boolean {
    return status === 'BORROWED' || status === 'OVERDUE';
  }

  isOverdue(status: string): boolean {
    return status === 'OVERDUE';
  }
}
