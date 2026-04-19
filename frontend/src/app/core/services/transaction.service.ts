import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  Transaction,
  TransactionHistoryResponse,
  TransactionRequest,
  TransferRequest,
} from '../../models/transaction.model';
import { v4 as uuidv4 } from 'uuid';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly apiUrl = '/api/transactions';

  constructor(private http: HttpClient) {}

  deposit(accountId: string, amount: number, currency: string, description?: string) {
    const body: TransactionRequest = {
      accountId,
      amount,
      currency,
      description,
      idempotencyKey: uuidv4(),
    };
    return this.http.post<Transaction>(`${this.apiUrl}/deposit`, body);
  }

  withdraw(accountId: string, amount: number, currency: string, description?: string) {
    const body: TransactionRequest = {
      accountId,
      amount,
      currency,
      description,
      idempotencyKey: uuidv4(),
    };
    return this.http.post<Transaction>(`${this.apiUrl}/withdraw`, body);
  }

  transfer(sourceAccountId: string, targetAccountId: string, amount: number, currency: string, description?: string) {
    const body: TransferRequest = {
      sourceAccountId,
      targetAccountId,
      amount,
      currency,
      description,
      idempotencyKey: uuidv4(),
    };
    return this.http.post<Transaction>(`${this.apiUrl}/transfer`, body);
  }

  getHistory(accountId: string, page = 0, size = 10) {
    const params = new HttpParams()
      .set('accountId', accountId)
      .set('page', page)
      .set('size', size);
    return this.http.get<TransactionHistoryResponse>(`${this.apiUrl}/history`, { params });
  }
}
