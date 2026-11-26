import { addMonths, firstDay, lastDay, monthLabel } from './dates';

describe('dates', () => {
  it('suma y resta meses cruzando el año', () => {
    expect(addMonths({ year: 2026, month: 12 }, 1)).toEqual({ year: 2027, month: 1 });
    expect(addMonths({ year: 2026, month: 1 }, -1)).toEqual({ year: 2025, month: 12 });
    expect(addMonths({ year: 2026, month: 5 }, -17)).toEqual({ year: 2024, month: 12 });
  });

  it('calcula el primer y último día del mes (incluye bisiestos)', () => {
    expect(firstDay({ year: 2026, month: 3 })).toBe('2026-03-01');
    expect(lastDay({ year: 2028, month: 2 })).toBe('2028-02-29');
    expect(lastDay({ year: 2026, month: 2 })).toBe('2026-02-28');
    expect(lastDay({ year: 2026, month: 11 })).toBe('2026-11-30');
  });

  it('muestra el mes en español', () => {
    expect(monthLabel({ year: 2026, month: 9 })).toBe('septiembre 2026');
  });
});
