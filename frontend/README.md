# Sentinel AML — Frontend

Angular 22 analyst dashboard for the Sentinel AML transaction monitoring system. Talks to the Spring Boot API over REST/JSON.

## Stack

- Angular 22 (standalone components, signals, lazy-loaded routes)
- Bootstrap 5.3 + Bootstrap Icons (via CDN in `index.html`) — no custom CSS
- JWT auth with an HTTP interceptor and role-based route guards

## Run

```bash
npm install
npm start        # http://localhost:4200
```

Expects the backend at `http://localhost:8080/api/v1`. Change it in `src/environments/environment.ts` (`apiBaseUrl`).

```bash
npm run build    # production build -> dist/frontend
npm test         # unit tests
```

## Features

- **Login** — JWT sign-in (demo: `analyst / analyst123`, `admin / admin123`)
- **Dashboard** — alert stat cards + severity/rule breakdown
- **Alerts** — queue with filters, sort, pagination, PII masked; detail view with evidence timeline and "create case"
- **Cases** — list + detail with a disposition form (reason required)
- **Admin** (ADMIN role) — rule threshold config + immutable audit trail

## Structure

```
src/app/
  core/        services, models, guards, interceptor, helpers
  shell/       app layout (sidebar nav + logout)
  features/    login, dashboard, alerts, cases, admin
  app.routes.ts
```

## Conventions

- Components use `x.component.ts` + `x.component.html` (templates in separate files).
- Services in `core/` as `x.service.ts`; endpoints built from `environment.apiBaseUrl`.
- Styling is Bootstrap classes only; the brand theme is set by overriding Bootstrap `--bs-*` variables in `index.html`.
- See `.kiro/steering/frontend-conventions.md` for the full ruleset.
```
