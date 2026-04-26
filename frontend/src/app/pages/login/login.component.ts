import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string = '/';

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
    }
    
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    
    if (this.loginForm.invalid) {
      return;
    }
    
    this.loading = true;
    
    this.authService.login({
      username: this.f['username'].value,
      password: this.f['password'].value
    }).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('登录成功，欢迎回来！');
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.toastService.showError(response.message || '登录失败');
          this.loading = false;
        }
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '登录失败，请检查用户名和密码';
        this.toastService.showError(errorMsg);
        this.loading = false;
      }
    });
  }
}
