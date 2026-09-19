---
inclusion: fileMatch
fileMatchPattern: ['frontend/**']
---

# Frontend conventions

Angular 22 SPA for the Sentinel AML app. Follow these rules when adding or editing frontend code.

## Stack

- Angular 22 with standalone components only. No `NgModule`.
- Signal-based local state (`signal`, `computed`); services expose RxJS `Observable`s.
- TypeScript (strict). Package manager: npm.

## Styling — Bootstrap only

- Style exclusively with Bootstrap 5.3 utility/component classes and `bootstrap-icons` in HTML templates.
- Bootstrap CSS/JS and icons are loaded via CDN in `src/index.html`; the app uses the light brand theme (`data-bs-theme="light"`).
- Do NOT add `.css`/`.scss` files, `styleUrl`/`styleUrls`, inline `styles:`, or `[style.*]` custom styling.
- The brand theme is applied ONLY by overriding Bootstrap's own `--bs-*` theme variables in a single `<style>` block in `src/index.html`. Components stay class-only.

## Brand theme (light, enterprise BFSI)

- Primary: deep navy `#1a2b6b` (Bootstrap `primary`, used for `btn-primary`, links, active nav).
- Accent: vivid orange `#f47b20` — reserved for emphasis, highlights, and warnings; not for full primary buttons.
- App background light `#f5f6f9`; white cards; dark navy sidebar with light text.
- Severity colors: CRITICAL/danger red, HIGH orange, MEDIUM info, LOW success (via `core/badges.ts`).

## Components

- One folder per feature under `src/app/features/<feature>/`.
- Templates live in separate `.html` files referenced by `templateUrl` — never inline `template:`.
- File naming: `x.component.ts` + `x.component.html`; selectors use the `app-` prefix (e.g. `app-alert-list`).
- Class names omit the `Component` suffix (e.g. class `AlertList`).
- Prefer `inject()` for dependencies over constructor injection.
- Declare component dependencies in the `imports:` array (e.g. `FormsModule`, `RouterLink`, pipes).
- Track `loading` and `error` state as signals; show a user-facing error rather than letting requests fail silently.

## Architecture

- Services and shared logic live under `src/app/core/` and are `@Injectable({ providedIn: 'root' })`.
  - Services: `x.service.ts`. Other shared helpers: plain files (e.g. `models.ts`, `badges.ts`, `mask.ts`, `guards.ts`).
- The app shell lives under `src/app/shell/`; routes in `src/app/app.routes.ts`; providers in `src/app/app.config.ts`.
- API base URL comes from `src/environments/environment.ts` (`apiBaseUrl`); build service endpoints from it, never hardcode URLs.
- Shared response/DTO types belong in `core/models.ts` (e.g. `Page<T>`, `Alert`, `Role`).

## Auth

- JWT auth: `AuthService` holds token/role/username as signals and persists them in `localStorage`.
- `authInterceptor` attaches the `Bearer` token and, on a `401` (except login), clears the session and redirects to `/login`.
- Guard routes with `authGuard` (authenticated) and `adminGuard` (ADMIN role) from `core/guards.ts`.

## PII

- Mask PII in list views using `core/mask.ts` (`maskName`, `maskAccount`). The backend also masks list endpoints; client masking is a safeguard and keeps rendering consistent.
