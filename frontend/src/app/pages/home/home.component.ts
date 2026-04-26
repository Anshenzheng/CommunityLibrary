import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BookService, Book, Category } from '../../services/book.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  featuredBooks: Book[] = [];
  categories: Category[] = [];
  loading = true;
  searchKeyword = '';

  constructor(
    private bookService: BookService,
    public authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadFeaturedBooks();
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

  loadFeaturedBooks(): void {
    this.bookService.searchBooks(undefined, undefined, 'AVAILABLE', 0, 8).subscribe({
      next: (response) => {
        this.featuredBooks = response.content;
        this.loading = false;
      },
      error: (error) => {
        console.error('加载推荐图书失败:', error);
        this.loading = false;
      }
    });
  }

  search(): void {
    if (this.searchKeyword.trim()) {
      this.router.navigate(['/books'], {
        queryParams: { keyword: this.searchKeyword }
      });
    }
  }

  searchByCategory(categoryId: number): void {
    this.router.navigate(['/books'], {
      queryParams: { category: categoryId }
    });
  }
  
  handleCategoryClick(categoryId: number, event: Event): void {
    event.preventDefault();
    this.searchByCategory(categoryId);
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
