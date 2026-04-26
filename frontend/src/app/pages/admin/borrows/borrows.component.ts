import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { BorrowRecord } from '../../../services/borrow.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-borrows',
  templateUrl: './borrows.component.html',
  styleUrl: './borrows.component.css'
})
export class AdminBorrowsComponent implements OnInit {
  records: BorrowRecord[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  selectedStatus: string = '';
  searchKeyword = '';
  
  rejectModal = {
    show: false,
    recordId: 0,
    reason: ''
  };
  
  loadingMap: { [key: number]: boolean } = {};

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    
    this.adminService.getBorrowRecords(
      this.searchKeyword || undefined,
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

  onSearch(): void {
    this.currentPage = 0;
    this.loadRecords();
  }

  onStatusChange(): void {
    this.currentPage = 0;
    this.loadRecords();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadRecords();
  }

  approveRecord(record: BorrowRecord): void {
    if (!confirm('确认批准此借阅申请吗？')) {
      return;
    }
    
    this.loadingMap[record.id] = true;
    
    this.adminService.approveBorrow(record.id).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('借阅申请已批准');
          this.loadRecords();
        } else {
          this.toastService.showError(response.message || '操作失败');
        }
        this.loadingMap[record.id] = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '操作失败';
        this.toastService.showError(errorMsg);
        this.loadingMap[record.id] = false;
      }
    });
  }

  openRejectModal(record: BorrowRecord): void {
    this.rejectModal = {
      show: true,
      recordId: record.id,
      reason: ''
    };
  }

  closeRejectModal(): void {
    this.rejectModal.show = false;
  }

  confirmReject(): void {
    if (!this.rejectModal.reason.trim()) {
      this.toastService.showWarning('请输入拒绝原因');
      return;
    }
    
    this.loadingMap[this.rejectModal.recordId] = true;
    
    this.adminService.rejectBorrow(this.rejectModal.recordId, this.rejectModal.reason).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('借阅申请已拒绝');
          this.closeRejectModal();
          this.loadRecords();
        } else {
          this.toastService.showError(response.message || '操作失败');
        }
        this.loadingMap[this.rejectModal.recordId] = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '操作失败';
        this.toastService.showError(errorMsg);
        this.loadingMap[this.rejectModal.recordId] = false;
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

  isPending(status: string): boolean {
    return status === 'PENDING';
  }
  
  isOverdue(status: string): boolean {
    return status === 'OVERDUE';
  }

  exportRecords(): void {
    this.adminService.exportBorrowRecords(
      this.searchKeyword || undefined,
      this.selectedStatus || undefined
    ).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `借阅记录_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.showSuccess('导出成功');
      },
      error: (error) => {
        this.toastService.showError('导出失败');
      }
    });
  }
}
