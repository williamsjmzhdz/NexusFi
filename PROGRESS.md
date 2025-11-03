# NexusFi - Development Progress & Learning Journey

**Last Updated:** November 3, 2025  
**Developer:** Francisco Williams Jiménez Hernández (williamsjmzhdz)  
**Learning Approach:** Hands-on, step-by-step, with mentor guidance

---

## 🎓 Important: Learning Philosophy

This project is being developed as a **hands-on learning experience**. The approach is:

- ✅ **Step-by-step guidance** - One feature at a time
- ✅ **Learn by doing** - Willy writes the code himself
- ✅ **Mentor-style support** - Copilot acts as Tech Lead/Mentor, not code generator
- ✅ **Explanations first** - Understanding the "why" before the "what"
- ✅ **No code dumps** - Guide, review, and help debug, don't just provide complete solutions

### 🤝 Copilot's Role as Mentor

When continuing in a new chat session, please:

1. **Act as a Tech Lead/Senior Developer**, not just a code assistant
2. **Explain concepts** before providing code
3. **Guide step-by-step** - break down complex tasks
4. **Review code** Willy writes and suggest improvements
5. **Answer questions** about why things work the way they do
6. **Help debug** when stuck, but let Willy solve problems
7. **Encourage best practices** and professional development habits

**DO NOT:**

- ❌ Generate entire features without explanation
- ❌ Dump large code blocks without context
- ❌ Skip explanations of commands/concepts
- ❌ Move too fast without checking understanding

---

## 📍 Current Status: Phase 5 Complete - Pending Testing 🔐✅

**Current Phase:** Phase 5 - Spring Security with JWT Authentication (Implementation Complete)

JWT-based authentication implementation is **complete**! All security components have been created and the code compiles successfully. Smoke testing is pending (requires PostgreSQL database).

**Current Branch:** `feature/spring-security`

**Implementation Status:**

- ✅ Spring Security and JWT dependencies added
- ✅ CustomUserDetails wrapper class created
- ✅ JwtUtil for token generation and validation
- ✅ JWT configuration in application.yml (fixed placement)
- ✅ Authentication DTOs (LoginRequest, RegisterRequest, AuthResponse)
- ✅ AuthController (register and login endpoints)
- ✅ CustomUserDetailsService (loads users from database)
- ✅ JwtAuthenticationFilter (intercepts requests, validates tokens)
- ✅ SecurityConfig (PasswordEncoder, AuthenticationManager, SecurityFilterChain)
- ✅ UserService updated with BCrypt password hashing
- ✅ Build successful (mvn clean package -DskipTests)
- ⏳ Smoke testing pending (requires PostgreSQL on other computer)

**Latest Release:** v0.2.0 - REST API Complete (October 20, 2025)

---

## ✅ Phase 5: Spring Security + JWT Authentication (IMPLEMENTATION COMPLETE - Nov 3, 2025)

### ✨ What We've Accomplished (Oct 21 - Nov 3, 2025):

**Maven Setup:**

- ✅ Installed Apache Maven 3.9.11
- ✅ Configured system PATH for Maven commands
- ✅ Verified Maven installation and project compilation

**Dependencies Added:**

