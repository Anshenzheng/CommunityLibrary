import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');
  
  if (password && confirmPassword && password.value !== confirmPassword.value) {
    return { passwordMismatch: true };
  }
  
  return null;
};

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  submitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
    }
    
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      confirmPassword: ['', Validators.required],
      realName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.email]],
      phone: ['', []]
    }, {
      validators: passwordMatchValidator
    });
  }

  get f() { return this.registerForm.controls; }

  onSubmit(): void {
    this.submitted = true;
    
    if (this.registerForm.invalid) {
      return;
    }
    
    this.loading = true;
    
    this.authService.register({
      username: this.f['username'].value,
      password: this.f['password'].value,
      realName: this.f['realName'].value,
      email: this.f['email'].value || undefined,
      phone: this.f['phone'].value || undefined
    }).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('注册成功！请登录您的账户');
          this.router.navigate(['/login']);
        } else {
          this.toastService.showError(response.message || '注册失败');
          this.loading = false;
        }
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '注册失败，请稍后重试';
        this.toastService.showError(errorMsg);
        this.loading = false;
      }
    });
  }
}
