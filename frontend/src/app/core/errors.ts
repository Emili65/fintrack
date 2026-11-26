import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './models';

/** Convierte cualquier error HTTP en un mensaje legible para el usuario. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No hay conexión con el servidor. Revisa que el backend esté corriendo.';
    }
    const body = err.error as Partial<ApiError> | null;
    if (body?.fieldErrors) {
      return Object.values(body.fieldErrors).join('. ');
    }
    if (body?.message) {
      return body.message;
    }
  }
  return 'Algo salió mal. Intenta de nuevo.';
}
