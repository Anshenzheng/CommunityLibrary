import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  @Input() pageNumber: number = 0;
  @Input() pageSize: number = 10;
  @Input() totalElements: number = 0;
  @Input() totalPages: number = 0;
  @Input() first: boolean = true;
  @Input() last: boolean = true;
  
  @Output() pageChange = new EventEmitter<number>();

  get pages(): (number | string)[] {
    const result: (number | string)[] = [];
    const currentPage = this.pageNumber + 1;
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 1; i <= this.totalPages; i++) {
        result.push(i);
      }
    } else {
      let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
      let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);
      
      if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
      }
      
      if (startPage > 1) {
        result.push(1);
        if (startPage > 2) {
          result.push('...');
        }
      }
      
      for (let i = startPage; i <= endPage; i++) {
        result.push(i);
      }
      
      if (endPage < this.totalPages) {
        if (endPage < this.totalPages - 1) {
          result.push('...');
        }
        result.push(this.totalPages);
      }
    }
    
    return result;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.pageNumber + 1) {
      this.pageChange.emit(page - 1);
    }
  }

  goToPrevious(): void {
    if (!this.first) {
      this.pageChange.emit(this.pageNumber - 1);
    }
  }

  goToNext(): void {
    if (!this.last) {
      this.pageChange.emit(this.pageNumber + 1);
    }
  }

  get currentPageDisplay(): number {
    return this.pageNumber + 1;
  }

  get startItem(): number {
    return this.totalElements > 0 ? this.pageNumber * this.pageSize + 1 : 0;
  }

  get endItem(): number {
    return Math.min((this.pageNumber + 1) * this.pageSize, this.totalElements);
  }
}
