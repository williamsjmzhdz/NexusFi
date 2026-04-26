import api from './api';
import type { IncomeRecord, ExpenseRecord, Movement, Category } from '../types/finance';

export async function getCategories(): Promise<Category[]> {
  const response = await api.get<Category[]>('/categories/tree');
  return response.data;
}

export async function createCategory(data: {
  name: string;
  percentage: number;
  parentId?: number | null;
}): Promise<Category> {
  const response = await api.post<Category>('/categories', data);
  return response.data;
}

export async function deleteCategory(id: number): Promise<void> {
  await api.delete(`/categories/${id}`);
}

export async function getIncomes(): Promise<IncomeRecord[]> {
  const response = await api.get<IncomeRecord[]>('/incomes');
  return response.data;
}

export async function recordIncome(data: {
  amount: number;
  source: string;
  description?: string;
  incomeDate: string;
}): Promise<IncomeRecord> {
  const response = await api.post<IncomeRecord>('/incomes', data);
  return response.data;
}

export async function getExpenses(): Promise<ExpenseRecord[]> {
  const response = await api.get<ExpenseRecord[]>('/expenses');
  return response.data;
}

export async function recordExpense(data: {
  amount: number;
  merchant: string;
  description?: string;
  expenseDate: string;
  categoryId: number;
}): Promise<ExpenseRecord> {
  const response = await api.post<ExpenseRecord>('/expenses', data);
  return response.data;
}

export async function getMovements(): Promise<Movement[]> {
  const response = await api.get<Movement[]>('/movements');
  return response.data;
}
