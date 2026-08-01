# NexusFi — Development Progress

**Last Updated:** August 1, 2026
**Developer:** Francisco Williams Jiménez Hernández (williamsjmzhdz)

---

## Copilot Mentor Guidelines

This project is a **hands-on learning experience**. Copilot acts as a Tech Lead/Senior Developer — not a code generator.

**Do:**
- Explain concepts before providing code
- Guide step-by-step, one feature at a time
- Review code Willy writes and suggest improvements
- Help debug, but let Willy solve the problem

**Do not:**
- Generate entire features without explanation
- Dump large code blocks without context
- Skip explanations of commands or concepts

---

## Current Status

**Phase:** 7 — Frontend Development (in progress)
**Branch:** `feature/dashboard`
**Production API:** https://nexusfi-production.up.railway.app/api/v1
**Latest Release:** v0.4.0

### Completed this phase (v0.4.0):
- Monorepo restructured: `backend/`, `frontend/`, `docs/` at repo root
- Branch strategy: `feature/*` → `develop` → `main` (no direct commits to develop or main)
- Vite 5 + React 19 + TypeScript 5.7 scaffolded in `frontend/`
- Tailwind CSS v3 + PostCSS configured
- React Router v7 + Axios v1.9 + Lucide React installed
- Dev server confirmed at `http://localhost:5173`
- Vite boilerplate cleaned; folder structure created (`pages/`, `components/`, `services/`, `hooks/`, `types/`)
- Design system documented in `frontend/docs/DESIGN_SYSTEM.md` (Inter font, indigo/emerald/rose palette)
- Inter font applied globally via Google Fonts in `index.css`
- Login page UI: modern glassmorphism card, gradient wordmark, animated button
- Auth types (`types/auth.ts`), Axios instance (`services/api.ts`), auth service (`services/authService.ts`)
- Login connected to Railway API — JWT stored in `localStorage`, redirects to `/dashboard`
- CORS fixed: `ALLOWED_ORIGINS=http://localhost:5173` set in Railway dashboard
- Register page skipped — single-user app, account created via Postman
- Dashboard layout: white sidebar (w-60), NavLink active highlight, topbar with firstName greeting
- Auth guard (`PrivateRoute`) — redirects to /login if no JWT in localStorage
- `firstName` added to `AuthResponse` DTO; backend `AuthController` fixed to cast principal to `CustomUserDetails`
- Dashboard page: 3 summary cards (Net Balance, Total Income, Total Expenses) + Recent Movements table (last 10, sorted newest first)
- Income page (`/income`): record income form + history table; income auto-distributes to categories
- Categories page (`/categories`): root allocation progress bar, add category form (name/percentage/parent), tree table with balance per category, delete leaf categories
- Expenses page (`/expenses`): record expense form (amount/merchant/category/date/description) + history table; category dropdown shows only leaf categories with `Parent › Child` label
- `finance.ts` types: `Category`, `Movement`, `IncomeRecord`, `ExpenseRecord`, `MovementType`
- `financeService.ts`: `getCategories`, `createCategory`, `deleteCategory`, `getIncomes`, `recordIncome`, `getExpenses`, `recordExpense`, `getMovements`

### Known issues / backlog for v0.5.0:
- **Data isolation bug:** Incomes and expenses from other users appear in the dashboard — `MovementController` and `ExpenseController` use hardcoded `userId=1` instead of the authenticated user. Need to add `@AuthenticationPrincipal CustomUserDetails` to those endpoints.
- **Error message formatting:** Backend errors like `"Insufficient balance in category 'Afore'. Available: 0.00, Required: 59000"` should be surfaced with better UI formatting (not raw alert/inline text).
- **Subcategory percentage constraint:** Need to verify that subcategory percentages are also validated to sum to 100% within their parent, same as root categories.
- **Parent category balance:** Category tree table does not show the summed balance for parent categories (only leaf balances shown). Should aggregate children balances and display on parent row.
- **Dashboard redesign:** User wants richer dashboard with: Recent Movements, Total Net Balance (with Afore), Total Net Balance (without Afore), Total Income current month, Total Expenses current month, Total Net current month, charts/graphs.
- **Categories page UX:** Needs visual redesign — current table is functional but not intuitive or visually clear.

### Next steps (v0.5.0):
- [ ] Fix data isolation: wire `@AuthenticationPrincipal` into MovementController and ExpenseController
- [ ] Fix parent category balance display
- [ ] Validate subcategory percentage constraint in UI
- [ ] Dashboard redesign with monthly stats and charts (consider recharts or chart.js)
- [ ] Categories page visual redesign
- [ ] Improve error message UI

---

## Dev Environment

