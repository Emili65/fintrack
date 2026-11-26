import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { currentYearMonth, firstDay, lastDay, monthLabel, YearMonth } from '../../core/dates';
import { errorMessage } from '../../core/errors';
import { FinanceService } from '../../core/finance.service';
import { Budget, Category, CategoryBreakdown, MonthlyPoint, Summary, Transaction } from '../../core/models';
import { CopPipe } from '../../shared/cop.pipe';
import { MonthPicker } from '../../shared/month-picker';
import { MonthlyChart } from '../../shared/monthly-chart';
import { TransactionForm } from '../../shared/transaction-form';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, DecimalPipe, CopPipe, MonthPicker, MonthlyChart, TransactionForm, RouterLink],
  templateUrl: './dashboard.html',
})
export class DashboardPage implements OnInit {
  private readonly api = inject(FinanceService);

  protected readonly period = signal<YearMonth>(currentYearMonth());
  protected readonly summary = signal<Summary | null>(null);
  protected readonly breakdown = signal<CategoryBreakdown[]>([]);
  protected readonly monthly = signal<MonthlyPoint[]>([]);
  protected readonly budgets = signal<Budget[]>([]);
  protected readonly recent = signal<Transaction[]>([]);
  protected readonly categories = signal<Category[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly formOpen = signal(false);

  protected readonly label = computed(() => monthLabel(this.period()));

  /**
   * La "tira del libro": el ingreso del mes es el 100 %; se divide en lo gastado y lo ahorrado.
   * Si el gasto supera el ingreso, la tira entera es gasto y se muestra el excedente.
   */
  protected readonly strip = computed(() => {
    const s = this.summary();
    if (!s) return null;
    const income = Number(s.totalIncome);
    const expense = Number(s.totalExpense);
    const base = Math.max(income, expense, 1);
    return {
      spentPct: (expense / base) * 100,
      savedPct: Math.max(income - expense, 0) / base * 100,
      overspent: expense > income ? expense - income : 0,
      empty: income === 0 && expense === 0,
    };
  });

  protected readonly topCategory = computed(() => this.breakdown()[0] ?? null);
  protected readonly maxCategory = computed(() => Math.max(...this.breakdown().map(b => Number(b.total)), 1));

  ngOnInit(): void {
    this.api.categories().subscribe(c => this.categories.set(c));
    this.load();
  }

  protected setPeriod(ym: YearMonth): void {
    this.period.set(ym);
    this.load();
  }

  protected onSaved(): void {
    this.formOpen.set(false);
    this.load();
  }

  private load(): void {
    const p = this.period();
    const from = firstDay(p);
    const to = lastDay(p);
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      summary: this.api.summary(from, to),
      breakdown: this.api.byCategory('EXPENSE', from, to),
      budgets: this.api.budgets(p.year, p.month),
      recent: this.api.transactions({ from, to, size: 6 }),
      monthly: this.api.monthly(p.year),
    }).subscribe({
      next: r => {
        this.summary.set(r.summary);
        this.breakdown.set(r.breakdown);
        this.budgets.set(r.budgets);
        this.recent.set(r.recent.content);
        this.monthly.set(r.monthly);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
