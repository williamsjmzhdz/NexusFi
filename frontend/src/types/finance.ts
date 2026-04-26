export type MovementType = 'ASSIGNMENT' | 'EXPENSE' | 'TRANSFER' | 'REBALANCE';

export interface Category {
  id: number;
  name: string;
  percentage: number;
  currentBalance: number;
  parentId: number | null;
  parentName: string | null;
  children: Category[];
}

export interface Movement {
  id: number;
  amount: number;
  type: MovementType;
  description: string | null;
  movementDate: string;
  categoryId: number;
  categoryName: string;
  createdAt: string;
  incomeRecordId: number | null;
  expenseRecordId: number | null;
  transferId: number | null;
}

export interface IncomeRecord {
  id: number;
  amount: number;
  source: string;
  description: string | null;
  incomeDate: string;
  createdAt: string;
}

export interface ExpenseRecord {
  id: number;
  amount: number;
  merchant: string;
  description: string | null;
  expenseDate: string;
  categoryId: number;
  categoryName: string;
  createdAt: string;
}
