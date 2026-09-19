import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.Login),
  },
  {
    path: '',
    loadComponent: () => import('./shell/shell.component').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.Dashboard),
      },
      {
        path: 'ingest',
        loadComponent: () => import('./features/ingest/ingest.component').then((m) => m.Ingest),
      },
      {
        path: 'alerts',
        loadComponent: () =>
          import('./features/alerts/alert-list.component').then((m) => m.AlertList),
      },
      {
        path: 'alerts/:id',
        loadComponent: () =>
          import('./features/alerts/alert-detail.component').then((m) => m.AlertDetailComponent),
      },
      {
        path: 'cases',
        loadComponent: () =>
          import('./features/cases/case-list.component').then((m) => m.CaseListComponent),
      },
      {
        path: 'cases/:id',
        loadComponent: () =>
          import('./features/cases/case-detail.component').then((m) => m.CaseDetailComponent),
      },
      {
        path: 'admin/rules',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/rule-config.component').then((m) => m.RuleConfigComponent),
      },
      {
        path: 'admin/audit',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/audit-trail.component').then((m) => m.AuditTrailComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
