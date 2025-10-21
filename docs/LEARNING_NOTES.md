# NexusFi - Learning Notes & Reference Guide

> **Personal study guide for Francisco Williams Jiménez Hernández**  
> Everything learned up to v0.2.0 (Complete REST API)  
> Last updated: October 21, 2025

---

## 📑 Table of Contents

1. [Project Overview](#project-overview)
2. [Spring Boot Fundamentals](#spring-boot-fundamentals)
3. [JPA & Hibernate (Database Layer)](#jpa--hibernate-database-layer)
4. [Repository Layer](#repository-layer)
5. [Service Layer](#service-layer)
6. [Controller Layer (REST API)](#controller-layer-rest-api)
7. [Exception Handling](#exception-handling)
8. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
9. [Maven Commands](#maven-commands)
10. [Git Workflow](#git-workflow)
11. [Configuration Files](#configuration-files)
12. [Quick Command Reference](#quick-command-reference)

---

## 🎯 Project Overview

**NexusFi** = Personal finance management system (Spring Boot + PostgreSQL)

**Architecture (Layered):**

```
┌─────────────────────────┐
│   Controller Layer      │ ← REST API endpoints (HTTP)
├─────────────────────────┤
│   Service Layer         │ ← Business logic
├─────────────────────────┤
│   Repository Layer      │ ← Database access (JPA)
├─────────────────────────┤
│   Entity Layer          │ ← Java objects (tables)
├─────────────────────────┤
│   PostgreSQL Database   │ ← Data storage
└─────────────────────────┘
```

**Current Status (v0.2.0):**

- ✅ 7 Entities
- ✅ 6 Repositories
- ✅ 6 Services
- ✅ 5 Controllers (21 endpoints)
- ✅ 11 DTOs
- ✅ Exception handling

---

## 🌱 Spring Boot Fundamentals

### Main Application Class

```java
@SpringBootApplication  // ← Combines 3 annotations!
public class NexusFiApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexusFiApplication.class, args);
    }
}
```

**What @SpringBootApplication does:**

1. `@Configuration` - Makes this class a configuration source
2. `@EnableAutoConfiguration` - Auto-configures based on dependencies
3. `@ComponentScan` - Scans package for components

### Component Scanning

Spring automatically finds and registers:

- `@Entity` classes
- `@Repository` interfaces
- `@Service` classes
- `@Controller` / `@RestController` classes

**Rule:** Main class should be in **root package** (`com.nexusfi`)

---

## 🗄️ JPA & Hibernate (Database Layer)

### Entity Basics

```java
@Entity                                    // ← JPA annotation: This is a table
@Table(name = "users")                    // ← Table name in database
public class User {

    @Id                                   // ← Primary key
    @GeneratedValue(strategy = IDENTITY)  // ← Auto-increment
    @Column(name = "user_id")             // ← Column name
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### Key Annotations

| Annotation             | Purpose                       | Example                                  |
| ---------------------- | ----------------------------- | ---------------------------------------- |
| `@Entity`              | Marks class as database table | Required on all entities                 |
| `@Table(name = "...")` | Specify table name            | `@Table(name = "users")`                 |
| `@Id`                  | Primary key                   | On `userId` field                        |
| `@GeneratedValue`      | Auto-generate ID              | `strategy = IDENTITY` for auto-increment |
| `@Column`              | Column properties             | `nullable`, `unique`, `length`           |
| `@Enumerated`          | Store enum in DB              | `@Enumerated(EnumType.STRING)`           |
| `@ManyToOne`           | Foreign key (many-to-one)     | Category → User                          |
| `@OneToMany`           | One-to-many relationship      | User → Categories                        |
| `@JoinColumn`          | Specify FK column name        | `@JoinColumn(name = "user_id")`          |

### Relationship Mappings

#### Many-to-One (Category → User)

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

#### Self-Referencing (Category → Parent Category)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_category_id")
private Category parentCategory;

@OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
private List<Category> subcategories = new ArrayList<>();
```

### Fetch Types

- `FetchType.LAZY` - Load only when accessed (preferred for performance)
- `FetchType.EAGER` - Load immediately with parent entity

### Cascade Types

```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Category> categories;
```

- `CascadeType.ALL` - All operations cascade (persist, merge, remove, etc.)
- `CascadeType.PERSIST` - Only cascade persist
- `CascadeType.REMOVE` - Only cascade delete

---

## 📚 Repository Layer

### Spring Data JPA Magic

```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Method name → SQL query (Spring generates implementation!)
    List<Category> findByUserId(Long userId);

    // More complex query
    List<Category> findByUserIdAndParentCategoryIsNull(Long userId);

    // Custom JPQL query
    @Query("SELECT c FROM Category c WHERE c.user.userId = :userId " +
           "AND c.parentCategory IS NULL ORDER BY c.name")
    List<Category> findRootCategories(@Param("userId") Long userId);

    // Aggregation query
    @Query("SELECT COALESCE(SUM(c.percentage), 0) FROM Category c " +
           "WHERE c.user.userId = :userId AND c.parentCategory IS NULL")
    BigDecimal sumRootCategoryPercentages(@Param("userId") Long userId);
}
```

### Method Naming Convention

| Method Name                                                       | Generated SQL                         |
| ----------------------------------------------------------------- | ------------------------------------- |
| `findByUserId(Long userId)`                                       | `WHERE user_id = ?`                   |
| `findByUserIdOrderByNameAsc(Long id)`                             | `WHERE user_id = ? ORDER BY name ASC` |
| `findByAmountGreaterThan(BigDecimal amount)`                      | `WHERE amount > ?`                    |
| `findByRecordedAtBetween(LocalDateTime start, LocalDateTime end)` | `WHERE recorded_at BETWEEN ? AND ?`   |
| `existsByEmail(String email)`                                     | `SELECT COUNT(*) > 0 WHERE email = ?` |

### Repository Methods (JpaRepository provides)

```java
// Save or update
category = repository.save(category);

// Find by ID
Optional<Category> cat = repository.findById(1L);

// Find all
List<Category> all = repository.findAll();

// Delete
repository.deleteById(1L);
repository.delete(category);

// Check existence
boolean exists = repository.existsById(1L);

// Count
long count = repository.count();
```

---

## 🛠️ Service Layer

### Service Class Structure

```java
@Service                                    // ← Spring manages this bean
@Transactional                             // ← All methods in a transaction
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Constructor injection (best practice)
    public CategoryService(CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Category createCategory(Long userId, String name,
                                   BigDecimal percentage, Long parentId) {
        // 1. Validation
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with id: " + userId));

        // 2. Business logic
        if (percentage.compareTo(BigDecimal.ZERO) < 0 ||
            percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidPercentageException(
                "Percentage must be between 0 and 100");
        }

        // 3. Save and return
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setPercentage(percentage);

        return categoryRepository.save(category);
    }
}
```

### Key Concepts

**@Service vs @Component:**

- `@Service` - Specialized `@Component` for business logic layer
- Makes code more readable (semantic meaning)

**@Transactional:**

- Wraps method in database transaction
- Auto-commits on success, rolls back on exception
- Can be at class level (all methods) or method level

**Constructor Injection vs Field Injection:**

```java
// ✅ GOOD: Constructor injection (recommended)
private final CategoryRepository repository;
public CategoryService(CategoryRepository repository) {
    this.repository = repository;
}

// ❌ AVOID: Field injection
@Autowired
private CategoryRepository repository;
```

**Why constructor injection?**

- Immutable (final fields)
- Easier to test
- Makes dependencies explicit

### Business Logic Examples

#### Income Distribution Algorithm

```java
// Distribute $1000 across categories
// Savings (50%) → $500.00
// Expenses (30%) → $300.00
// Entertainment (20%) → $200.00

List<Category> categories = categoryRepository
    .findByUserIdAndParentCategoryIsNull(userId);

BigDecimal remainingAmount = amount;
List<IncomeRecord> records = new ArrayList<>();

for (Category category : categories) {
    BigDecimal categoryAmount = amount
        .multiply(category.getPercentage())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

    // Handle rounding error in last category
    if (category.equals(categories.get(categories.size() - 1))) {
        categoryAmount = remainingAmount;
    }

    // Create income record
    IncomeRecord record = new IncomeRecord();
    record.setAmount(categoryAmount);
    record.setCategory(category);
    records.add(record);

    remainingAmount = remainingAmount.subtract(categoryAmount);
}
```

#### Balance Validation

```java
public void recordExpense(Long userId, Long categoryId, BigDecimal amount) {
    Category category = findCategoryById(categoryId);

    // Validate sufficient balance
    if (category.getBalance().compareTo(amount) < 0) {
        throw new InsufficientBalanceException(
            "Insufficient balance in category: " + category.getName() +
            ". Available: " + category.getBalance() +
            ", Required: " + amount);
    }

    // Deduct from balance
    category.setBalance(category.getBalance().subtract(amount));
    categoryRepository.save(category);
}
```

---

## 🌐 Controller Layer (REST API)

### REST Controller Basics

```java
@RestController                                    // ← @Controller + @ResponseBody
@RequestMapping("/api/v1/categories")             // ← Base URL
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/v1/categories?userId=1
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam Long userId) {
        // ...
        return ResponseEntity.ok(responses);       // ← 200 OK
    }

    // POST /api/v1/categories
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        // ...
        return ResponseEntity.status(HttpStatus.CREATED)  // ← 201 Created
                             .body(response);
    }

    // PUT /api/v1/categories/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        // ...
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();  // ← 204 No Content
    }
}
```

### HTTP Methods & Status Codes

| Method   | Purpose                 | Success Status   | Example                   |
| -------- | ----------------------- | ---------------- | ------------------------- |
| `GET`    | Read data               | `200 OK`         | Get categories list       |
| `POST`   | Create new resource     | `201 Created`    | Create category           |
| `PUT`    | Update entire resource  | `200 OK`         | Update category           |
| `PATCH`  | Update partial resource | `200 OK`         | Update category name only |
| `DELETE` | Remove resource         | `204 No Content` | Delete category           |

### Common Status Codes

| Code                        | Meaning                   | When to Use           |
| --------------------------- | ------------------------- | --------------------- |
| `200 OK`                    | Success                   | GET, PUT requests     |
| `201 Created`               | Resource created          | POST requests         |
| `204 No Content`            | Success, no response body | DELETE requests       |
| `400 Bad Request`           | Invalid input             | Validation errors     |
| `404 Not Found`             | Resource doesn't exist    | ID not found          |
| `409 Conflict`              | Duplicate resource        | Email already exists  |
| `500 Internal Server Error` | Server error              | Unexpected exceptions |

### Key Annotations

| Annotation        | Purpose                | Example                            |
| ----------------- | ---------------------- | ---------------------------------- |
| `@RestController` | REST API controller    | Class level                        |
| `@RequestMapping` | Base URL path          | `@RequestMapping("/api/v1/users")` |
| `@GetMapping`     | Handle GET requests    | Read operations                    |
| `@PostMapping`    | Handle POST requests   | Create operations                  |
| `@PutMapping`     | Handle PUT requests    | Update operations                  |
| `@DeleteMapping`  | Handle DELETE requests | Delete operations                  |
| `@RequestParam`   | Query parameter        | `?userId=1`                        |
| `@PathVariable`   | URL path variable      | `/users/{id}`                      |
| `@RequestBody`    | JSON body to object    | POST/PUT data                      |
| `@Valid`          | Trigger validation     | With `@RequestBody`                |

### Request/Response Flow

```
Client Request:
POST /api/v1/categories
Content-Type: application/json

{
  "userId": 1,
  "name": "Groceries",
  "percentage": 30.00
}

↓

@PostMapping + @Valid + @RequestBody
↓
CategoryRequest (DTO) validated
↓
Service layer (business logic)
↓
Repository layer (database)
↓
Entity → DTO transformation
↓
ResponseEntity<CategoryResponse>

↓

Server Response:
HTTP/1.1 201 Created
Content-Type: application/json

{
  "categoryId": 5,
  "name": "Groceries",
  "percentage": 30.00,
  "balance": 0.00,
  "createdAt": "2025-10-21T10:30:00"
}
```

---

## ❌ Exception Handling

### Custom Exception Hierarchy

```
NexusFiException (base)
├── ResourceNotFoundException (404)
├── DuplicateResourceException (409)
├── InsufficientBalanceException (400)
└── InvalidPercentageException (400)
```

### Exception Classes

```java
// Base exception
public class NexusFiException extends RuntimeException {
    public NexusFiException(String message) {
        super(message);
    }
}

// Specific exceptions
public class ResourceNotFoundException extends NexusFiException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class InsufficientBalanceException extends NexusFiException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice  // ← Handles exceptions globally
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)    // 404
            .body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)     // 409
            .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPercentageException.class)
    public ResponseEntity<String> handleInvalidPercentage(InvalidPercentageException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)  // 400
            .body(ex.getMessage());
    }
}
```

### Usage in Service

```java
// Throw custom exception
User user = userRepository.findById(userId)
    .orElseThrow(() -> new ResourceNotFoundException(
        "User not found with id: " + userId));

// Check for duplicate
if (userRepository.existsByEmail(email)) {
    throw new DuplicateResourceException(
        "Email already registered: " + email);
}

// Validate business rule
if (balance.compareTo(amount) < 0) {
    throw new InsufficientBalanceException(
        "Insufficient balance. Available: " + balance);
}
```

---

## 📦 DTOs (Data Transfer Objects)

### Why Use DTOs?

**Problem:** Exposing entities directly

```java
// ❌ BAD: Exposes password hash, internal IDs
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).get();  // Returns entire User entity!
}
```

**Solution:** Use DTOs

```java
// ✅ GOOD: Only exposes what's needed
@GetMapping("/users/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    UserResponse response = new UserResponse(user);  // DTO
    return ResponseEntity.ok(response);
}
```

### DTO Benefits

1. **Security** - Don't expose sensitive fields (password, internal IDs)
2. **Flexibility** - Different shapes for input vs output
3. **Decoupling** - Change entity without breaking API
4. **Validation** - Input validation with Bean Validation

### Request DTO Example

```java
public class CategoryRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 50, message = "Name must be 1-50 characters")
    private String name;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    private BigDecimal percentage;

    private Long parentCategoryId;  // Optional

    // Getters and setters
}
```

### Response DTO Example

```java
public class CategoryResponse {
    private Long categoryId;
    private String name;
    private BigDecimal percentage;
    private BigDecimal balance;
    private String parentCategoryName;  // ← Embedded name (not just ID)
    private LocalDateTime createdAt;

    // Constructor from entity
    public CategoryResponse(Category category) {
        this.categoryId = category.getCategoryId();
        this.name = category.getName();
        this.percentage = category.getPercentage();
        this.balance = category.getBalance();
        this.createdAt = category.getCreatedAt();

        // Embed parent name if exists
        if (category.getParentCategory() != null) {
            this.parentCategoryName = category.getParentCategory().getName();
        }
    }

    // Getters and setters
}
```

### Bean Validation Annotations

| Annotation    | Purpose                | Example                                   |
| ------------- | ---------------------- | ----------------------------------------- |
| `@NotNull`    | Field cannot be null   | `@NotNull(message = "ID required")`       |
| `@NotBlank`   | String not null/empty  | `@NotBlank(message = "Name required")`    |
| `@Size`       | String/collection size | `@Size(min = 1, max = 50)`                |
| `@Email`      | Valid email format     | `@Email(message = "Invalid email")`       |
| `@DecimalMin` | Minimum decimal value  | `@DecimalMin(value = "0.0")`              |
| `@DecimalMax` | Maximum decimal value  | `@DecimalMax(value = "100.0")`            |
| `@Positive`   | Must be > 0            | `@Positive(message = "Must be positive")` |
| `@Past`       | Date in the past       | `@Past(message = "Must be past date")`    |

### Entity ↔ DTO Transformation

```java
// Controller method
@PostMapping
public ResponseEntity<CategoryResponse> createCategory(
        @Valid @RequestBody CategoryRequest request) {

    // DTO → Service (pass primitive values)
    Category category = categoryService.createCategory(
        request.getUserId(),
        request.getName(),
        request.getPercentage(),
        request.getParentCategoryId()
    );

    // Entity → DTO
    CategoryResponse response = new CategoryResponse(category);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

## 📦 Maven Commands

### Essential Commands

```bash
# Clean compiled files
mvn clean

# Compile source code
mvn compile

# Run tests
mvn test

# Skip tests during build
mvn package -DskipTests

# Package as JAR
mvn package

# Install to local Maven repo
mvn install

# Run Spring Boot application
mvn spring-boot:run

# Clean + Package (common workflow)
mvn clean package

# Clean + Package + Skip tests (faster)
mvn clean package -DskipTests
```

### Build Output

```
target/
├── classes/                      ← Compiled .class files
├── generated-sources/           ← Auto-generated code
├── test-classes/                ← Compiled test files
└── nexusfi-0.0.1-SNAPSHOT.jar   ← Executable JAR
```

### Running the Application

```bash
# Option 1: Maven
mvn spring-boot:run

# Option 2: JAR file
java -jar target/nexusfi-0.0.1-SNAPSHOT.jar

# Option 3: Windows script (if you created one)
.\scripts\build.bat
```

---

## 🔀 Git Workflow

### Branch Strategy

```
main (production-ready releases)
  ↑
develop (integration branch)
  ↑
feature/xxx (feature branches)
```

### Feature Development Workflow

```bash
# 1. Start on develop
git checkout develop
git pull origin develop

# 2. Create feature branch
git checkout -b feature/security-jwt

# 3. Work on feature (multiple commits)
git add .
git commit -m "feat: Add JWT token generation"
git commit -m "feat: Add authentication filter"

# 4. Push feature branch
git push -u origin feature/security-jwt

# 5. Merge to develop (no fast-forward)
git checkout develop
git merge --no-ff feature/security-jwt

# 6. Push develop
git push origin develop

# 7. Clean up feature branch
git branch -d feature/security-jwt          # Delete local
git push origin --delete feature/security-jwt  # Delete remote
```

### Git Tags (Releases)

```bash
# Create annotated tag
git tag -a v0.3.0 -m "Add JWT authentication and authorization"

# Push tag to GitHub
git push origin v0.3.0

# Push all tags
git push origin --tags

# List tags
git tag

# Show tag details
git show v0.3.0

# Checkout specific version
git checkout v0.3.0

# Delete tag (if needed)
git tag -d v0.3.0                    # Delete local
git push origin --delete v0.3.0      # Delete remote
```

### Semantic Versioning

Format: `vMAJOR.MINOR.PATCH`

- **MAJOR** (1.0.0) - Breaking changes
- **MINOR** (0.1.0) - New features (backward compatible)
- **PATCH** (0.0.1) - Bug fixes

Examples:

- `v0.1.0` - First release (Database + Backend)
- `v0.2.0` - REST API added
- `v0.3.0` - Security added (next)
- `v1.0.0` - Production ready

### Useful Git Commands

```bash
# Status
git status
git status --short

# Log
git log --oneline -10           # Last 10 commits
git log --graph --oneline       # Visual branch graph

# Diff
git diff                        # Unstaged changes
git diff --staged              # Staged changes
git diff main develop          # Compare branches

# Stash (temporary save)
git stash                      # Save changes
git stash list                 # List stashes
git stash pop                  # Restore latest stash
git stash clear               # Delete all stashes

# Undo changes
git restore <file>             # Discard changes
git restore --staged <file>    # Unstage
git reset --hard HEAD          # Discard all changes
git reset --hard origin/develop  # Match remote

# Branch management
git branch                     # List local branches
git branch -a                  # List all branches
git branch -d feature/xxx      # Delete branch
```

---

## ⚙️ Configuration Files

### application.yml (Main Config)

```yaml
spring:
  application:
    name: NexusFi

  # Database Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/nexusfi
    username: ${DB_USERNAME:postgres} # Environment variable with default
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  # JPA/Hibernate Configuration
  jpa:
    hibernate:
      ddl-auto: validate # validate | update | create | create-drop
    show-sql: false # Log SQL queries
    properties:
      hibernate:
        format_sql: true # Format SQL for readability
        dialect: org.hibernate.dialect.PostgreSQLDialect

# Server Configuration
server:
  port: 8080
```

### application-dev.yml (Development Profile)

```yaml
spring:
  jpa:
    show-sql: true # Show SQL in development
    hibernate:
      ddl-auto: update # Auto-update schema

# Logging
logging:
  level:
    com.nexusfi: DEBUG # Detailed logs for our code
    org.hibernate.SQL: DEBUG
```

### Activate Profile

```bash
# Option 1: Command line
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Option 2: Environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Option 3: application.yml
spring:
  profiles:
    active: dev
```

### pom.xml Key Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Web (REST API) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA (Database access) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Bean Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

---

## 🚀 Quick Command Reference

### Start Development

```bash
# 1. Pull latest changes
git checkout develop
git pull origin develop

# 2. Start PostgreSQL (if not running)
# (Check your PostgreSQL service)

# 3. Run application
mvn spring-boot:run

# 4. Test an endpoint
curl http://localhost:8080/api/v1/categories?userId=1
```

### Create New Feature

```bash
# 1. Create feature branch
git checkout -b feature/my-feature

# 2. Make changes, commit
git add .
git commit -m "feat: Add feature description"

# 3. Push and merge
git push -u origin feature/my-feature
git checkout develop
git merge --no-ff feature/my-feature
git push origin develop

# 4. Clean up
git branch -d feature/my-feature
git push origin --delete feature/my-feature
```

### Create Release Tag

```bash
# 1. Make sure develop is clean
git checkout develop
git pull origin develop

# 2. Merge to main
git checkout main
git merge --no-ff develop
git push origin main

# 3. Create tag
git tag -a v0.3.0 -m "Release description"
git push origin v0.3.0

# 4. Back to develop
git checkout develop
```

### Test Endpoints (curl)

```bash
# GET - List categories
curl http://localhost:8080/api/v1/categories?userId=1

# POST - Create category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"name":"Groceries","percentage":30.00}'

# PUT - Update category
curl -X PUT http://localhost:8080/api/v1/categories/5 \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"name":"Food","percentage":35.00}'

# DELETE - Delete category
curl -X DELETE http://localhost:8080/api/v1/categories/5
```

### Database Commands (PostgreSQL)

```bash
# Connect to database
psql -U postgres -d nexusfi

# List tables
\dt

# Describe table
\d users

# Query
SELECT * FROM users;
SELECT * FROM categories WHERE user_id = 1;

# Exit
\q
```

---

## 📝 Best Practices Learned

### Code Organization

✅ **DO:**

- Use layered architecture (Controller → Service → Repository)
- Constructor injection for dependencies
- DTOs for API requests/responses
- Custom exceptions for business errors
- `@Transactional` on service methods
- Meaningful method and variable names

❌ **DON'T:**

- Expose entities directly in REST API
- Use field injection (`@Autowired`)
- Hardcode values (use configuration)
- Ignore exception handling
- Mix business logic in controllers

### REST API Design

✅ **DO:**

- Use nouns for URLs (`/categories`, not `/getCategories`)
- Use HTTP methods correctly (GET for read, POST for create)
- Return proper status codes
- Validate input with `@Valid`
- Use `ResponseEntity` for full control

❌ **DON'T:**

- Use verbs in URLs
- Return 200 OK for everything
- Ignore validation
- Expose internal errors to clients

### Database

✅ **DO:**

- Use `BigDecimal` for money
- Use `LocalDateTime` for timestamps
- Use `FetchType.LAZY` by default
- Name foreign keys clearly
- Use indexes on frequently queried columns

❌ **DON'T:**

- Use `float` or `double` for money (rounding errors!)
- Use `FetchType.EAGER` everywhere (performance issues)
- Ignore cascade types
- Store dates as strings

---

## 🎓 Key Takeaways

### Architecture Layers (Bottom to Top)

1. **Database** - PostgreSQL stores data
2. **Entities** - Java classes map to tables (`@Entity`)
3. **Repositories** - Data access (`extends JpaRepository`)
4. **Services** - Business logic (`@Service`, `@Transactional`)
5. **Controllers** - REST API (`@RestController`, HTTP endpoints)
6. **DTOs** - Request/response objects (validation, security)

### Data Flow

```
HTTP Request (JSON)
    ↓
Controller (validate, parse DTO)
    ↓
Service (business logic, transactions)
    ↓
Repository (JPA query)
    ↓
Database (PostgreSQL)
    ↓
Entity (Java object)
    ↓
Service (transform to DTO)
    ↓
Controller (return ResponseEntity)
    ↓
HTTP Response (JSON)
```

### Technologies Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Database:** PostgreSQL 15+
- **ORM:** Hibernate (JPA implementation)
- **Build Tool:** Maven
- **Version Control:** Git + GitHub

---

## 📚 What's Next (Phase 5)

**Spring Security with JWT:**

- Password encryption (BCrypt)
- Login endpoint
- JWT token generation
- Authentication filter
- Secure endpoints
- Authorization roles

**After completing Phase 5:**

- Create `v0.3.0` tag
- Update this document with security notes

---

## 🔍 Quick Search

**Need to find something?** Use Ctrl+F:

- Entity annotations → Search "@Entity"
- Repository examples → Search "Repository"
- Exception handling → Search "Exception"
- DTOs → Search "DTO"
- Git commands → Search "git"
- Maven commands → Search "mvn"

---

**End of Learning Notes**  
_Keep learning, keep building! 🚀_
