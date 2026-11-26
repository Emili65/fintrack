export type CategoryType = 'INCOME' | 'EXPENSE';

export interface User {
  id: number;
  name: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  color: string;
  icon?: string | null;
}

export interface CategoryRequest {
  name: string;
  type: CategoryType;
  color: string;
  icon?: string | null;
}

export interface Transaction {
  id: number;
  amount: number;
  date: string; // yyyy-MM-dd
  description?: string | null;
  category: Category;
}

export interface TransactionRequest {
  categoryId: number;
  amount: number;
  date: string;
  description?: string | null;
}

export interface TransactionFilter {
  from?: string;
  to?: string;
  type?: CategoryType | '';
  categoryId?: number | null;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Summary {
  from: string;
  to: string;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  savingsRate: number;
  transactionCount: number;
}

export interface CategoryBreakdown {
  categoryId: number;
  name: string;
  color: string;
  total: number;
  percentage: number;
}

export interface MonthlyPoint {
  month: number;
  income: number;
  expense: number;
  balance: number;
}

export type BudgetStatus = 'OK' | 'WARNING' | 'EXCEEDED';

export interface Budget {
  id: number;
  category: Category;
  year: number;
  month: number;
  limit: number;
  spent: number;
  remaining: number;
  percentUsed: number;
  status: BudgetStatus;
}

export interface ApiError {
  status: number;
  message: string;
  fieldErrors?: Record<string, string>;
}
