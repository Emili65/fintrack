import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/errors';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth">
      <section class="auth__intro">
        <a class="brand brand--lg" routerLink="/login"><span class="brand__mark" aria-hidden="true"></span>FinTrack</a>
        <p class="auth__pitch">Anota cada peso que entra y sale. Al final del mes sabrás exactamente cuánto ahorraste y en qué se fue el resto.</p>
        <div class="ledger-sample" aria-hidden="true">
          <div><span>Salario</span><span class="pos">+ 3.800.000</span></div>
          <div><span>Arriendo</span><span class="neg">− 1.200.000</span></div>
          <div><span>Mercado</span><span class="neg">− 214.500</span></div>
          <div class="ledger-sample__total"><span>Te quedan</span><span>2.385.500</span></div>
        </div>
      </section>

      <section class="auth__panel">
        <h1>Inicia sesión</h1>
        <form [formGroup]="form" (ngSubmit)="submit()" class="stack">
          <label class="field">
            <span>Correo</span>
            <input type="email" formControlName="email" autocomplete="email">
          </label>
          <label class="field">
            <span>Contraseña</span>
            <input type="password" formControlName="password" autocomplete="current-password">
          </label>
          @if (error()) {
            <p class="alert" role="alert">{{ error() }}</p>
          }
          <button class="btn btn-primary btn-block" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Entrando…' : 'Entrar' }}
          </button>
        </form>
        <button type="button" class="btn btn-ghost btn-block" (click)="useDemo()">Probar con la cuenta demo</button>
        <p class="muted">¿No tienes cuenta? <a routerLink="/registro">Crea una gratis</a></p>
      </section>
    </div>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected useDemo(): void {
    this.form.setValue({ email: 'demo@fintrack.dev', password: 'Demo1234' });
    this.submit();
  }

  protected submit(): void {
    if (this.form.invalid) return;
    const { email, password } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigate(['/']),
      error: err => {
        this.loading.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