**As of Aug 1, 2026, primary dev machine moved to Linux (Fedora).** The Windows setup below is kept for reference in case that machine is used again, but is no longer the canonical environment.

### Linux (Fedora, current)

| Item | Value |
|------|-------|
| Dev path | `/home/williams/Documents/repositories/NexusFi` |
| Java (system default) | JDK 25 (Fedora 44 default; `dnf` no longer ships `java-17-openjdk`) |
| Java (backend build) | **JDK 17 required** — Spring Boot 3.2.0 / `maven-compiler-plugin` targets `--release 17`, which JDK 25's `javac` refuses to emit. Installed Temurin 17 standalone at `~/.jdks/temurin-17` (not the system default). Always run backend Maven commands with `JAVA_HOME=~/.jdks/temurin-17` — the `scripts/*.sh` dev scripts already do this. |
| Node | v24.18.0 / npm 11.16.0, installed via `sudo dnf install -y nodejs24 nodejs24-npm` |
| PostgreSQL | 18.3, installed via `sudo dnf install -y postgresql-server postgresql-contrib`; cluster initialized with `postgresql-setup --initdb`, `systemctl enable --now postgresql`. Host (TCP) auth in `pg_hba.conf` had to be changed from Fedora's default `ident` to `scram-sha-256` for `localhost`/`::1`, since the app connects via `jdbc:postgresql://localhost:5432/...` (TCP), not the local socket. |
| Local DB password | Generated dev password stored in `backend/.env.local` (gitignored, not committed) — source it or export `DB_PASSWORD` before running the backend. |
| Vite | Upgraded to v8.x — the Windows Rolldown/App-Control blocker below no longer applies now that Linux is primary. |
| Tailwind | Upgraded to v4.x, using the `@tailwindcss/vite` plugin (no `tailwind.config.js`/`postcss.config.js` anymore — see Phase 7 notes). |
| Dev server | `cd frontend && npm run dev` → `localhost:5173` |
| Backend dev scripts | `backend/scripts/{start,stop,status,reset-dev-data}-dev.sh` — Linux ports of the existing `.ps1`/`.cmd` scripts, same behavior/flags (`--restart`, `--force`, `--password=`). |
| Git push auth | `gh auth login` (browser flow) + `gh auth setup-git` — no PAT/SSH key needed. |

### Windows (historical / secondary)

| Item | Value |
|------|-------|
| Dev path | `C:\dev\nexusfi` — canonical, npm works here (no spaces) |
| Backup path | `G:\Other computers\PC\Work\projects\nexusfi` — Google Drive sync, **read-only** |
| Dev server | `cd C:\dev\nexusfi\frontend` then `npm run dev` → `localhost:5174` |
| Java | 17 |
| Spring Boot | 3.2.0 |
| Node | v24.14.0 / npm 11.9.0 |
| Vite | Was pinned to 5.4.x — v6+ uses Rolldown native binary blocked by Windows App Control policy on this machine. If resuming work here, re-check whether that policy still blocks it before assuming the Linux versions apply as-is. |
| Tailwind | Was v3 + PostCSS (v4 requires Vite 6+) |

---

## Phase History

### Phase 1 — Project Setup & Database (Oct 12, 2025) ✅

- PostgreSQL 16 database `nexusfi` with 6 tables, 27 indexes, 6 triggers, 3 functions, 2 views
- 7 JPA entities with relationships and Lombok
- Spring Boot application configured with `application-dev.yml` and env-var credentials
- Git + GitHub set up; branches `main` and `develop` established

### Phase 2 — Repository Layer (Oct 18, 2025) ✅

- 6 Spring Data JPA repositories with derived query methods and custom `@Query`
- Feature branch `feature/repository-layer` merged to `develop` with `--no-ff`

### Phase 3 — Service Layer (Oct 19, 2025) ✅

- 6 service classes: user registration, category percentage validation, income auto-distribution, expense balance check, zero-sum transfers, movement history
- Custom exception hierarchy: `NexusFiException` → `ResourceNotFoundException` (404), `DuplicateResourceException` (409), `InsufficientBalanceException` (400), `InvalidPercentageException` (400)
- `GlobalExceptionHandler` with `@RestControllerAdvice`

### Phase 4 — REST Controller Layer (Oct 20, 2025) ✅

- 5 controllers, 21 endpoints, 11 DTOs (request + response)
- Input validation with `@Valid` and Bean Validation
- Proper HTTP status codes throughout
- Released as **v0.2.0**

### Phase 5 — Spring Security + JWT (Nov 3, 2025 → Jan 10, 2026) ✅

