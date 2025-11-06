# NexusFi - Minimum Viable Product (MVP) Definition

**Version:** 1.0  
**Created:** October 16, 2025  
**Target:** First deployable version with core functionality

---

## 🎯 MVP Scope

The MVP is the **minimum feature set** that makes NexusFi **useful and deployable**. A user should be able to:

- Register and login securely
- Create a budget with percentage-based categories
- Record income that auto-distributes to categories
- Record expenses that deduct from categories
- View current balances and recent movements

---

## ✅ MVP Checklist

### 1. Backend API (Spring Boot)

#### 1.1 Repository Layer ✅ COMPLETED

- [x] `UserRepository` - User data access
- [x] `CategoryRepository` - Category CRUD with user filtering
- [x] `IncomeRecordRepository` - Income records with date filtering
- [x] `ExpenseRecordRepository` - Expense records with date filtering
- [x] `TransferRepository` - Transfer records
- [x] `MovementRepository` - All movements view (read-only)

**Completed:** October 18, 2025 (45 minutes)

#### 1.2 Service Layer ✅ COMPLETED

- [x] `UserService` - Registration, user lookup
- [x] `CategoryService` - Create/update/delete categories, validate percentages sum to 100%
- [x] `IncomeService` - Record income, implement distribution algorithm
- [x] `ExpenseService` - Record expenses, validate sufficient balance
- [x] `TransferService` - Transfer between categories (zero-sum validation)
- [x] `MovementService` - Query all movements with filters

**Additional:** Professional exception handling

- [x] Custom exception classes (ResourceNotFoundException, DuplicateResourceException, etc.)
- [x] GlobalExceptionHandler for REST error responses

**Completed:** October 19, 2025 (3 hours including refactoring)

#### 1.3 REST Controllers ✅ COMPLETED

- [x] `CategoryController` - CRUD endpoints for categories (6 endpoints)
- [x] `IncomeController` - POST `/api/incomes`, GET `/api/incomes` (3 endpoints)
- [x] `ExpenseController` - POST `/api/expenses`, GET `/api/expenses` (4 endpoints)
- [x] `TransferController` - POST `/api/transfers`, GET `/api/transfers` (4 endpoints)
- [x] `MovementController` - GET `/api/movements` (with filters) (4 endpoints)

**Additional:** 11 DTO classes (Request + Response objects) for all controllers

**Total:** 21 REST endpoints, 1229 lines of code

**Features:**

- Input validation with @Valid annotations
- Proper HTTP status codes (200, 201, 204, 404)
- CategoryName embedding in responses for better UX
- Consistent patterns across all controllers

**Note:** AuthController delayed until security configuration phase

**Completed:** October 20, 2025 (4 hours)

#### 1.4 Security Configuration ✅ COMPLETED (Implementation)

- [x] Install Maven and configure development environment
- [x] Add Spring Security and JWT dependencies
- [x] Create CustomUserDetails (UserDetails adapter)
- [x] Create JwtUtil (token generation and validation)
- [x] Fix JWT configuration in application.yml (moved to root level)
- [x] Create authentication DTOs (LoginRequest, RegisterRequest, AuthResponse)
- [x] Create AuthController (register and login endpoints)
- [x] Create CustomUserDetailsService (loads users from database)
- [x] Create JwtAuthenticationFilter (intercepts requests, validates tokens)
- [x] Create SecurityConfig (PasswordEncoder, AuthenticationManager, SecurityFilterChain)
- [x] Update UserService with BCrypt password encryption
- [x] Configure public endpoints: `/api/v1/auth/**`
- [x] Configure protected endpoints: all others require authentication
- [x] Build verification: `mvn clean package -DskipTests` ✅ SUCCESS

**Status:** Implementation complete - Testing pending (requires PostgreSQL)

**Started:** October 21, 2025  
**Completed:** November 3, 2025  
**Time Spent:** ~4 hours

#### 1.5 Testing (Basic) ⏳ PENDING

- [ ] Manual smoke testing with Postman/Thunder Client
- [ ] Test authentication flow (register, login, get token)
- [ ] Test protected endpoints with valid JWT token
- [ ] Test protected endpoints without token (expect 401)
- [ ] Verify business rules (percentages, balance validation)
- [ ] End-to-end scenario testing

**Status:** Pending - Requires computer with PostgreSQL database

**Estimated Time:** 30 minutes - 1 hour

- [ ] Test authentication flow

**Estimated Time:** 1 hour

### 2. Frontend (Basic UI)

**Choice A: Simple HTML + JavaScript (Fastest)**

- [ ] Login/Register page
- [ ] Dashboard with category cards showing balances
- [ ] Form to create/edit categories
- [ ] Form to record income
- [ ] Form to record expenses
- [ ] Table showing recent movements

**Estimated Time:** 3-4 hours  
**Tech Stack:** Plain HTML, CSS, Vanilla JavaScript, Fetch API

**Choice B: React (More Professional)**

- [ ] Same features as Choice A
- [ ] React Router for navigation
- [ ] Component-based architecture
- [ ] State management (Context API or simple useState)

**Estimated Time:** 5-6 hours  
**Tech Stack:** React, React Router, Axios

**Recommendation for MVP:** Start with **Choice A** (plain HTML/JS) to deploy faster, then migrate to React later if desired.

### 3. Database

- [x] PostgreSQL schema deployed ✅ (Already done)
- [ ] Production database setup (cloud provider)
- [ ] Environment-specific configurations

