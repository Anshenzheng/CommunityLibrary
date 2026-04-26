import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { AuthService, User } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-users',
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  loading = false;
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;
  searchKeyword = '';
  selectedRole: string = '';
  selectedStatus: string = '';
  
  loadingMap: { [key: number]: boolean } = {};

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    
    this.adminService.getUsers(
      this.searchKeyword || undefined,
      this.selectedRole || undefined,
      this.selectedStatus || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.users = data.content || [];
        this.totalElements = data.totalElements || 0;
        this.totalPages = data.totalPages || 0;
        this.currentPage = data.pageNumber || 0;
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载用户列表失败');
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  updateStatus(user: User, status: string): void {
    if (!confirm(`确认将用户「${user.realName}」的状态更新为「${status === 'ACTIVE' ? '正常' : '禁用'}」吗？`)) {
      return;
    }
    
    this.loadingMap[user.id] = true;
    
    this.adminService.updateUserStatus(user.id, status).subscribe({
      next: (response: any) => {
        if (response.success !== false) {
          this.toastService.showSuccess('用户状态更新成功');
          this.loadUsers();
        } else {
          this.toastService.showError(response.message || '操作失败');
        }
        this.loadingMap[user.id] = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '操作失败';
        this.toastService.showError(errorMsg);
        this.loadingMap[user.id] = false;
      }
    });
  }

  updateRole(user: User, role: string): void {
    if (!confirm(`确认将用户「${user.realName}」的角色更新为「${role === 'ADMIN' ? '管理员' : '普通读者'}」吗？`)) {
      return;
    }
    
    this.loadingMap[user.id] = true;
    
    this.adminService.updateUserRole(user.id, role).subscribe({
      next: (response: any) => {
        if (response.success !== false) {
          this.toastService.showSuccess('用户角色更新成功');
          this.loadUsers();
        } else {
          this.toastService.showError(response.message || '操作失败');
        }
        this.loadingMap[user.id] = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '操作失败';
        this.toastService.showError(errorMsg);
        this.loadingMap[user.id] = false;
      }
    });
  }

  exportUsers(): void {
    this.adminService.exportUsers(
      this.searchKeyword || undefined,
      this.selectedRole || undefined,
      this.selectedStatus || undefined
    ).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `用户列表_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.showSuccess('导出成功');
      },
      error: (error) => {
        this.toastService.showError('导出失败');
      }
    });
  }

  getRoleText(role: string): string {
    return role === 'ADMIN' ? '管理员' : '普通读者';
  }

  getStatusText(status: string): string {
    return status === 'ACTIVE' ? '正常' : '禁用';
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'bg-primary' : 'bg-success';
  }

  getStatusBadgeClass(status: string): string {
    return status === 'ACTIVE' ? 'bg-success' : 'bg-danger';
  }

  isCurrentUser(userId: number): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === userId;
  }
}
