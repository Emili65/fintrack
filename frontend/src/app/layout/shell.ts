import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <nav class="rail" aria-label="Principal">
        <a routerLink="/" class="brand" aria-label="FinTrack, ir al resumen">
          <span class="brand__mark" aria-hidden="true"></span>FinTrack
        </a>
        <ul class="rail__links">
          <li><a routerLink="/" routerLinkActive="is-active" [routerLinkActiveOptions]="{ exact: true }">Resumen</a></li>
          <li><a routerLink="/movimientos" routerLinkActive="is-active">Movimientos</a></li>
          <li><a routerLink="/presupuestos" routerLinkActive="is-active">Presupuestos</a></li>
          <li><a routerLink="/categorias" routerLinkActive="is-active">Categorías</a></li>
        </ul>
        <div class="rail__user">
          <span class="avatar" aria-hidden="true">{{ initials() }}</span>
          <span class="rail__name">{{ auth.user()?.name }}</span>
          <button type="button" class="link-btn" (click)="auth.logout()">Cerrar sesión</button>
        </div>
      </nav>
      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class Shell {
  protected readonly auth = inject(AuthService);
  protected readonly initials = computed(() =>
    (this.auth.user()?.name ?? '?')
      .split(/\s+/)
      .slice(0, 2)
      .map(p => p[0]?.toUpperCase() ?? '')
      .join(''));
}
