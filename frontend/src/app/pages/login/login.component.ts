import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
      <div class="w-full max-w-md">
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 bg-primary rounded-full mb-4">
            <span class="text-white font-bold text-2xl">B</span>
          </div>
          <h1 class="text-2xl font-bold text-gray-800">SmallBank</h1>
          <p class="text-gray-500 text-sm mt-1">Sign in to your account</p>
        </div>
        <div class="card">
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <div class="mb-4">
              <label class="form-label">Email</label>
              <input type="email" formControlName="email" class="form-input" placeholder="you@example.com" />
              @if (form.get('email')?.invalid && form.get('email')?.touched) {
                <p class="text-danger text-xs mt-1">Valid email is required.</p>
              }
            </div>
            <div class="mb-6">
              <label class="form-label">Password</label>
              <input type="password" formControlName="password" class="form-input" placeholder="••••••••" />
              @if (form.get('password')?.invalid && form.get('password')?.touched) {
                <p class="text-danger text-xs mt-1">Password is required.</p>
              }
            </div>
            @if (errorMsg) {
              <div class="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
                {{ errorMsg }}
              </div>
            }
            <button type="submit" [disabled]="form.invalid || loading" class="btn-primary w-full justify-center">
              {{ loading ? 'Signing in...' : 'Sign In' }}
            </button>
          </form>
          <p class="mt-4 text-center text-sm text-gray-500">
            Don't have an account?
            <a routerLink="/register" class="text-primary hover:underline font-medium">Register</a>
          </p>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  loading  = false;
  errorMsg = '';

  onSubmit() {
    if (this.form.invalid) return;
    this.loading  = true;
    this.errorMsg = '';
    this.auth.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.errorMsg = err.error?.detail ?? 'Invalid credentials. Please try again.';
        this.loading  = false;
      },
    });
  }
}
