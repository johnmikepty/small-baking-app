export interface TransactionRequest {
  accountId: string;
  amount: number;
  currency: string;
  description?: string;
  idempotencyKey: string;
}

export interface TransferRequest {
  sourceAccountId: string;
  targetAccountId: string;
  amount: number;
  currency: string;
  description?: string;
  idempotencyKey: string;
}

export interface Transaction {
  transactionId: string;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  amount: number;
  currency: string;
  status: 'COMPLETED' | 'PENDING' | 'FAILED';
  createdAt: string;
}

export interface TransactionHistoryResponse {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
