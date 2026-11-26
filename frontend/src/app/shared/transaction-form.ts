import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { today } from '../core/dates';
import { errorMessage } from '../core/errors';
import { FinanceService } from '../core/finance.service';
import { Category, CategoryType, Transaction } from '../core/models';

/** Panel lateral para registrar o editar un movimiento. */
@Component({
  selector: 'app-transaction-form',
  host: { '(document:keydown.escape)': 'closed.emit()' },
  imports: [ReactiveFormsModule],
  template: `
    <div class="sheet-backdrop" (click)="closed.emit()"></div>
    <aside class="sheet" role="dialog" aria-modal="true" [attr.aria-label]="title()">
      <header class="sheet__head">
        <h2>{{ title() }}</h2>
        <button type="button" class="icon-btn" (click)="closed.emit()" aria-label="Cerrar">×</button>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()" class="stack">
        <div class="segmented" role="radiogroup" aria-label="Tipo de movimiento">
          <button type="button" role="radio" [attr.aria-checked]="type() === 'EXPENSE'"
                  [class.is-on]="type() === 'EXPENSE'" class="seg-expense" (click)="setType('EXPENSE')">Gasto</button>
          <button type="button" role="radio" [attr.aria-checked]="type() === 'INCOME'"
                  [class.is-on]="type() === 'INCOME'" class="seg-income" (click)="setType('INCOME')">Ingreso</button>
        </div>

        <label class="field">
          <span>Monto (COP)</span>
          <input type="number" inputmode="decimal" min="0" step="100" formControlName="amount" class="input-amount"
                 placeholder="0" autofocus>
        </label>

        <label class="field">
          <span>Categoría</span>
          <select formControlName="categoryId">
            <option [ngValue]="null" disabled>Elige una categoría</option>
            @for (c of options(); track c.id) {
              <option [ngValue]="c.id">{{ c.name }}</option>
            }
          </select>
        </label>

        <label class="field">
          <span>Fecha</span>
          <input type="date" formControlName="date" [max]="maxDate">
        </label>

        <label class="field">
          <span>Descripción <em>(opcional)</em></span>
          <input type="text" formControlName="description" maxlength="255" placeholder="Ej. mercado de la semana">
        </label>

        @if (error()) {
          <p class="alert" role="alert">{{ error() }}</p>
        }

        <div class="row-end">
          <button type="button" class="btn btn-ghost" (click)="closed.emit()">Cancelar</button>
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid || saving()">
            {{ saving() ? 'Guardando…' : (editing() ? 'Guardar cambios' : 'Registrar movimiento') }}
          </button>
        </div>
      </form>
    </aside>
  `,
})
export class TransactionForm {
  private readonly api = inject(FinanceService);
  private readonly fb = inject(FormBuilder);

  readonly categories = input.required<Category[]>();
  readonly transaction = input<Transaction | null>(null);
  readonly saved = output<Transaction>();
  readonly closed = output<void>();

  protected readonly maxDate = today();
  protected readonly type = signal<CategoryType>('EXPENSE');
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly editing = computed(() => this.transaction() !== null);
  protected readonly title = computed(() => (this.editing() ? 'Editar movimiento' : 'Nuevo movimiento'));
  protected readonly options = computed(() => this.categories().filter(c => c.type === this.type()));

  protected readonly form = this.fb.group({
    amount: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
    categoryId: this.fb.control<number | null>(null, Validators.required),
    date: this.fb.nonNullable.control(today(), Validators.required),
    description: this.fb.nonNullable.control(''),
  });

  constructor() {
    effect(() => {
      const tx = this.transaction();
      if (tx) {
        this.type.set(tx.category.type);
        this.form.setValue({
          amount: tx.amount,
          categoryId: tx.category.id,
          date: tx.date,
          description: tx.description ?? '',
        });
      }
    });
  }

  protected setType(type: CategoryType): void {
    if (this.type() !== type) {
      this.type.set(type);
      this.form.controls.categoryId.setValue(null);
    }
  }

  protected save(): void {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    const body = {
      amount: Number(v.amount),
      categoryId: Number(v.categoryId),
      date: v.date,
      description: v.description.trim() || null,
    };
    this.saving.set(true);
    this.error.set(null);
    const tx = this.transaction();
    const request = tx ? this.api.updateTransaction(tx.id, body) : this.api.createTransaction(body);
    request.subscribe({
      next: saved => {
        this.saving.set(false);
        this.saved.emit(saved);
      },
      error: err => {
        this.saving.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
