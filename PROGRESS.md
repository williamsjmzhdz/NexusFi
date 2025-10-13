# NexusFi - Development Progress & Learning Journey

**Last Updated:** October 12, 2025  
**Developer:** Francisco (williamsjmzhdz)  
**Learning Approach:** Hands-on, step-by-step, with mentor guidance

---

## 🎓 Important: Learning Philosophy

This project is being developed as a **hands-on learning experience**. The approach is:

- ✅ **Step-by-step guidance** - One feature at a time
- ✅ **Learn by doing** - Francisco writes the code himself
- ✅ **Mentor-style support** - Copilot acts as Tech Lead/Mentor, not code generator
- ✅ **Explanations first** - Understanding the "why" before the "what"
- ✅ **No code dumps** - Guide, review, and help debug, don't just provide complete solutions

### 🤝 Copilot's Role as Mentor

When continuing in a new chat session, please:

1. **Act as a Tech Lead/Senior Developer**, not just a code assistant
2. **Explain concepts** before providing code
3. **Guide step-by-step** - break down complex tasks
4. **Review code** Francisco writes and suggest improvements
5. **Answer questions** about why things work the way they do
6. **Help debug** when stuck, but let Francisco solve problems
7. **Encourage best practices** and professional development habits

**DO NOT:**

- ❌ Generate entire features without explanation
- ❌ Dump large code blocks without context
- ❌ Skip explanations of commands/concepts
- ❌ Move too fast without checking understanding

---

## 📍 Current Status: Database Setup Complete

We are at the **beginning of the Spring Boot application development**. All foundational work is complete.

### ✅ Phase 1: Project Setup & Database (COMPLETED)

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

## 🚧 Current Task: NOT STARTED

### Next Step: Create Spring Boot Application Class

**What needs to be done:**
Create `NexusFiApplication.java` - the main entry point for Spring Boot.

**Location:** `src/main/java/com/nexusfi/NexusFiApplication.java`

**This will:**

1. Bootstrap the Spring Boot application
2. Enable auto-configuration
3. Allow the app to start and connect to PostgreSQL
4. Serve as the foundation for all other components

**Recommended approach for next session:**

1. Create a feature branch: `git checkout -b feature/spring-boot-setup`
2. Guide Francisco to create the main application class
3. Explain each annotation (@SpringBootApplication, @ComponentScan, etc.)
4. Configure database connection in `application.yml`
5. Test that the app starts successfully
6. Verify database connection works
7. Commit and merge to develop

---

## 📋 Roadmap: What's Next After Spring Boot Setup

### Phase 2: Repositories (Spring Data JPA)

**Goal:** Create repository interfaces for database access

- [ ] UserRepository
- [ ] CategoryRepository
- [ ] IncomeRecordRepository
- [ ] ExpenseRecordRepository
- [ ] TransferRepository
- [ ] MovementRepository

**Learning focus:**

- Spring Data JPA magic (no SQL needed)
- Query methods by naming convention
- Custom @Query annotations
- Repository patterns

---

### Phase 3: Services (Business Logic)

**Goal:** Implement core business rules

- [ ] CategoryService
  - Create/edit categories
  - Validate percentage sums (must = 100%)
  - Archive/unarchive logic
  - Rebalance percentages
- [ ] IncomeService
  - Register income
  - Automatic distribution algorithm
  - Create ASSIGNMENT movements
- [ ] ExpenseService
  - Register expenses
  - Update category balances
  - Create EXPENSE movements
- [ ] TransferService
  - Zero-sum transfers
  - Create two TRANSFER movements
  - Validate source ≠ destination

**Learning focus:**

- Service layer patterns
- Transaction management (@Transactional)
- Business rule validation
- Exception handling

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

By completing NexusFi, Francisco will learn:

### Backend

- ✅ PostgreSQL database design and SQL
- ✅ JPA/Hibernate entities and relationships
- ✅ Git version control and GitHub
- 🚧 Spring Boot framework
- 🔜 Spring Data JPA (repositories)
- 🔜 Service layer architecture
- 🔜 REST API design
- 🔜 Spring Security
- 🔜 Exception handling
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

I want to continue from where I left off. The next step is
to create NexusFiApplication.java (main Spring Boot class).

Current branch: develop
```

### For Copilot (Acting as Mentor):

**Context Summary:**
Francisco is building NexusFi, a personal finance app with Spring Boot + PostgreSQL. He's learning hands-on with step-by-step guidance. Database is ready, entities are created. Next task is creating the main Spring Boot application class to start the app. Please act as mentor - guide, don't just provide code. Explain each step.

### Alternative Opening Messages:

**Option 1 - Direct and concise:**

```
"Hi! Please read PROGRESS.md and help me continue NexusFi
development. Act as my mentor, guide step-by-step."
```

**Option 2 - With more context:**

```
"Hi! I'm continuing development of NexusFi. I need to create the
main Spring Boot application class (NexusFiApplication.java).
Please guide me step-by-step as my mentor. Current branch: develop"
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