- `CustomUserDetails`, `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`
- BCrypt password hashing; stateless JWT authentication
- `AuthController`: `POST /api/v1/auth/register` (201), `POST /api/v1/auth/login` (200)
- All endpoints versioned under `/api/v1/`
- Hierarchical categories: max 2 levels, `MaxDepthExceededException` on level 3
- Recursive income distribution algorithm; `FetchType.EAGER` to prevent `LazyInitializationException`
- 36/36 Postman tests passing
- Released as **v0.3.0**

### Phase 6 — Production Deployment on Railway (Feb 22, 2026) ✅

- Multi-stage Dockerfile (`eclipse-temurin:17-jdk` → `17-jre-jammy`), non-root user, HEALTHCHECK
- `application-prod.yml`; JWT secret via env var; CORS configured
- Railway: PostgreSQL 17.7 with SSL, HikariCP pool tuned (max=3), auto-deploy from GitHub
- 76/78 Postman tests passing in production
- Released as **v0.3.1**, then restructured to monorepo as **v0.3.2**

### Phase 7 — Frontend Development (Apr 7, 2026 → present) 🚧

See **Current Status** section above.

**Key decision — Vite 5 (superseded Aug 1, 2026):** v6/v7/v8 ship Rolldown, a Rust native binary (`.node` file) blocked by Windows Application Control policy on the original Windows dev machine. Pinned to Vite 5 which uses pure-JS Rollup. Once the primary dev machine moved to Linux, this blocker no longer applied, so the project was upgraded to Vite 8.x.

