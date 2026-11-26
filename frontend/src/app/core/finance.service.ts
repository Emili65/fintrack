import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Budget, Category, CategoryBreakdown, CategoryRequest, CategoryType, MonthlyPoint, Page,
  Summary, Transaction, TransactionFilter, TransactionRequest
} from './models';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private readonly http = inject(HttpClient);

  // ----- Categorías -----
  categories(type?: CategoryType): Observable<Category[]> {
    const params = type ? new HttpParams().set('type', type) : undefined;
    return this.http.get<Category[]>('/api/categories', { params });
  }

  createCategory(body: CategoryRequest): Observable<Category> {
    return this.http.post<Category>('/api/categories', body);
  }

  updateCategory(id: number, body: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`/api/categories/${id}`, body);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`/api/categories/${id}`);
  }

  // ----- Transacciones -----
  transactions(filter: TransactionFilter): Observable<Page<Transaction>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(filter)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<Page<Transaction>>('/api/transactions', { params });
  }

  createTransaction(body: TransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>('/api/transactions', body);
  }

  updateTransaction(id: number, body: TransactionRequest): Observable<Transaction> {
    return this.http.put<Transaction>(`/api/transactions/${id}`, body);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`/api/transactions/${id}`);
  }

  // ----- Presupuestos -----
  budgets(year: number, month: number): Observable<Budget[]> {
    return this.http.get<Budget[]>('/api/budgets', { params: { year, month } });
  }

  createBudget(body: { categoryId: number; year: number; month: number; limit: number }): Observable<Budget> {
    return this.http.post<Budget>('/api/budgets', body);
  }

  updateBudget(id: number, limit: number): Observable<Budget> {
    return this.http.put<Budget>(`/api/budgets/${id}`, { limit });
  }

  deleteBudget(id: number): Observable<void> {
    return this.http.delete<void>(`/api/budgets/${id}`);
  }

  // ----- Reportes -----
  summary(from: string, to: string): Observable<Summary> {
    return this.http.get<Summary>('/api/reports/summary', { params: { from, to } });
  }

  byCategory(type: CategoryType, from: string, to: string): Observable<CategoryBreakdown[]> {
    return this.http.get<CategoryBreakdown[]>('/api/reports/by-category', { params: { type, from, to } });
  }

  monthly(year: number): Observable<MonthlyPoint[]> {
    return this.http.get<MonthlyPoint[]>('/api/reports/monthly', { params: { year } });
  }
}
