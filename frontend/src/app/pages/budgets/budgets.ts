import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { currentYearMonth, monthLabel, YearMonth } from '../../core/dates';
import { errorMessage } from '../../core/errors';
import { FinanceService } from '../../core/finance.service';
import { Budget, Category } from '../../core/models';
import { CopPipe } from '../../shared/cop.pipe';
import { MonthPicker } from '../../shared/month-picker';

@Component({
  selector: 'app-budgets',
  imports: [DecimalPipe, ReactiveFormsModule, FormsModule, CopPipe, MonthPicker],
  template: `
    <header class="page-head">
      <h1>Presupuestos de {{ label() }}</h1>
      <div class="page-head__actions">
        <app-month-picker [value]="period()" (change)="setPeriod($event)" />
      </div>
    </header>

    @if (available().length > 0) {
      <section class="panel">
        <h2>Ponle un límite a una categoría</h2>
        <form [formGroup]="form" (ngSubmit)="create()" class="inline-form">
          <label class="field field--grow">
            <span>Categoría de gasto</span>
            <select formControlName="categoryId">
              <option [ngValue]="null" disabled>Elige una categoría</option>
              @for (c of available(); track c.id) {
                <option [ngValue]="c.id">{{ c.name }}</option>
              }
            </select>
          </label>
          <label class="field">
            <span>Límite mensual (COP)</span>
            <input type="number" min="1" step="1000" formControlName="limit" placeholder="500000">
          </label>
          <div class="inline-form__actions">
            <button type="submit" class="btn btn-primary" [disabled]="form.invalid">Crear presupuesto</button>
          </div>
        </form>
      </section>
    }

    @if (error()) {
      <p class="alert" role="alert">{{ error() }}</p>
    }

    <section class="panel">
      @if (budgets().length === 0) {
        <p class="muted">No hay presupuestos para {{ label() }}. Crea uno arriba y verás cuánto llevas gastado frente al límite.</p>
      } @else {
        <ul class="budget-list budget-list--roomy">
          @for (b of budgets(); track b.id) {
            <li [attr.data-status]="b.status">
              <div class="bars__row">
                <span class="budget-list__name"><i class="swatch" [style.background]="b.category.color"></i>{{ b.category.name }}</span>
                <span class="num">{{ b.percentUsed | number: '1.0-1' }} %</span>
              </div>
              <div class="meter"><div class="meter__fill" [style.width.%]="b.percentUsed > 100 ? 100 : b.percentUsed"></div></div>
              <div class="bars__row">
                <small class="muted">
                  {{ b.spent | cop }} gastado de {{ b.limit | cop }}.
                  @if (b.remaining >= 0) { Quedan {{ b.remaining | cop }}. } @else { <span class="neg">Te pasaste por {{ -b.remaining | cop }}.</span> }
                </small>
                <span>
                  @if (editingId() === b.id) {
                    <input type="number" class="input-sm" min="1" [(ngModel)]="editLimit" [ngModelOptions]="{ standalone: true }"
                           aria-label="Nuevo límite">
                    <button type="button" class="link-btn" (click)="saveLimit(b)">Guardar</button>
                    <button type="button" class="link-btn" (click)="editingId.set(null)">Cancelar</button>
                  } @else {
                    <button type="button" class="link-btn" (click)="startEdit(b)">Cambiar límite</button>
                    <button type="button" class="link-btn" (click)="remove(b)">Eliminar</button>
                  }
                </span>
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
})
export class BudgetsPage implements OnInit {
  private readonly api = inject(FinanceService);

  protected readonly period = signal<YearMonth>(currentYearMonth());
  protected readonly label = computed(() => monthLabel(this.period()));
  protected readonly budgets = signal<Budget[]>([]);
  protected readonly expenseCategories = signal<Category[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly editingId = signal<number | null>(null);
  protected editLimit = 0;

  /** Solo se ofrecen categorías que aún no tienen presupuesto este mes. */
  protected readonly available = computed(() => {
    const used = new Set(this.budgets().map(b => b.category.id));
    return this.expenseCategories().filter(c => !used.has(c.id));
  });

  protected readonly form = inject(FormBuilder).group({
    categoryId: [null as number | null, Validators.required],
    limit: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    this.api.categories('EXPENSE').subscribe(c => this.expenseCategories.set(c));
    this.load();
  }

  protected setPeriod(ym: YearMonth): void {
    this.period.set(ym);
    this.load();
  }

  protected create(): void {
    const v = this.form.getRawValue();
    const p = this.period();
    this.error.set(null);
    this.api.createBudget({ categoryId: Number(v.categoryId), limit: Number(v.limit), year: p.year, month: p.month })
      .subscribe({
        next: () => {
          this.form.reset();
          this.load();
        },
        error: err => this.error.set(errorMessage(err)),
      });
  }

  protected startEdit(b: Budget): void {
    this.editLimit = b.limit;
    this.editingId.set(b.id);
  }

  protected saveLimit(b: Budget): void {
    this.api.updateBudget(b.id, Number(this.editLimit)).subscribe({
      next: () => {
        this.editingId.set(null);
        this.load();
      },
      error: err => this.error.set(errorMessage(err)),
    });
  }

  protected remove(b: Budget): void {
    this.api.deleteBudget(b.id).subscribe({
      next: () => this.load(),
      error: err => this.error.set(errorMessage(err)),
    });
  }

  private load(): void {
    const p = this.period();
    this.api.budgets(p.year, p.month).subscribe({
      next: b => this.budgets.set(b),
      error: err => this.error.set(errorMessage(err)),
    });
  }
}
