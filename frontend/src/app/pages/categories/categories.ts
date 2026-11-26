import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../core/errors';
import { FinanceService } from '../../core/finance.service';
import { Category, CategoryType } from '../../core/models';

const PALETTE = ['#16A34A', '#0D9488', '#3B82F6', '#8B5CF6', '#EC4899', '#EF4444', '#F97316', '#EAB308', '#64748B'];

@Component({
  selector: 'app-categories',
  imports: [ReactiveFormsModule],
  template: `
    <header class="page-head">
      <h1>Categorías</h1>
    </header>

    <section class="panel">
      <h2>{{ editingId() ? 'Editar categoría' : 'Nueva categoría' }}</h2>
      <form [formGroup]="form" (ngSubmit)="save()" class="inline-form">
        <label class="field field--grow">
          <span>Nombre</span>
          <input type="text" formControlName="name" maxlength="60" placeholder="Ej. Mascotas">
        </label>
        <label class="field">
          <span>Tipo</span>
          <select formControlName="type">
            <option value="EXPENSE">Gasto</option>
            <option value="INCOME">Ingreso</option>
          </select>
        </label>
        <fieldset class="field">
          <legend>Color</legend>
          <div class="palette">
            @for (c of palette; track c) {
              <button type="button" class="palette__swatch" [style.background]="c"
                      [class.is-on]="form.controls.color.value === c"
                      [attr.aria-pressed]="form.controls.color.value === c"
                      [attr.aria-label]="'Color ' + c" (click)="form.controls.color.setValue(c)"></button>
            }
          </div>
        </fieldset>
        <div class="inline-form__actions">
          @if (editingId()) {
            <button type="button" class="btn btn-ghost" (click)="reset()">Cancelar</button>
          }
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid || saving()">
            {{ editingId() ? 'Guardar cambios' : 'Crear categoría' }}
          </button>
        </div>
      </form>
      @if (error()) {
        <p class="alert" role="alert">{{ error() }}</p>
      }
    </section>

    <div class="grid-2">
      @for (group of groups(); track group.type) {
        <section class="panel">
          <h2>{{ group.title }}</h2>
          <ul class="cat-list">
            @for (c of group.items; track c.id) {
              <li>
                <i class="swatch swatch--lg" [style.background]="c.color"></i>
                <span class="cat-list__name">{{ c.name }}</span>
                <button type="button" class="link-btn" (click)="edit(c)">Editar</button>
                <button type="button" class="link-btn" (click)="remove(c)">Eliminar</button>
              </li>
            } @empty {
              <li class="muted">Sin categorías.</li>
            }
          </ul>
        </section>
      }
    </div>
  `,
})
export class CategoriesPage implements OnInit {
  private readonly api = inject(FinanceService);

  protected readonly palette = PALETTE;
  protected readonly categories = signal<Category[]>([]);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly groups = computed(() => [
    { type: 'EXPENSE' as CategoryType, title: 'Gastos', items: this.categories().filter(c => c.type === 'EXPENSE') },
    { type: 'INCOME' as CategoryType, title: 'Ingresos', items: this.categories().filter(c => c.type === 'INCOME') },
  ]);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(60)]],
    type: ['EXPENSE' as CategoryType, Validators.required],
    color: [PALETTE[2], Validators.required],
  });

  ngOnInit(): void {
    this.refresh();
  }

  protected edit(c: Category): void {
    this.editingId.set(c.id);
    this.error.set(null);
    this.form.setValue({ name: c.name, type: c.type, color: c.color });
  }

  protected reset(): void {
    this.editingId.set(null);
    this.form.reset({ name: '', type: 'EXPENSE', color: PALETTE[2] });
  }

  protected save(): void {
    if (this.form.invalid) return;
    const body = this.form.getRawValue();
    const id = this.editingId();
    this.saving.set(true);
    this.error.set(null);
    (id ? this.api.updateCategory(id, body) : this.api.createCategory(body)).subscribe({
      next: () => {
        this.saving.set(false);
        this.reset();
        this.refresh();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }

  protected remove(c: Category): void {
    this.error.set(null);
    this.api.deleteCategory(c.id).subscribe({
      next: () => this.refresh(),
      error: err => this.error.set(errorMessage(err)),
    });
  }

  private refresh(): void {
    this.api.categories().subscribe(c => this.categories.set(c));
  }
}
