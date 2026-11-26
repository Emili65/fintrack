/** Utilidades de fechas sin librerías externas. Trabajan con 'yyyy-MM-dd' en hora local. */

export interface YearMonth {
  year: number;
  month: number; // 1-12
}

const MONTHS = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto',
  'septiembre', 'octubre', 'noviembre', 'diciembre'];

export const MONTHS_SHORT = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

export function currentYearMonth(): YearMonth {
  const now = new Date();
  return { year: now.getFullYear(), month: now.getMonth() + 1 };
}

export function addMonths(ym: YearMonth, delta: number): YearMonth {
  const index = ym.year * 12 + (ym.month - 1) + delta;
  return { year: Math.floor(index / 12), month: (index % 12) + 1 };
}

export function monthLabel(ym: YearMonth): string {
  return `${MONTHS[ym.month - 1]} ${ym.year}`;
}

export function firstDay(ym: YearMonth): string {
  return `${ym.year}-${pad(ym.month)}-01`;
}

export function lastDay(ym: YearMonth): string {
  const days = new Date(ym.year, ym.month, 0).getDate();
  return `${ym.year}-${pad(ym.month)}-${pad(days)}`;
}

export function today(): string {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export function isCurrentOrFuture(ym: YearMonth): boolean {
  const now = currentYearMonth();
  return ym.year > now.year || (ym.year === now.year && ym.month >= now.month);
}

function pad(n: number): string {
  return String(n).padStart(2, '0');
}
