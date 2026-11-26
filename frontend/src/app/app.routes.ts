import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';
import { Shell } from './layout/shell';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login').then(m => m.LoginPage),
    title: 'Iniciar sesión · FinTrack',
  },
  {
    path: 'registro',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/register/register').then(m => m.RegisterPage),
    title: 'Crear cuenta · FinTrack',
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.DashboardPage),
        title: 'Resumen · FinTrack',
      },
      {
        path: 'movimientos',
        loadComponent: () => import('./pages/transactions/transactions').then(m => m.TransactionsPage),
        title: 'Movimientos · FinTrack',
      },
      {
        path: 'presupuestos',
        loadComponent: () => import('./pages/budgets/budgets').then(m => m.BudgetsPage),
        title: 'Presupuestos · FinTrack',
      },
      {
        path: 'categorias',
        loadComponent: () => import('./pages/categories/categories').then(m => m.CategoriesPage),
        title: 'Categorías · FinTrack',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
