import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-100 flex items-center justify-center px-4">
      <div class="w-full max-w-md">

        <!-- Header -->
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 bg-primary rounded-full mb-4">
            <span class="text-white font-bold text-2xl">B</span>
          </div>
          <h1 class="text-2xl font-bold text-gray-800">Create Account</h1>
          <p class="text-gray-500 text-sm mt-1">Start banking today</p>
        </div>

        <div class="card">
          <form [formGroup]="form" (ngSubmit)="onSubmit()">

            <div class="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label class="form-label">First Name</label>
                <input type="text" formControlName="firstName" class="form-input" placeholder="John" />
                @if (form.get('firstName')?.invalid && form.get('firstName')?.touched) {
                  <p class="text-danger text-xs mt-1">Required.</p>
                }
              </div>
              <div>
                <label class="form-label">Last Name</label>
                <input type="text" formControlName="lastName" class="form-input" placeholder="Doe" />
                @if (form.get('lastName')?.invalid && form.get('lastName')?.touched) {
                  <p class="text-danger text-xs mt-1">Required.</p>
                }
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label">Email</label>
              <input type="email" formControlName="email" class="form-input" placeholder="you@example.com" />
              @if (form.get('email')?.invalid && form.get('email')?.touched) {
                <p class="text-danger text-xs mt-1">Valid email required.</p>
              }
            </div>

            <div class="mb-6">
              <label class="form-label">Password</label>
              <input type="password" formControlName="password" class="form-input" placeholder="Min 8 characters" />
              @if (form.get('password')?.invalid && form.get('password')?.touched) {
                <p class="text-danger text-xs mt-1">Min 8 characters required.</p>
              }
            </div>

            @if (errorMsg) {
              <div class="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
                {{ errorMsg }}
              </div>
            }

            <button type="submit" [disabled]="form.invalid || loading" class="btn-primary w-full justify-center">
              {{ loading ? 'Creating account...' : 'Create Account' }}
            </button>
          </form>

          <p class="mt-4 text-center text-sm text-gray-500">
            Already have an account?
            <a routerLink="/login" class="text-primary hover:underline font-medium">Sign in</a>
          </p>
        </div>

      </div>
    </div>
  `,
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  errorMsg = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName:  ['', Validators.required],
      email:     ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.loading = true;
    this.errorMsg = '';

    this.auth.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.errorMsg = err.error?.detail ?? 'Registration failed. Please try again.';
        this.loading = false;
      },
    });
  }
}