**Estimated Time:** 30 minutes (when deploying)

### 4. Deployment

- [ ] Choose deployment platform (Railway, Heroku, or AWS)
- [ ] Create production configuration profile
- [ ] Add Dockerfile (optional, depends on platform)
- [ ] Set up environment variables (DB credentials, JWT secret)
- [ ] Deploy backend
- [ ] Deploy frontend (can be same server or separate)
- [ ] Verify deployment works

**Estimated Time:** 1-2 hours (first time), 10 minutes (subsequent deployments)

---

## 🚫 Out of Scope for MVP (Future Enhancements)

These features are **NOT required** for the first deployment:

- ❌ Password reset/recovery
- ❌ Email verification
- ❌ Profile picture upload
- ❌ Recurring income/expenses automation
- ❌ Budget reports and charts
- ❌ Export data (CSV, PDF)
- ❌ Multi-currency support
- ❌ Mobile app (native iOS/Android)
- ❌ Notifications/alerts
- ❌ Shared budgets (multi-user)
- ❌ Advanced filtering/search
- ❌ Dark mode
- ❌ Unit/Integration tests (nice to have, but not blocking deployment)

---

## 📊 MVP Progress Tracker

### Current Status: **Phase 1 - Backend Foundation**

| Phase       | Component                | Status         | Completion |
| ----------- | ------------------------ | -------------- | ---------- |
| **Phase 1** | **Backend Foundation**   | 🟡 In Progress | **20%**    |
|             | Data Model (Entities)    | ✅ Done        | 100%       |
|             | Database Schema          | ✅ Done        | 100%       |
|             | Spring Boot Setup        | ✅ Done        | 100%       |
|             | Repository Layer         | ⬜ Not Started | 0%         |
|             | Service Layer            | ⬜ Not Started | 0%         |
|             | REST Controllers         | ⬜ Not Started | 0%         |
|             | Security Config          | ⬜ Not Started | 0%         |
| **Phase 2** | **Frontend**             | ⬜ Not Started | **0%**     |
|             | Auth UI (Login/Register) | ⬜ Not Started | 0%         |
|             | Dashboard                | ⬜ Not Started | 0%         |
|             | Category Management      | ⬜ Not Started | 0%         |
|             | Income/Expense Forms     | ⬜ Not Started | 0%         |
|             | Movements View           | ⬜ Not Started | 0%         |
| **Phase 3** | **Deployment**           | ⬜ Not Started | **0%**     |
|             | Production Config        | ⬜ Not Started | 0%         |
|             | Platform Setup           | ⬜ Not Started | 0%         |
|             | Deploy & Test            | ⬜ Not Started | 0%         |

**Overall MVP Completion: ~7%** (Foundation laid, features to be built)

---

## 📅 Estimated Timeline

Based on focused development sessions:

| Session       | Tasks                                                       | Time      |
| ------------- | ----------------------------------------------------------- | --------- |
| **Session 1** | Repository Layer (6 interfaces)                             | 45 min    |
| **Session 2** | Service Layer (part 1: Auth, Category)                      | 2 hours   |
| **Session 3** | Service Layer (part 2: Income, Expense, Transfer, Movement) | 2 hours   |
| **Session 4** | REST Controllers + DTOs                                     | 2-3 hours |
| **Session 5** | Security Configuration + Manual Testing                     | 2 hours   |
| **Session 6** | Basic Frontend (HTML/CSS/JS)                                | 3-4 hours |
| **Session 7** | Frontend-Backend Integration + Testing                      | 2 hours   |
| **Session 8** | Deployment Setup + Deploy                                   | 1-2 hours |

**Total Estimated Time:** 15-18 hours of focused work  
**Sessions Required:** ~8 sessions (2-3 hours each)  
**Calendar Time:** 2-3 weeks (if working 2-3 times per week)

---

## 🎯 MVP Success Criteria

The MVP is **complete and ready to deploy** when:

✅ A user can register a new account  
✅ A user can login and receive a JWT token  
✅ A user can create categories that sum to 100%  
✅ A user can record income and see it auto-distribute to categories  
✅ A user can record expenses and see category balances update  
✅ A user can view all movements in chronological order  
✅ All operations are secure (authentication required)  
✅ The application runs on a cloud server accessible via URL  
✅ The application works on both desktop and mobile browsers

---

## 🚀 Post-MVP Roadmap (v0.2+)

After successful MVP deployment, prioritize based on user feedback:

**v0.2 - User Experience Improvements**

- Password reset functionality
- Better error messages and validation
- Loading states and animations
- Responsive design improvements

**v0.3 - Enhanced Features**

- Budget reports (monthly/yearly)
- Charts and visualizations
- Data export (CSV)
- Category icons/colors

**v0.4 - Automation**

- Recurring income/expenses
- Budget templates
- Automatic categorization suggestions

**v0.5 - Advanced Features**

- Multi-currency support
- Shared budgets (family/couples)
- Mobile app (React Native)
- Advanced analytics

---

## 📝 Notes

- This MVP definition is **flexible** - adjust based on learning and discoveries
- Focus on **working features** over perfect code
- **Ship early, iterate often** - don't wait for perfection
- Each completed phase should be merged to `main` and tagged (v0.2, v0.3, etc.)
- Keep PROGRESS.md updated with current status

---

**Next Step:** Start with Repository Layer (Session 1)  
**Document Updates:** Update this file as we complete each checklist item
