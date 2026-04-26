import { Component, OnInit } from '@angular/core';
import { AdminService, Statistics } from '../../../services/admin.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-admin-statistics',
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css'
})
export class AdminStatisticsComponent implements OnInit {
  statistics: Statistics[] = [];
  loading = false;
  startDate: string = '';
  endDate: string = '';

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) { }

  ngOnInit(): void {
    const today = new Date();
    const last30Days = new Date(today);
    last30Days.setDate(today.getDate() - 30);
    
    this.startDate = last30Days.toISOString().split('T')[0];
    this.endDate = today.toISOString().split('T')[0];
    
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    
    this.adminService.getStatistics(
      this.startDate || undefined,
      this.endDate || undefined
    ).subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.statistics = data || [];
        this.loading = false;
      },
      error: (error) => {
        this.toastService.showError('加载统计数据失败');
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.loadStatistics();
  }

  exportStatistics(): void {
    this.adminService.exportStatistics(
      this.startDate || undefined,
      this.endDate || undefined
    ).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `统计数据_${new Date().toISOString().split('T')[0]}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toastService.showSuccess('导出成功');
      },
      error: (error) => {
        this.toastService.showError('导出失败');
      }
    });
  }

  getTotalUsers(): number {
    return this.statistics.reduce((sum, s) => sum + (s.totalUsers || 0), 0);
  }

  getTotalBooks(): number {
    return this.statistics.reduce((sum, s) => sum + (s.totalBooks || 0), 0);
  }

  getTotalBorrowed(): number {
    return this.statistics.reduce((sum, s) => sum + (s.totalBorrowed || 0), 0);
  }

  getTotalReturned(): number {
    return this.statistics.reduce((sum, s) => sum + (s.totalReturned || 0), 0);
  }

  getTotalOverdue(): number {
    return this.statistics.reduce((sum, s) => sum + (s.totalOverdue || 0), 0);
  }
}
