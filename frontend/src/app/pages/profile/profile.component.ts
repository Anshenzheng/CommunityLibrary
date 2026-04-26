import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  currentUser: any = null;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  loading = false;
  passwordLoading = false;
  submitted = false;
  passwordSubmitted = false;

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private toastService: ToastService
  ) {
    this.currentUser = this.authService.getCurrentUser();
    
    this.profileForm = this.formBuilder.group({
      realName: ['', Validators.required],
      email: ['', [Validators.email]],
      phone: ['', []]
    });
    
    this.passwordForm = this.formBuilder.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.authService.getCurrentUserProfile().subscribe({
      next: (response: any) => {
        const data = response.data || response;
        this.currentUser = data;
        this.profileForm.patchValue({
          realName: data.realName || '',
          email: data.email || '',
          phone: data.phone || ''
        });
      },
      error: (error) => {
        console.error('加载用户信息失败:', error);
      }
    });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { passwordMismatch: true };
    }
    
    return null;
  }

  get pf() { return this.profileForm.controls; }
  get pwf() { return this.passwordForm.controls; }

  updateProfile(): void {
    this.submitted = true;
    
    if (this.profileForm.invalid) {
      return;
    }
    
    this.loading = true;
    
    this.authService.updateProfile({
      realName: this.pf['realName'].value,
      email: this.pf['email'].value || undefined,
      phone: this.pf['phone'].value || undefined
    }).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('个人资料更新成功');
          
          if (this.currentUser) {
            this.currentUser.realName = this.pf['realName'].value;
            this.authService.setCurrentUser(this.currentUser);
          }
        } else {
          this.toastService.showError(response.message || '更新失败');
        }
        this.loading = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '更新失败';
        this.toastService.showError(errorMsg);
        this.loading = false;
      }
    });
  }

  changePassword(): void {
    this.passwordSubmitted = true;
    
    if (this.passwordForm.invalid) {
      return;
    }
    
    this.passwordLoading = true;
    
    this.authService.changePassword(
      this.pwf['oldPassword'].value,
      this.pwf['newPassword'].value
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.toastService.showSuccess('密码修改成功');
          this.passwordForm.reset();
          this.passwordSubmitted = false;
        } else {
          this.toastService.showError(response.message || '密码修改失败');
        }
        this.passwordLoading = false;
      },
      error: (error) => {
        const errorMsg = error.error?.message || error.message || '密码修改失败';
        this.toastService.showError(errorMsg);
        this.passwordLoading = false;
      }
    });
  }

  getRoleText(role: string): string {
    return role === 'ADMIN' ? '管理员' : '普通读者';
  }

  getStatusText(status: string): string {
    return status === 'ACTIVE' ? '正常' : '禁用';
  }
}
