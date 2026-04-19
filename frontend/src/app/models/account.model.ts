export interface Account {
  id: string;
  accountNumber: string;
  accountType: 'SAVINGS' | 'CHECKING';
  status: 'ACTIVE' | 'INACTIVE' | 'FROZEN';
  balance: number;
  currency: string;
  createdAt: string;
}

export interface BalanceResponse {
  accountId: string;
  balance: number;
  currency: string;
}
