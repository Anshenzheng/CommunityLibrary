import { Component, OnInit } from '@angular/core';
import { AdminService, DashboardStats } from '../../../services/admin.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    
    this.adminService.getDashboardStats().subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.stats = data;
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载统计数据失败');
        this.loading = false;
      }
    });
  }

  getStatIcon(key: string): string {
    const icons: { [key: string]: string } = {
      totalUsers: '👥',
      totalReaders: '👤',
      totalBooks: '📚',
      availableBooks: '📖',
      borrowedBooks: '📦',
      pendingRequests: '⏳',
      overdueCount: '⚠️',
      weeklyBorrows: '📈',
      weeklyReturns: '📉'
    };
    return icons[key] || '📊';
  }

  getStatLabel(key: string): string {
    const labels: { [key: string]: string } = {
      totalUsers: '总用户数',
      totalReaders: '读者数量',
      totalBooks: '总图书数',
      availableBooks: '可借图书',
      borrowedBooks: '借出图书',
      pendingRequests: '待审核申请',
      overdueCount: '逾期记录',
      weeklyBorrows: '本周借阅',
      weeklyReturns: '本周归还'
    };
    return labels[key] || key;
  }

  getStatColor(key: string): string {
    const colors: { [key: string]: string } = {
      overdueCount: 'var(--danger-color)',
      pendingRequests: 'var(--warning-color)'
    };
    return colors[key] || 'var(--primary-color)';
  }
}
