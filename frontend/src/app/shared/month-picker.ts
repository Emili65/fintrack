import { Component, computed, input, output } from '@angular/core';
import { addMonths, isCurrentOrFuture, monthLabel, YearMonth } from '../core/dates';

@Component({
  selector: 'app-month-picker',
  template: `
    <div class="month-picker">
      <button type="button" class="icon-btn" (click)="change.emit(prev())" aria-label="Mes anterior">‹</button>
      <span class="month-picker__label">{{ label() }}</span>
      <button type="button" class="icon-btn" (click)="change.emit(next())" [disabled]="atCurrent()"
              aria-label="Mes siguiente">›</button>
    </div>
  `,
})
export class MonthPicker {
  readonly value = input.required<YearMonth>();
  readonly change = output<YearMonth>();

  protected readonly label = computed(() => monthLabel(this.value()));
  protected readonly prev = computed(() => addMonths(this.value(), -1));
  protected readonly next = computed(() => addMonths(this.value(), 1));
  protected readonly atCurrent = computed(() => isCurrentOrFuture(this.value()));
}
