import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Account, BalanceResponse } from '../../models/account.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly apiUrl = '/api/accounts';

  constructor(private http: HttpClient) {}

  getAccount(id: string) {
    return this.http.get<Account>(`${this.apiUrl}/${id}`);
  }

  getBalance(id: string) {
    return this.http.get<BalanceResponse>(`${this.apiUrl}/${id}/balance`);
  }
}
