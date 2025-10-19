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

#### 1.1 Repository Layer

- [ ] `UserRepository` - User data access
- [ ] `CategoryRepository` - Category CRUD with user filtering
- [ ] `IncomeRecordRepository` - Income records with date filtering
- [ ] `ExpenseRecordRepository` - Expense records with date filtering
- [ ] `TransferRepository` - Transfer records
- [ ] `MovementRepository` - All movements view (read-only)

**Estimated Time:** 30-45 minutes (mostly typing, Spring Data JPA generates implementation)

#### 1.2 Service Layer

- [ ] `AuthService` - Registration, login, JWT token generation
- [ ] `CategoryService` - Create/update/delete categories, validate percentages sum to 100%
- [ ] `IncomeService` - Record income, implement distribution algorithm
- [ ] `ExpenseService` - Record expenses, validate sufficient balance
- [ ] `TransferService` - Transfer between categories (zero-sum validation)
- [ ] `MovementService` - Query all movements with filters

**Estimated Time:** 2-3 hours (core business logic)

#### 1.3 REST Controllers

- [ ] `AuthController` - `/api/auth/register`, `/api/auth/login`
- [ ] `CategoryController` - CRUD endpoints for categories
- [ ] `IncomeController` - POST `/api/income`, GET `/api/income`
- [ ] `ExpenseController` - POST `/api/expense`, GET `/api/expense`
- [ ] `TransferController` - POST `/api/transfer`, GET `/api/transfer`
- [ ] `MovementController` - GET `/api/movements` (with filters)

**Estimated Time:** 2-3 hours (DTOs + validation + error handling)

#### 1.4 Security Configuration

- [ ] JWT-based authentication (stateless)
- [ ] Password encryption with BCrypt
- [ ] Public endpoints: `/api/auth/**`
- [ ] Protected endpoints: `/api/**` (require authentication)
- [ ] CORS configuration for frontend

**Estimated Time:** 1-2 hours

#### 1.5 Testing (Basic)

- [ ] Manual testing with Postman/Thunder Client
- [ ] Verify all endpoints work
- [ ] Test business rules (percentages, balance validation)
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
