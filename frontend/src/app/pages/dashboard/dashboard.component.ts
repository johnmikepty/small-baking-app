import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AccountService } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div>
      <!-- Page title -->
      <div class="mb-6">
        <h2 class="text-2xl font-bold text-gray-800">
          Welcome back, {{ auth.currentUser()?.firstName }}
        </h2>
        <p class="text-gray-500 text-sm mt-1">Here's your account overview</p>
      </div>

      <!-- Balance Card -->
      @if (account()) {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">

          <div class="card border-l-4 border-primary">
            <p class="text-sm text-gray-500 mb-1">Available Balance</p>
            <p class="text-3xl font-bold text-gray-800">
              {{ account()!.balance | number:'1.2-2' }}
              <span class="text-lg text-gray-400 font-normal">{{ account()!.currency }}</span>
            </p>
            <p class="text-xs text-gray-400 mt-2">Account #{{ account()!.accountNumber }}</p>
          </div>

          <div class="card border-l-4 border-blue-400">
            <p class="text-sm text-gray-500 mb-1">Account Type</p>
            <p class="text-2xl font-bold text-gray-800">{{ account()!.accountType }}</p>
            <span class="inline-block mt-2 px-2 py-0.5 rounded text-xs font-medium"
                  [class]="account()!.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'">
              {{ account()!.status }}
            </span>
          </div>

          <div class="card border-l-4 border-yellow-400">
            <p class="text-sm text-gray-500 mb-1">Member Since</p>
            <p class="text-2xl font-bold text-gray-800">
              {{ account()!.createdAt | date:'MMM yyyy' }}
            </p>
            <p class="text-xs text-gray-400 mt-2">Account ID: {{ account()!.id | slice:0:8 }}...</p>
          </div>

        </div>

        <!-- Quick Actions -->
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-700 mb-4">Quick Actions</h3>
          <div class="flex flex-wrap gap-3">
            <a routerLink="/transactions" class="btn-primary">💸 Make a Transaction</a>
            <a routerLink="/history"      class="btn-outline">📋 View History</a>
          </div>
        </div>

      } @else if (loading()) {
        <div class="card flex items-center justify-center h-40">
          <p class="text-gray-400">Loading account data...</p>
        </div>
      } @else if (errorMsg()) {
        <div class="card border border-red-200 bg-red-50">
          <p class="text-red-600 text-sm">{{ errorMsg() }}</p>
        </div>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  account  = signal<Account | null>(null);
  loading  = signal(true);
  errorMsg = signal('');

  constructor(
    public auth: AuthService,
    private accountService: AccountService
  ) {}

  ngOnInit() {
    // The account ID comes from the JWT — the backend ties it to the logged-in user.
    // For this demo we rely on the backend returning the account linked to the token.
    // A real app would decode the JWT or call /api/users/me first.
    const accountId = this.getAccountIdFromToken();
    if (!accountId) {
      this.errorMsg.set('Could not determine account ID. Please log in again.');
      this.loading.set(false);
      return;
    }

    this.accountService.getAccount(accountId).subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.detail ?? 'Failed to load account.');
        this.loading.set(false);
      },
    });
  }

  /** Decode accountId stored in JWT claims. */
  private getAccountIdFromToken(): string | null {
    const token = this.auth.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.accountId ?? null;
    } catch {
      return null;
    }
  }
}
