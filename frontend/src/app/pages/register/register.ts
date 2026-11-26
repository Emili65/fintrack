import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/errors';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth auth--single">
      <section class="auth__panel">
        <a class="brand" routerLink="/login"><span class="brand__mark" aria-hidden="true"></span>FinTrack</a>
        <h1>Crea tu cuenta</h1>
        <p class="muted">Empiezas con categorías listas: salario, alimentación, transporte y más. Puedes cambiarlas cuando quieras.</p>
        <form [formGroup]="form" (ngSubmit)="submit()" class="stack">
          <label class="field">
            <span>Nombre</span>
            <input type="text" formControlName="name" autocomplete="name">
          </label>
          <label class="field">
            <span>Correo</span>
            <input type="email" formControlName="email" autocomplete="email">
          </label>
          <label class="field">
            <span>Contraseña</span>
            <input type="password" formControlName="password" autocomplete="new-password">
            <small>Mínimo 8 caracteres.</small>
          </label>
          @if (error()) {
            <p class="alert" role="alert">{{ error() }}</p>
          }
          <button class="btn btn-primary btn-block" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Creando cuenta…' : 'Crear cuenta' }}
          </button>
        </form>
        <p class="muted">¿Ya tienes cuenta? <a routerLink="/login">Inicia sesión</a></p>
      </section>
    </div>
  `,
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  protected submit(): void {
    if (this.form.invalid) return;
    const { name, email, password } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.auth.register(name, email, password).subscribe({
      next: () => this.router.navigate(['/']),
      error: err => {
        this.loading.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