**Key decision — Tailwind v3 (superseded Aug 1, 2026):** `@tailwindcss/vite` plugin (v4's install method) requires Vite 6+. Used v3 with `postcss.config.js` and `tailwind.config.js` while pinned to Vite 5. Migrated to Tailwind v4 alongside the Vite upgrade: `postcss.config.js`/`autoprefixer`/`tailwind.config.js` removed, `@tailwindcss/vite` plugin added to `vite.config.ts`, and `src/index.css` now uses `@import "tailwindcss";` instead of the three `@tailwind` directives. No custom theme was defined in the old config, so no visual changes.

### Environment setup session (Aug 1, 2026)

Retook the project on a new Linux machine after ~3 months idle. What changed:
- Installed and verified toolchain: JDK 17 (Temurin, standalone), Node 24, PostgreSQL 18 — see Dev Environment table above.
- Verified backend compiles (`mvnw compile`), runs locally against a fresh local DB loaded from `database/schema.sql`, and answers auth requests correctly.
- Verified frontend installs, builds, lints, and dev-serves — found and fixed two **pre-existing, previously-untested** bugs in the scaffold, unrelated to the Vite/Tailwind upgrade:
  - `tsconfig.*.json` used `erasableSyntaxOnly`, a TS 5.8+ option, while `typescript` was pinned to `~5.7.2` → `npm run build` failed. Fixed by bumping `typescript` to `^5.9.3` (stayed on the 5.x line; TypeScript 7 native-compiler rewrite was available but not adopted here to avoid an unrelated large migration).
  - `eslint.config.js` referenced `reactHooks.configs.flat.recommended`, which doesn't exist in the installed `eslint-plugin-react-hooks@5.2.0` → `npm run lint` crashed. Fixed to `reactHooks.configs['recommended-latest']`.
  - Both bugs meant `npm run build` and `npm run lint` had likely never been run successfully before — only `npm run dev` had been exercised.
- Upgraded Vite 5→8 and Tailwind 3→4 (see key decisions above) now that Linux is the primary environment; ran `npm audit fix` twice, cutting frontend vulnerabilities from 10 to 1 (the remaining `react-router` advisory is a React Server Components CSRF bypass — not applicable, this app is a plain SPA — and has no patched release yet).
- Added `backend/scripts/{start,stop,status,reset-dev-data}-dev.sh` as Linux equivalents of the existing `.ps1`/`.cmd` scripts.
- Git branch cleanup: created local `develop` tracking `origin/develop` (was missing locally); deleted `origin/feature/frontend-setup` (fully merged into `develop`, stale since April). Current branches: `main`, `develop` only.
- This machine had no git identity, no push credentials, and no `gh` CLI — set up `git config user.name/email` (matches existing commit history), installed `gh`, and authenticated via `gh auth login` + `gh auth setup-git`.

---

## Release Roadmap

| Version | Name | Scope | Status |
|---|---|---|---|
| v0.1.0 | Foundation | Spring Boot + DB + Repositories + Services | ✅ Released |
| v0.2.0 | REST API | 21 endpoints, DTOs, validation | ✅ Released |
| v0.3.0 | Security | JWT auth, Spring Security, hierarchical categories | ✅ Released |
| v0.3.1 | Production | Railway deployment, Docker, PostgreSQL SSL | ✅ Released |
| v0.3.2 | Monorepo | Repository restructure | ✅ Released |
| **v0.4.0** | **First Usable** | Dashboard + auth guard + balances + income/expense forms + categories | ✅ Released |
| v0.5.0 | Polish & Fixes | Data isolation fix, dashboard redesign with charts, categories UX, error formatting | 🚧 Next |
| v0.6.0 | Polish | Edit/delete movements, charts, mobile nav, empty states | ⬜ Planned |
| v1.0.0 | Production Frontend | Frontend deployed (Vercel/Railway), full E2E on production URL | ⬜ Planned |

**Versioning rules:**
- `v0.x.0` — new feature set shipped and working
- `v0.x.y` — bug fixes or minor corrections to an existing release
- `v1.0.0` — the app is fully deployed, usable at a real URL, and stable

---

## v0.4.0 Milestone — "First Usable Release" ✅

**Definition:** You can log in and actually use the app to manage your finances.

| # | Feature | Status |
|---|---|---|
| 1 | Dashboard layout (sidebar + topbar) | ✅ |
| 2 | Auth guard (redirect to login if no token) | ✅ |
| 3 | Balance summary cards | ✅ |
| 4 | Recent movements list | ✅ |
| 5 | Add income form | ✅ |
| 6 | Add expense form | ✅ |
| 7 | Categories page | ✅ |

## v0.5.0 Milestone — "Polish & Fixes"

**Definition:** Data is correct, UX is clear, dashboard shows meaningful monthly stats with charts.

| # | Feature | Status |
|---|---|---|
| 1 | Fix data isolation (MovementController + ExpenseController use hardcoded userId=1) | ⬜ |
| 2 | Fix parent category balance (aggregate children balances) | ⬜ |
| 3 | Validate subcategory % constraint in UI | ⬜ |
| 4 | Dashboard redesign: monthly stats + charts | ⬜ |
| 5 | Categories page visual redesign | ⬜ |
| 6 | Improve error message formatting/UI | ⬜ |

---

## Releases

| Tag | Description | Date |
|-----|-------------|------|
| v0.4.0 | First Usable Release — full frontend (login, dashboard, income, expenses, categories) | Apr 25, 2026 |
| v0.3.2 | Backend repository reorganization (monorepo) | Mar 15, 2026 |
| v0.3.1 | Production deployment on Railway | Feb 22, 2026 |
| v0.3.0 | Spring Security + JWT + Hierarchical Categories | Jan 10, 2026 |
| v0.2.0 | REST API complete (21 endpoints) | Oct 20, 2025 |
| v0.1.0 | Spring Boot + Database + Repositories + Services | Oct 18, 2025 |

All releases: https://github.com/williamsjmzhdz/NexusFi/releases

---

## Resume Prompt

Copy this into a new chat to restore context:

```
Continuemos con NexusFi. Lee docs/PROGRESS.md para el contexto completo.
Actúa como Tech Lead / mentor: explica antes de codificar, guía paso a paso.

Estado actual:
- Branch: develop (crear feature/* desde aquí para nuevo trabajo)
- Dev entorno: Linux — cd frontend && npm run dev → localhost:5173
- Backend local: cd backend && ./scripts/start-dev.sh (necesita JDK 17 y Postgres local, ver Dev Environment)
- Backend en Railway: https://nexusfi-production.up.railway.app/api/v1
- Frontend apunta directo a Railway (src/services/api.ts) — funciona sin backend local

Siguiente tarea: v0.5.0 — empezar por el bug de aislamiento de datos
(MovementController/ExpenseController usan userId=1 hardcodeado en vez de
@AuthenticationPrincipal)
```

---

## Quick Reference

### Git workflow
```bash
git checkout -b feature/name   # create feature branch from develop
git add . && git commit -m ""  # commit
git checkout develop
git merge --no-ff feature/name # merge back
git tag -a v0.x.0 -m ""        # tag release
git push && git push --tags
```

### Frontend dev server
```bash
# Linux
cd frontend && npm run dev              # http://localhost:5173

# Windows
cd C:\dev\nexusfi\frontend
npm run dev                             # http://localhost:5174
```

### Backend (local)
```bash
# Linux — needs JDK 17 explicitly (system default may be newer)
cd backend
JAVA_HOME=~/.jdks/temurin-17 DB_PASSWORD=... ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# or use the helper scripts:
./scripts/start-dev.sh   # add --restart to replace a running instance
./scripts/status-dev.sh
./scripts/stop-dev.sh    # add --force to stop a non-NexusFi process on 8080
./scripts/reset-dev-data.sh   # wipes local data, asks for confirmation

# Windows
cd C:\dev\nexusfi\backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
