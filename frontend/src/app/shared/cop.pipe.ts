import { Pipe, PipeTransform } from '@angular/core';

const formatter = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

const compact = new Intl.NumberFormat('es-CO', {
  notation: 'compact',
  maximumFractionDigits: 1,
});

/** Formatea un número como pesos colombianos: 1234567 → "$ 1.234.567". Con 'compact' → "1,2 M". */
@Pipe({ name: 'cop' })
export class CopPipe implements PipeTransform {
  transform(value: number | null | undefined, mode: 'full' | 'compact' = 'full'): string {
    const n = Number(value ?? 0);
    return mode === 'compact' ? compact.format(n) : formatter.format(n);
  }
}
