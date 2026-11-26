import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../core/errors';
import { FinanceService } from '../../core/finance.service';
import { Category, CategoryType, Page, Transaction } from '../../core/models';
import { CopPipe } from '../../shared/cop.pipe';
import { TransactionForm } from '../../shared/transaction-form';

interface Filters {
  from: string;
  to: string;
  type: CategoryType | '';
  categoryId: number | null;
  search: string;
}

const EMPTY: Filters = { from: '', to: '', type: '', categoryId: null, search: '' };

@Component({
  selector: 'app-transactions',
  imports: [FormsModule, DatePipe, CopPipe, TransactionForm],
  template: `
    <header class="page-head">
      <h1>Movimientos</h1>
      <div class="page-head__actions">
        <button type="button" class="btn btn-primary" (click)="openNew()">Registrar movimiento</button>
      </div>
    </header>

    <form class="filters" (ngSubmit)="apply()" aria-label="Filtrar movimientos">
      <label class="field field--grow">
        <span>Buscar</span>
        <input type="search" name="search" [(ngModel)]="filters.search" placeholder="Descripción…">
      </label>
      <label class="field">
        <span>Tipo</span>
        <select name="type" [(ngModel)]="filters.type" (ngModelChange)="onTypeChange($event)">
          <option value="">Todos</option>
          <option value="INCOME">Ingresos</option>
          <option value="EXPENSE">Gastos</option>
        </select>
      </label>
      <label class="field">
        <span>Categoría</span>
        <select name="categoryId" [(ngModel)]="filters.categoryId">
          <option [ngValue]="null">Todas</option>
          @for (c of filterCategories(); track c.id) {
            <option [ngValue]="c.id">{{ c.name }}</option>
          }
        </select>
      </label>
      <label class="field">
        <span>Desde</span>
        <input type="date" name="from" [(ngModel)]="filters.from">
      </label>
      <label class="field">
        <span>Hasta</span>
        <input type="date" name="to" [(ngModel)]="filters.to">
      </label>
      <div class="filters__actions">
        <button type="submit" class="btn btn-secondary">Filtrar</button>
        <button type="button" class="btn btn-ghost" (click)="clear()">Limpiar</button>
      </div>
    </form>

    @if (error()) {
      <p class="alert" role="alert">{{ error() }}</p>
    }

    <section class="panel panel--flush">
      @if (page(); as p) {
        @if (p.content.length === 0) {
          <div class="empty">
            <p>No hay movimientos con estos filtros.</p>
            <button type="button" class="btn btn-secondary" (click)="openNew()">Registrar el primero</button>
          </div>
        } @else {
          <div class="table-wrap">
            <table class="table" [class.is-loading]="loading()">
              <thead>
                <tr>
                  <th scope="col">
                    <button type="button" class="sort-btn" (click)="toggleSort('date')">Fecha {{ arrow('date') }}</button>
                  </th>
                  <th scope="col">Descripción</th>
                  <th scope="col">Categoría</th>
                  <th scope="col" class="num">
                    <button type="button" class="sort-btn" (click)="toggleSort('amount')">Monto {{ arrow('amount') }}</button>
                  </th>
                  <th scope="col"><span class="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                @for (t of p.content; track t.id) {
                  <tr>
                    <td class="nowrap">{{ t.date | date: 'd MMM y' }}</td>
                    <td>{{ t.description || '—' }}</td>
                    <td><span class="chip"><i class="swatch" [style.background]="t.category.color"></i>{{ t.category.name }}</span></td>
                    <td class="num nowrap" [class.pos]="t.category.type === 'INCOME'" [class.neg]="t.category.type === 'EXPENSE'">
                      {{ t.category.type === 'INCOME' ? '+' : '−' }} {{ t.amount | cop }}
                    </td>
                    <td class="actions">
                      @if (confirming() === t.id) {
                        <span class="confirm">¿Eliminar?
                          <button type="button" class="link-btn neg" (click)="remove(t)">Sí, eliminar</button>
                          <button type="button" class="link-btn" (click)="confirming.set(null)">No</button>
                        </span>
                      } @else {
                        <button type="button" class="link-btn" (click)="openEdit(t)">Editar</button>
                        <button type="button" class="link-btn" (click)="confirming.set(t.id)">Eliminar</button>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <footer class="pager">
            <span class="muted">{{ p.totalElements }} movimientos</span>
            <div>
              <button type="button" class="btn btn-ghost" [disabled]="p.page === 0" (click)="go(p.page - 1)">Anterior</button>
              <span class="pager__pos">Página {{ p.page + 1 }} de {{ p.totalPages }}</span>
              <button type="button" class="btn btn-ghost" [disabled]="p.page + 1 >= p.totalPages" (click)="go(p.page + 1)">Siguiente</button>
            </div>
          </footer>
        }
      }
    </section>

    @if (formOpen()) {
      <app-transaction-form [categories]="categories()" [transaction]="editing()"
                            (saved)="onSaved()" (closed)="closeForm()" />
    }
  `,
})
export class TransactionsPage implements OnInit {
  private readonly api = inject(FinanceService);

  protected filters: Filters = { ...EMPTY };
  private applied: Filters = { ...EMPTY };

  protected readonly categories = signal<Category[]>([]);
  protected readonly page = signal<Page<Transaction> | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly sort = signal<{ field: 'date' | 'amount'; dir: 'asc' | 'desc' }>({ field: 'date', dir: 'desc' });
  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Transaction | null>(null);
  protected readonly confirming = signal<number | null>(null);
  private readonly typeFilter = signal<CategoryType | ''>('');

  protected readonly filterCategories = computed(() => {
    const type = this.typeFilter();
    return type ? this.categories().filter(c => c.type === type) : this.categories();
  });

  ngOnInit(): void {
    this.api.categories().subscribe(c => this.categories.set(c));
    this.load(0);
  }

  protected apply(): void {
    this.applied = { ...this.filters };
    this.typeFilter.set(this.filters.type);
    this.load(0);
  }

  protected onTypeChange(type: CategoryType | ''): void {
    this.filters.categoryId = null;
    this.typeFilter.set(type);
  }

  protected clear(): void {
    this.filters = { ...EMPTY };
    this.apply();
  }

  protected go(page: number): void {
    this.load(page);
  }

  protected toggleSort(field: 'date' | 'amount'): void {
    const current = this.sort();
    this.sort.set({ field, dir: current.field === field && current.dir === 'desc' ? 'asc' : 'desc' });
    this.load(0);
  }

  protected arrow(field: 'date' | 'amount'): string {
    const s = this.sort();
    return s.field !== field ? '' : s.dir === 'desc' ? '↓' : '↑';
  }

  protected openNew(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(t: Transaction): void {
    this.editing.set(t);
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  protected onSaved(): void {
    this.closeForm();
    this.load(this.page()?.page ?? 0);
  }

  protected remove(t: Transaction): void {
    this.api.deleteTransaction(t.id).subscribe({
      next: () => {
        this.confirming.set(null);
        const p = this.page();
        const stay = p && p.content.length === 1 && p.page > 0 ? p.page - 1 : p?.page ?? 0;
        this.load(stay);
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }

  private load(page: number): void {
    const f = this.applied;
    const s = this.sort();
    this.loading.set(true);
    this.error.set(null);
    this.api.transactions({
      ...f,
      page,
      size: 15,
      sort: `${s.field},${s.dir}`,
    }).subscribe({
      next: p => {
        this.page.set(p);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
