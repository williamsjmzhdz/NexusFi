# NexusFi — Development Progress

**Last Updated:** April 25, 2026
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

| Item | Value |
|------|-------|
| Dev path | `C:\dev\nexusfi` — canonical, npm works here (no spaces) |
| Backup path | `G:\Other computers\PC\Work\projects\nexusfi` — Google Drive sync, **read-only** |
| Dev server | `cd C:\dev\nexusfi\frontend` then `npm run dev` → `localhost:5174` |
| Java | 17 |
| Spring Boot | 3.2.0 |
| Node | v24.14.0 / npm 11.9.0 |
| Vite | 5.4.x (must stay v5 — v6+ uses Rolldown native binary blocked by Windows App Control) |
| Tailwind | v3 + PostCSS (v4 requires Vite 6+) |

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

**Key decision — Vite 5:** v6/v7/v8 ship Rolldown, a Rust native binary (`.node` file) blocked by Windows Application Control policy on this machine. Pinned to Vite 5 which uses pure-JS Rollup.

**Key decision — Tailwind v3:** `@tailwindcss/vite` plugin (v4's install method) requires Vite 6+. Using v3 with `postcss.config.js` and `tailwind.config.js` instead.

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

Copy this into a new Copilot chat to restore context:

```
Continuemos con NexusFi. Lee docs/PROGRESS.md para el contexto completo.
Actúa como Tech Lead / mentor: explica antes de codificar, guía paso a paso.

Estado actual:
- Branch: feature/frontend-setup
- Dev: cd C:\dev\nexusfi\frontend && npm run dev  →  localhost:5174
- Backend en Railway: https://nexusfi-production.up.railway.app/api/v1

Siguiente tarea: Block 2 — crear estructura de carpetas en frontend/src/
(pages/, components/, services/, hooks/, types/)
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
cd C:\dev\nexusfi\frontend
npm run dev                    # http://localhost:5174
```

### Backend (local)
```bash
cd C:\dev\nexusfi\backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