- ✅ `spring-boot-starter-security` - Spring Security framework
- ✅ `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (v0.12.5) - JWT library for token operations

**Security Infrastructure Created:**

- ✅ `CustomUserDetails` - Adapter between User entity and Spring Security
  - Implements UserDetails interface
  - Wraps User entity for authentication
  - Returns email as username
  - Comprehensive Javadoc comments
  
- ✅ `JwtUtil` - JWT token operations utility
  - Token generation with email, timestamps, expiration
  - Token validation (signature and expiration)
  - Claims extraction (email, expiration date)
  - Base64 secret key conversion to cryptographic key
  - Comprehensive Javadoc comments
  
- ✅ `CustomUserDetailsService` - Spring Security user loader
  - Implements UserDetailsService interface
  - Loads users from database by email
  - Throws UsernameNotFoundException if user doesn't exist
  - Used by AuthenticationManager during login
  
- ✅ `JwtAuthenticationFilter` - Request interceptor
  - Extends OncePerRequestFilter
  - Extracts JWT from Authorization header ("Bearer <token>")
  - Validates token signature and expiration
  - Sets SecurityContext authentication for valid tokens
  - Allows filter chain to continue for all requests
  
- ✅ `SecurityConfig` - Spring Security configuration
  - PasswordEncoder bean (BCryptPasswordEncoder)
  - AuthenticationManager bean (exposed from AuthenticationConfiguration)
  - SecurityFilterChain with JwtAuthenticationFilter registration
  - CSRF disabled (stateless JWT authentication)
  - Public endpoints: `/api/v1/auth/**` (permitAll)
  - All other endpoints require authentication
  - Stateless session management (no cookies)

**Configuration:**

- ✅ JWT configuration in `application.yml`
  - **Fixed placement:** Moved from `spring:` block to root level
  - Secret key (Base64 encoded, 64 characters)
  - Expiration time (24 hours = 86400000 ms)

**Authentication DTOs:**

- ✅ `LoginRequest` - Email + password for login (with validation)
- ✅ `RegisterRequest` - Email + password for registration (min 8 chars)
- ✅ `AuthResponse` - Returns JWT token + email

**Controllers:**

- ✅ `AuthController` - Public authentication endpoints
  - POST `/api/v1/auth/register` - User registration with password hashing
  - POST `/api/v1/auth/login` - User login with AuthenticationManager validation
  - Returns 201 Created for register, 200 OK for login
  - Returns AuthResponse with JWT token

**Services Updated:**

- ✅ `UserService` - Updated for BCrypt password encryption
  - Constructor now injects PasswordEncoder
  - `registerUser()` hashes password before saving
  - Throws DuplicateResourceException for existing emails

**Build & Compilation:**

- ✅ Build successful: `mvn clean package -DskipTests`
- ✅ All code compiles without errors
- ✅ No syntax or import issues

**Git Workflow:**

- ✅ Created `feature/spring-security` branch from develop
- ✅ Committed JWT infrastructure with proper messages

**Key Learning:**

- JWT (JSON Web Token) structure and purpose (Header.Payload.Signature)
- Stateless authentication vs session-based (no cookies, token in every request)
- Token signing with HMAC-SHA and secret keys (like wax seal)
- BCrypt password hashing with automatic salt generation
- How salt is stored in hash string and why it doesn't weaken security
- Base64 encoding for cryptographic keys
- Spring Security UserDetails interface and UserDetailsService
- SecurityContext and SecurityFilterChain concepts
- Filter registration order (addFilterBefore)
- CSRF protection and when to disable it
- AuthenticationManager and how it validates credentials
- PasswordEncoder bean exposure and dependency injection
- Proper Git branching workflow
- Moving commits between branches with reset/checkout

### 🧪 Next Steps (Testing & Deployment):

**Pending Tasks:**

- [ ] **Smoke testing on computer with PostgreSQL:**
  - Test POST `/api/v1/auth/register` with valid credentials
  - Test POST `/api/v1/auth/login` with registered user
  - Test protected endpoints without token (should return 401)
  - Test protected endpoints with valid token (should return 200)
- [ ] **Optional enhancements:**
  - Add AuthenticationException handler in GlobalExceptionHandler
  - Add token refresh endpoint
  - Add logout (client-side only for stateless JWT)
- [ ] **Merge to develop:**
  - Final commit and push to `feature/spring-security`
  - Merge feature branch to develop (--no-ff)
  - Tag release v0.3.0
  - Push to GitHub

**Estimated Time for Testing:** 30 minutes - 1 hour (on computer with PostgreSQL)

---

## 🎉 Releases

### v0.2.0 - REST API Complete (October 20, 2025)

- Professional exception handling
- Ready for security layer implementation

### ✅ Phase 1: Project Setup & Database (COMPLETED - Oct 12, 2025)

#### 1.1 PostgreSQL Database ✅

- **Installed:** PostgreSQL 16 on Windows
- **Database created:** `nexusfi`
- **Schema executed:** All 6 tables created successfully
  - `users`
  - `categories` (hierarchical with parent/child)
  - `income_records`
  - `expense_records`
  - `transfers`
  - `movements` (central ledger)
- **Additional objects:**
  - 27 indexes for performance
  - 6 triggers (auto-update `updated_at` timestamps)
  - 3 functions (helper functions for business logic)
  - 2 views (category hierarchy and balance summary)
- **Test user:** `user@nexusfi.com` / `password123`

**Key Learning:**

- Understood SQL functions (procedural code in database)
- Understood SQL triggers (automatic event handlers)
- Understood SQL views (saved queries as virtual tables)
- Learned difference between source code (`src/`) and compiled code (`target/`)

#### 1.2 Project Structure ✅

```
NexusFi/
├── docs/                  # Documentation
├── database/              # SQL scripts
│   └── schema.sql
├── src/
│   └── main/
│       ├── java/
│       │   └── com/nexusfi/model/
│       │       ├── User.java
│       │       ├── Category.java
│       │       ├── IncomeRecord.java
│       │       ├── ExpenseRecord.java
│       │       ├── Transfer.java
│       │       ├── Movement.java
│       │       └── enums/
│       │           └── MovementType.java
│       └── resources/
│           └── application.yml
├── pom.xml
├── README.md
└── requirements.md
```

#### 1.3 JPA Entities ✅

All entity classes created with:

- Proper JPA annotations (@Entity, @Table, @Column)
- Relationships (@OneToMany, @ManyToOne, @ManyToOne)
- Constraints (foreign keys, unique constraints)
- Validation (@NotNull, @NotBlank, @Email, etc.)
- Audit timestamps (@PrePersist, @PreUpdate)
- Helper methods for common operations
- Lombok annotations to reduce boilerplate

**Key Learning:**

- Entities are in `src/` (source code you edit)
- `target/` contains compiled `.class` files (generated, ignored by Git)
- `target/` can be deleted anytime with `mvn clean`

#### 1.4 Git & GitHub ✅

- **Repository initialized:** Local Git repo
- **Connected to GitHub:** https://github.com/williamsjmzhdz/NexusFi
- **Branches:**
  - `main` - Stable code
  - `develop` - Active development (current branch)
- **Initial commit:** All foundational code committed and pushed
- **`.gitignore` configured:** Properly ignoring `target/`, IDE files, etc.

**Key Learning:**

- Git workflow: add → commit → push
- Branch strategy: main (stable) + develop (work in progress)
- Understanding `.gitignore` (don't version compiled/generated files)

---

## ✅ Current Task: COMPLETED - Spring Boot Application Setup

### ✨ What We Accomplished (October 15, 2025):

**Created Spring Boot Application:**

- ✅ `NexusFiApplication.java` - Main application class written by Willy
- ✅ Fixed duplicate lifecycle callbacks in `Transfer.java`
- ✅ Created `application-dev.yml` for development configuration
- ✅ Configured environment variables for database credentials (professional approach)
- ✅ Set up `CamelCaseToUnderscoresNamingStrategy` for proper database mapping

**Development Tools:**

- ✅ Created `scripts/` folder with convenient run scripts
  - `run-dev.bat` / `run-dev.ps1` - Start app with one command
  - `build.bat` - Clean and compile project
  - `package.bat` - Package as JAR
- ✅ Maven added to PATH (no more full path needed)

**Testing & Validation:**

- ✅ Application starts successfully in 2.7 seconds
- ✅ Connects to PostgreSQL database `nexusfi`
- ✅ Schema validation passes
- ✅ All entities load correctly
- ✅ Tomcat server running on port 8080
- ✅ Spring Security enabled (login page works)

**Git Workflow:**

- ✅ Feature branch created: `feature/spring-boot-setup`
- ✅ Comprehensive commit with 7 files changed
- ✅ Pushed to GitHub

**Key Learning:**

- Professional use of environment variables for credentials
- Maven lifecycle and commands
- Spring Boot auto-configuration
- JPA lifecycle callbacks (@PrePersist, @PreUpdate)
- Git feature branch workflow

---

## ✅ Phase 2: Repository Layer (COMPLETED - Oct 18, 2025)

### ✨ What We Accomplished:

**Created 6 Spring Data JPA Repository Interfaces:**

- ✅ `UserRepository` - User authentication and lookup
- ✅ `CategoryRepository` - Category CRUD with percentage calculations
- ✅ `IncomeRecordRepository` - Income records with date filtering
- ✅ `ExpenseRecordRepository` - Expense records by user and category
- ✅ `TransferRepository` - Transfer operations
- ✅ `MovementRepository` - Unified transaction history (read-only view)

**Key Features:**

- Query methods derived from method names (Spring magic!)
- Custom JPQL queries with @Query annotation
- Date range filtering
- No SQL needed - Spring generates implementation

**Git Workflow:**

- ✅ Feature branch: `feature/repository-layer`
- ✅ Merged to `develop` with `--no-ff`
- ✅ Branch cleaned up (local and remote deleted)
- ✅ Pushed to GitHub

**Key Learning:**

- Spring Data JPA method naming conventions
- `findByUserIdOrderByRecordedAtDesc` → automatic SQL generation
- Custom @Query for complex operations (SUM aggregations)
- Repository pattern benefits

---

## ✅ Phase 3: Service Layer (COMPLETED - Oct 19, 2025)

### ✨ What We Accomplished:

**Created 6 Service Classes with Business Logic:**

1. ✅ `UserService` - User registration, email validation
2. ✅ `CategoryService` - **Core percentage validation** (must sum to 100%)
3. ✅ `IncomeService` - **Auto-distribution algorithm** (splits income across categories)
4. ✅ `ExpenseService` - Balance validation before spending
5. ✅ `TransferService` - Zero-sum transfers between categories
6. ✅ `MovementService` - Read-only transaction history queries

**Professional Exception Handling:**

- ✅ Custom exception hierarchy (`NexusFiException` base class)
- ✅ `ResourceNotFoundException` → HTTP 404
- ✅ `DuplicateResourceException` → HTTP 409 Conflict
- ✅ `InsufficientBalanceException` → HTTP 400
- ✅ `InvalidPercentageException` → HTTP 400
- ✅ `GlobalExceptionHandler` - Centralized error responses with proper HTTP status codes

**Git Workflow:**

- ✅ Feature branch: `feature/service-layer`
- ✅ Multiple commits (services, then exception refactor)
- ✅ Merged to `develop` with `--no-ff`
- ✅ Branch cleaned up
- ✅ **Total:** 846 lines of business logic added!

**Key Learning:**

- Service layer architecture (@Service, @Transactional)
- Constructor injection (best practice)
- Business rule enforcement in services
- BigDecimal arithmetic for financial calculations
- Income distribution algorithm with rounding error handling
- Professional exception handling vs generic IllegalArgumentException
- @RestControllerAdvice for global exception handling

---

## ✅ Phase 4: REST Controller Layer (COMPLETED - Oct 20, 2025)

### ✨ What We Accomplished:

**Created 5 REST Controllers with 21 Endpoints:**

1. ✅ `CategoryController` (6 endpoints) - Full CRUD + remaining percentage query
2. ✅ `IncomeController` (3 endpoints) - Record and query income
3. ✅ `ExpenseController` (4 endpoints) - Record and query expenses with category filtering
4. ✅ `TransferController` (4 endpoints) - Execute zero-sum transfers between categories
5. ✅ `MovementController` (4 endpoints) - Read-only transaction history with filtering

**Created 11 DTO Classes:**

- Request DTOs: CategoryRequest, IncomeRequest, ExpenseRequest, TransferRequest
- Response DTOs: CategoryResponse, IncomeResponse, ExpenseResponse, TransferResponse, MovementResponse
- 2 additional DTOs for CategoryController

**Key Features Implemented:**

- ✅ Input validation with @Valid and Bean Validation annotations
- ✅ Proper HTTP status codes (200 OK, 201 Created, 204 No Content, 404 Not Found)
- ✅ RESTful endpoint design following best practices
- ✅ CategoryName embedding in responses (reduces frontend API calls)
- ✅ Consistent patterns across all controllers
- ✅ Constructor injection for dependencies
- ✅ Stream API for entity-to-DTO transformations
- ✅ TODO comments for Spring Security integration

**Total Code:** 1,229 lines across 16 files

**Git Workflow:**

- ✅ Feature branch: `feature/controller-layer`
- ✅ Multiple commits (CategoryController first, then remaining 4 controllers)
- ✅ All code pushed to GitHub
- ⏳ Ready to merge to `develop`

**Key Learning:**

- **REST API Design Principles:**
  - Resources (nouns) not actions (verbs)
  - HTTP methods for CRUD (GET, POST, PUT, DELETE)
  - Status codes communicate operation results
  - URLs should be intuitive and predictable
- **DTOs vs Entities:**
  - Security: Don't expose passwords, internal fields
  - Flexibility: Different shapes for input vs output
  - Decoupling: Change entity without breaking API
- **@RestController vs @Controller:**
  - @RestController = @Controller + @ResponseBody
  - Returns JSON automatically via Jackson
  - Perfect for modern SPAs and mobile apps
- **Zero-Sum Operations:**
  - Transfers preserve total balance
  - Source decreases, destination increases
  - No money created or destroyed
- **Read-Only Transaction History:**
  - Movements created automatically by system
  - Provides audit trail of all operations
  - Cannot be created or modified directly

---

## 🎉 Releases

### v0.2.0 - REST API Complete (October 20, 2025)

**Status:** Released and tagged on GitHub

**What's Included:**

- 5 REST Controllers (21 endpoints total)
  - CategoryController: 6 endpoints
  - IncomeController: 3 endpoints
  - ExpenseController: 4 endpoints
  - TransferController: 4 endpoints
  - MovementController: 4 endpoints
- 11 DTO classes (Request + Response)
- Professional exception handling
- Input validation with Bean Validation
- Complete backend API (~3,300 lines of code)

**GitHub Release:** [v0.2.0](https://github.com/williamsjmzhdz/NexusFi/releases/tag/v0.2.0)

**Known Limitations:**

- No authentication (endpoints unsecured)
- Not production-ready
- Hardcoded user ID in controllers

---

### v0.1.0 - Spring Boot + Database (October 16-18, 2025)

**Status:** Released and tagged on GitHub

**What's Included:**

- PostgreSQL database with complete schema
- 7 JPA entities with relationships
- Spring Boot application setup
- 6 Repository interfaces
- 6 Service classes (846 lines)
- 6 Custom exception classes
- GlobalExceptionHandler

**GitHub Release:** [v0.1.0](https://github.com/williamsjmzhdz/NexusFi/releases/tag/v0.1)

---

## 🚧 Next Tasks: Phase 5 Completion

### Phase 5: Security Configuration (IN PROGRESS for v0.3)

**Goal:** Implement JWT-based authentication

- ✅ Spring Security and JWT dependencies
- ✅ CustomUserDetails wrapper class
- ✅ JWT token generation and validation utility
- ✅ Authentication DTOs
- 🔄 AuthController (login, register endpoints) - **NEXT**
- [ ] JWT authentication filter
- [ ] Security configuration
- [ ] Password encryption with BCrypt
- [ ] Public vs protected endpoints

---

## 📋 Roadmap: What's Next After Spring Boot Setup

### Phase 2: Repositories (Spring Data JPA) ✅ COMPLETED

**See above for completion details**

---

### Phase 3: Services (Business Logic) ✅ COMPLETED

**See above for completion details**

---

### Phase 4: REST Controllers 🔄 IN PROGRESS

**See "Phase 4" section above for current status**

---

### Phase 4: Controllers (REST API)

**Goal:** Expose functionality via HTTP endpoints

- [ ] AuthController (login/logout)
- [ ] CategoryController (CRUD operations)
- [ ] IncomeController (register income, list)
- [ ] ExpenseController (register expense, list)
- [ ] TransferController (create transfers)
- [ ] DashboardController (summary stats)

**Learning focus:**

- REST principles
- HTTP methods (GET, POST, PUT, DELETE)
- Request/Response DTOs
- Status codes and error handling

---

### Phase 5: Security

**Goal:** Secure the application

- [ ] Spring Security configuration
- [ ] Login functionality
- [ ] Session management
- [ ] Password encryption (BCrypt)
- [ ] Protect all endpoints except login

**Learning focus:**

- Authentication vs Authorization
- Spring Security filters
- SecurityContext
- Password hashing

---

### Phase 6: Frontend (UI)

**Goal:** User interface for the application

**Options to decide:**

1. Thymeleaf (server-side templates)
2. React/Vue (SPA - Single Page Application)
3. Simple HTML + vanilla JS

**Features:**

- Login page
- Dashboard (balance overview)
- Category management (tree view)
- Register income
- Register expenses
- Transfer between categories
- Reports and charts

---

### Phase 7: Testing

**Goal:** Ensure quality and reliability

- [ ] Unit tests (services)
- [ ] Integration tests (repositories)
- [ ] API tests (controllers)
- [ ] End-to-end scenarios

---

### Phase 8: Deployment

**Goal:** Run in production

- [ ] Package as JAR
- [ ] Production configuration
- [ ] Deploy strategy (local/cloud)
- [ ] Monitoring and logging

---

## 🎯 Learning Goals for This Project

By completing NexusFi, Willy will learn:

### Backend

- ✅ PostgreSQL database design and SQL
- ✅ JPA/Hibernate entities and relationships
- ✅ Git version control and GitHub
- ✅ Spring Boot framework
- ✅ Spring Data JPA (repositories)
- ✅ Service layer architecture
- ✅ REST API design
- � Spring Security (in progress)
- � JWT authentication (in progress)
- ✅ Exception handling
- 🔜 Transaction management
- 🔜 Testing (JUnit, Mockito)

### Professional Skills

- ✅ Project organization and structure
- ✅ Documentation practices
- ✅ Clean code principles
- 🔜 Design patterns
- 🔜 Best practices for Java/Spring
- 🔜 Debugging techniques
- 🔜 Performance optimization

---

## 📝 Important Technical Details

### Database Connection Info

- **Host:** localhost
- **Port:** 5432
- **Database:** nexusfi
- **Username:** postgres
- **Password:** [stored locally, not in Git]
- **Test User:** user@nexusfi.com / password123

### Application Configuration

- **Port:** 8080 (default)
- **Profile:** dev (for development)
- **Java Version:** 17
- **Spring Boot Version:** 3.2.0
- **Build Tool:** Maven

### Business Rules to Remember

1. **Accounting Integrity:** Money only enters via income, exits via expenses
2. **Allocation Integrity:** Sibling categories MUST sum to 100%
3. **Archive Requirements:** Balance = 0 AND percentage = 0
4. **Zero-Sum Operations:** Transfers and rebalances don't change total

---

## 🤔 Questions & Clarifications Needed

_None currently - ready to continue development_

---

## 🔄 How to Resume in Next Session

### 📢 Message to Start New Chat Session:

Copy and paste this message when opening a new Copilot chat:

```
Hi! I'm developing NexusFi (Spring Boot + PostgreSQL).
Please read PROGRESS.md to understand the full context.
I need you to act as my mentor (Tech Lead) guiding me step
by step, explaining concepts before providing code, not just
giving complete solutions.

Phase 5 (Spring Security + JWT) is complete but needs testing.
Next step: Test authentication on computer with PostgreSQL.

Current branch: feature/spring-security
```

### For Copilot (Acting as Mentor):

**Context Summary:**
Willy is building NexusFi, a personal finance app with Spring Boot + PostgreSQL. He's learning hands-on with step-by-step guidance. 

**Completed (Nov 3, 2025):**
- Database schema with 6 tables (PostgreSQL)
- 7 JPA entities with relationships
- 6 Repository interfaces (Spring Data JPA)
- 6 Service classes with business logic (846 lines)
- 5 REST Controllers with 21 endpoints (1,229 lines)
- Professional exception handling (GlobalExceptionHandler)
- **Phase 5 - Spring Security + JWT (IMPLEMENTATION COMPLETE):**
  - CustomUserDetails (UserDetails adapter)
  - JwtUtil (token generation/validation)
  - CustomUserDetailsService (loads users from DB)
  - JwtAuthenticationFilter (intercepts requests, validates tokens)
  - SecurityConfig (PasswordEncoder, AuthenticationManager, SecurityFilterChain)
  - AuthController (register/login endpoints)
  - UserService updated with BCrypt password hashing
  - JWT config in application.yml (fixed placement to root level)
  - Build successful: `mvn clean package -DskipTests` ✅

**Current Branch:** `feature/spring-security`

**Pending Tasks:**
1. **Smoke testing** (requires computer with PostgreSQL):
   - Test POST /api/v1/auth/register
   - Test POST /api/v1/auth/login
   - Test protected endpoints with/without JWT token
2. **Commit and push** all Phase 5 changes
3. **Merge to develop** and tag v0.3.0

**Mentor Guidance Needed:**
Please continue acting as mentor - guide step-by-step with explanations. Help with testing setup (Postman/HTTP client), explain expected responses, and guide through merge/tag workflow. Keep explanations concise but clear.

### Alternative Opening Messages:

**Option 1 - Direct and concise:**

```
"Hi! Please read PROGRESS.md and help me continue NexusFi
development. Act as my mentor, guide step-by-step."
```

**Option 2 - With more context:**

```
"Hi! I'm continuing development of NexusFi. Phase 5 (Spring Security + JWT)
is implemented but needs testing. Please guide me step-by-step as my mentor.
Current branch: feature/spring-security"
```

### Quick Git Status Check:

```bash
git status
git branch
git log --oneline -5
```

---

## 🛠️ Useful Commands Reference

### Git Commands

```bash
# Check status
git status

# See branches
git branch -a

# Create feature branch
git checkout -b feature/name

# Add and commit
git add .
git commit -m "Description"

# Push to GitHub
git push

# Merge feature to develop
git checkout develop
git merge feature/name
git push
```

### Maven Commands

```bash
# Clean compiled files
mvn clean

# Compile
mvn compile

# Run Spring Boot
mvn spring-boot:run

# Package as JAR
mvn package

# Run tests
mvn test
```

### PostgreSQL Commands

```bash
# Connect to database
psql -U postgres -d nexusfi

# Inside psql:
\dt                    # List tables
\d table_name          # Describe table
\dv                    # List views
\df                    # List functions
\q                     # Quit
```

---

## 📚 Documentation & Resources

- **Project Requirements:** `requirements.md`
- **Data Model Details:** `docs/DATA_MODEL.md`
- **Quick Reference:** `docs/QUICK_REFERENCE.md`
- **Database Schema:** `database/schema.sql`
- **README:** `README.md`

---

## 💡 Notes & Insights

### Key Learnings So Far:

1. **SQL Functions are like Java methods but run in the database**

   - Better performance for data-heavy operations
   - Always available regardless of application

2. **Triggers are automatic event handlers**

   - Execute on INSERT/UPDATE/DELETE
   - Great for audit fields (like `updated_at`)
   - Can't be "forgotten" - always run

3. **Views are saved queries**

   - Act like tables but don't store data
   - Simplify complex queries
   - Great for reporting

4. **`src/` vs `target/`**

   - `src/` = Your source code (edit this)
   - `target/` = Compiled bytecode (never edit)
   - `target/` can be deleted and regenerated

5. **Git ignores generated files**
   - Don't version compiled code
   - Don't version IDE settings
   - Do version source code and configuration

---

## ⚠️ Known Issues & Blockers

_None currently_

---

## 🎓 Learning Resources Used

- PostgreSQL Official Documentation
- Spring Boot Documentation
- Spring Data JPA Documentation
- Git Documentation
- Stack Overflow (for specific issues)

---

**End of Progress Document**

_This file should be updated after each major milestone or at the end of each development session._

---

## 🚀 Ready to Continue?

When you return:

1. Read this document
2. Check Git status
3. Open a new Copilot chat
4. Provide context and ask for guidance
5. Continue learning and building! 💪
