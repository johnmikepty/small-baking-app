import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TransactionService } from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';
import { Transaction } from '../../models/transaction.model';

type TxType = 'deposit' | 'withdraw' | 'transfer';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  template: `
    <div>
      <h2 class="text-2xl font-bold text-gray-800 mb-6">Transactions</h2>

      <!-- Tab selector -->
      <div class="flex gap-2 mb-6">
        @for (tab of tabs; track tab.value) {
          <button
            (click)="activeTab.set(tab.value)"
            [class]="activeTab() === tab.value ? 'btn-primary' : 'btn-outline'"
            class="text-sm px-4 py-2 rounded font-medium transition-colors duration-200">
            {{ tab.label }}
          </button>
        }
      </div>

      <!-- Form Card -->
      <div class="card max-w-lg">
        <form [formGroup]="form" (ngSubmit)="onSubmit()">

          <!-- Transfer only: source account -->
          @if (activeTab() === 'transfer') {
            <div class="mb-4">
              <label class="form-label">From Account ID</label>
              <input type="text" formControlName="sourceAccountId" class="form-input" placeholder="UUID" />
            </div>
            <div class="mb-4">
              <label class="form-label">To Account ID</label>
              <input type="text" formControlName="targetAccountId" class="form-input" placeholder="UUID" />
            </div>
          } @else {
            <div class="mb-4">
              <label class="form-label">Account ID</label>
              <input type="text" formControlName="accountId" class="form-input" placeholder="UUID" />
            </div>
          }

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="form-label">Amount</label>
              <input type="number" formControlName="amount" class="form-input" placeholder="0.00" min="0.01" step="0.01" />
            </div>
            <div>
              <label class="form-label">Currency</label>
              <select formControlName="currency" class="form-input">
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
              </select>
            </div>
          </div>

          <div class="mb-6">
            <label class="form-label">Description (optional)</label>
            <input type="text" formControlName="description" class="form-input" placeholder="e.g. Rent payment" />
          </div>

          @if (errorMsg()) {
            <div class="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
              {{ errorMsg() }}
            </div>
          }

          @if (successTx()) {
            <div class="mb-4 px-4 py-3 bg-green-50 border border-green-200 rounded text-green-700 text-sm">
              ✅ Transaction <strong>{{ successTx()!.transactionId | slice:0:8 }}...</strong>
              completed — {{ successTx()!.amount | number:'1.2-2' }} {{ successTx()!.currency }}
            </div>
          }

          <button type="submit" [disabled]="form.invalid || loading()" class="btn-primary w-full justify-center">
            {{ loading() ? 'Processing...' : 'Submit' }}
          </button>
        </form>
      </div>
    </div>
  `,
})
export class TransactionsComponent implements OnInit {
  form!: FormGroup;
  activeTab  = signal<TxType>('deposit');
  loading    = signal(false);
  errorMsg   = signal('');
  successTx  = signal<Transaction | null>(null);

  tabs = [
    { label: '⬆️ Deposit',  value: 'deposit'  as TxType },
    { label: '⬇️ Withdraw', value: 'withdraw' as TxType },
    { label: '↔️ Transfer', value: 'transfer' as TxType },
  ];

  constructor(
    private fb: FormBuilder,
    private txService: TransactionService,
    public auth: AuthService
  ) {}

  ngOnInit() {
    this.buildForm();
  }

  buildForm() {
    this.form = this.fb.group({
      accountId:       [''],
      sourceAccountId: [''],
      targetAccountId: [''],
      amount:          [null, [Validators.required, Validators.min(0.01)]],
      currency:        ['USD', Validators.required],
      description:     [''],
    });
  }

  onSubmit() {
    this.loading.set(true);
    this.errorMsg.set('');
    this.successTx.set(null);

    const { accountId, sourceAccountId, targetAccountId, amount, currency, description } = this.form.value;
    const tab = this.activeTab();

    const call$ = tab === 'deposit'
      ? this.txService.deposit(accountId, amount, currency, description)
      : tab === 'withdraw'
        ? this.txService.withdraw(accountId, amount, currency, description)
        : this.txService.transfer(sourceAccountId, targetAccountId, amount, currency, description);

    call$.subscribe({
      next: (tx) => {
        this.successTx.set(tx);
        this.loading.set(false);
        this.form.reset({ currency: 'USD' });
      },
      error: (err) => {
        this.errorMsg.set(err.error?.detail ?? 'Transaction failed.');
        this.loading.set(false);
      },
    });
  }
}
