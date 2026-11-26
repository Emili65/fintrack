import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './errors';

describe('errorMessage', () => {
  it('usa el mensaje del backend', () => {
    const err = new HttpErrorResponse({ status: 409, error: { status: 409, message: 'Ya existe una cuenta con ese correo' } });
    expect(errorMessage(err)).toBe('Ya existe una cuenta con ese correo');
  });

  it('une los errores de validación por campo', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: { status: 400, message: 'x', fieldErrors: { amount: 'El monto debe ser mayor que cero', date: 'La fecha es obligatoria' } },
    });
    expect(errorMessage(err)).toBe('El monto debe ser mayor que cero. La fecha es obligatoria');
  });

  it('explica cuando no hay conexión', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('No hay conexión');
  });
});
