import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionService } from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';
import { Transaction, TransactionHistoryResponse } from '../../models/transaction.model';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-gray-800">Transaction History</h2>
        <span class="text-sm text-gray-500">{{ totalElements() }} total records</span>
      </div>
      @if (loading()) {
        <div class="card flex items-center justify-center h-40">
          <p class="text-gray-400">Loading history...</p>
        </div>
      } @else if (errorMsg()) {
        <div class="card border border-red-200 bg-red-50">
          <p class="text-red-600 text-sm">{{ errorMsg() }}</p>
        </div>
      } @else {
        <div class="card p-0 overflow-hidden">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 border-b border-gray-200">
              <tr>
                <th class="text-left px-6 py-3 text-gray-500 font-medium">ID</th>
                <th class="text-left px-6 py-3 text-gray-500 font-medium">Type</th>
                <th class="text-right px-6 py-3 text-gray-500 font-medium">Amount</th>
                <th class="text-left px-6 py-3 text-gray-500 font-medium">Status</th>
                <th class="text-left px-6 py-3 text-gray-500 font-medium">Date</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              @for (tx of transactions(); track tx.transactionId) {
                <tr class="hover:bg-gray-50 transition-colors">
                  <td class="px-6 py-4 text-gray-500 font-mono text-xs">{{ tx.transactionId | slice:0:8 }}...</td>
                  <td class="px-6 py-4"><span [class]="typeBadge(tx.type)">{{ tx.type }}</span></td>
                  <td class="px-6 py-4 text-right font-semibold"
                      [class]="tx.type === 'DEPOSIT' ? 'text-primary' : 'text-danger'">
                    {{ tx.type === 'DEPOSIT' ? '+' : '-' }}{{ tx.amount | number:'1.2-2' }}
                    <span class="text-gray-400 font-normal text-xs">{{ tx.currency }}</span>
                  </td>
                  <td class="px-6 py-4"><span class="badge-success">{{ tx.status }}</span></td>
                  <td class="px-6 py-4 text-gray-500">{{ tx.createdAt | date:'dd MMM yyyy, HH:mm' }}</td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="px-6 py-12 text-center text-gray-400">No transactions found.</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        @if (totalPages() > 1) {
          <div class="flex items-center justify-between mt-4">
            <button (click)="changePage(currentPage() - 1)" [disabled]="currentPage() === 0"
              class="btn-outline text-sm disabled:opacity-40 disabled:cursor-not-allowed">← Previous</button>
            <span class="text-sm text-gray-500">Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
            <button (click)="changePage(currentPage() + 1)" [disabled]="currentPage() >= totalPages() - 1"
              class="btn-outline text-sm disabled:opacity-40 disabled:cursor-not-allowed">Next →</button>
          </div>
        }
      }
    </div>
  `,
})
export class HistoryComponent implements OnInit {
  private txService = inject(TransactionService);
  private auth      = inject(AuthService);

  transactions  = signal<Transaction[]>([]);
  loading       = signal(true);
  errorMsg      = signal('');
  currentPage   = signal(0);
  totalPages    = signal(0);
  totalElements = signal(0);

  ngOnInit() { this.loadHistory(); }

  loadHistory(page = 0) {
    this.loading.set(true);
    const accountId = this.getAccountIdFromToken();
    if (!accountId) {
      this.errorMsg.set('Could not determine account. Please log in again.');
      this.loading.set(false);
      return;
    }
    this.txService.getHistory(accountId, page).subscribe({
      next: (res: TransactionHistoryResponse) => {
        this.transactions.set(res.content);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.currentPage.set(res.number);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err.error?.detail ?? 'Failed to load history.');
        this.loading.set(false);
      },
    });
  }

  changePage(page: number) { this.loadHistory(page); }

  typeBadge(type: string): string {
    if (type === 'DEPOSIT')    return 'badge-success';
    if (type === 'WITHDRAWAL') return 'badge-danger';
    return 'badge-neutral';
  }

  private getAccountIdFromToken(): string | null {
    const token = this.auth.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.accountId ?? null;
    } catch { return null; }
  }
}
