# NexusFi - Learning Notes & Reference Guide

> **Personal study guide for Francisco Williams Jiménez Hernández**  
> Everything learned including Phase 5 (Spring Security + JWT) and Hierarchical Categories  
> Last updated: January 10, 2026

---

## 📑 Table of Contents

1. [Project Overview](#-project-overview)
2. [Spring Boot Fundamentals](#-spring-boot-fundamentals)
3. [JPA & Hibernate (Database Layer)](#️-jpa--hibernate-database-layer)
4. [Repository Layer](#-repository-layer)
5. [Service Layer](#️-service-layer)
6. [Controller Layer (REST API)](#-controller-layer-rest-api)
7. [Exception Handling](#-exception-handling)
8. [DTOs (Data Transfer Objects)](#-dtos-data-transfer-objects)
9. [Spring Security & JWT](#-spring-security--jwt)
10. [Hierarchical Categories](#-hierarchical-categories)
11. [Maven Commands](#-maven-commands)
12. [Git Workflow](#-git-workflow)
13. [Configuration Files](#️-configuration-files)
14. [Quick Command Reference](#-quick-command-reference)
15. [Best Practices Learned](#-best-practices-learned)
16. [Key Takeaways](#-key-takeaways)
17. [What's Next](#-whats-next)
18. [Quick Search](#-quick-search)

---

## 🎯 Project Overview

**NexusFi** = Personal finance management system (Spring Boot + PostgreSQL)

**Architecture (Layered):**

```
┌─────────────────────────┐
│   Controller Layer      │ ← REST API endpoints (HTTP)
├─────────────────────────┤
│   Security Layer        │ ← JWT Authentication
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

**Current Status (v0.3.0 - ready to tag):**

- ✅ 7 Entities
- ✅ 6 Repositories
- ✅ 6 Services
- ✅ 6 Controllers (35 endpoints)
- ✅ 11 DTOs
- ✅ Exception handling
- ✅ JWT Authentication
- ✅ Hierarchical Categories (2-level max)

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

## 📚 What's Next

**Phase 5 COMPLETE ✅** (January 10, 2026)

**Next Steps:**

1. **Tag v0.3.0** - Merge to develop and create release
2. **Frontend Development** - Build UI for the application
   - Login/Register page
   - Dashboard with category balances
   - Income/Expense forms
   - Movements history view
3. **Unit Tests** - Add JUnit/Mockito tests for services
4. **Integration Tests** - Test API endpoints with MockMvc
5. **Deployment** - Set up production environment

---

## 🔐 Spring Security & JWT (Phase 5) ✅ COMPLETE

### JWT (JSON Web Token) Basics

**What is JWT?**

- Stateless authentication token
- Contains user information (email, expiration)
- Signed with secret key to prevent tampering
- No database lookup needed to validate

**JWT Structure:**

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.signature
    ↑ Header          ↑ Payload (claims)         ↑ Signature
```

- **Header**: Algorithm and token type (Base64 encoded)
- **Payload**: User data (claims) like email, expiration (Base64 encoded)
- **Signature**: HMAC-SHA hash of header+payload+secret (prevents tampering)

**Token Flow:**

```
1. User logs in with email/password
2. Server validates credentials via AuthenticationManager
3. Server generates JWT token (signed with secret key)
4. Client stores token (localStorage, sessionStorage, memory)
5. Client sends token in Authorization header: "Bearer <token>"
6. JwtAuthenticationFilter validates token signature and expiration
7. Filter sets SecurityContext authentication
8. Request reaches controller with authenticated user
```

### Key Security Classes Created

**CustomUserDetails:**

- Adapter between User entity and Spring Security
- Implements UserDetails interface
- Returns email as username
- Wraps User entity for authentication
- Used by AuthenticationManager during login

**JwtUtil:**

- Generates JWT tokens with email, timestamps, expiration
- Validates tokens (signature + expiration check)
- Extracts claims (email, expiration date)
- Converts Base64 secret key to cryptographic key (SecretKey)
- Uses JJWT library (io.jsonwebtoken)

**CustomUserDetailsService:**

- Implements UserDetailsService interface
- Loads users from database by email
- Returns CustomUserDetails wrapper
- Throws UsernameNotFoundException if user not found
- Called by AuthenticationManager and JwtAuthenticationFilter

**JwtAuthenticationFilter:**

- Extends OncePerRequestFilter (runs once per request)
- Extracts JWT from "Authorization: Bearer <token>" header
- Validates token with JwtUtil
- Loads user from database via CustomUserDetailsService
- Creates UsernamePasswordAuthenticationToken
- Sets authentication in SecurityContext
- Always continues filter chain (doesn't block)

**SecurityConfig:**

- Main Spring Security configuration class
- **PasswordEncoder bean**: BCryptPasswordEncoder for password hashing
- **AuthenticationManager bean**: Exposed from AuthenticationConfiguration for controller injection
- **SecurityFilterChain bean**: Configures security rules
  - Disables CSRF (stateless JWT doesn't need it)
  - Public endpoints: `/api/v1/auth/**` (permitAll)
  - Protected endpoints: all others (authenticated)
  - Stateless session management (no cookies)
  - Registers JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter

**AuthController:**

- Public endpoints (no JWT required)
- POST `/api/v1/auth/register` - User registration
  - Creates User object, hashes password with PasswordEncoder
  - Calls UserService.registerUser()
  - Generates JWT token
  - Returns 201 Created with AuthResponse (token + email)
- POST `/api/v1/auth/login` - User login
  - Uses AuthenticationManager.authenticate()
  - Validates email/password (calls CustomUserDetailsService)
  - Generates JWT token on success
  - Returns 200 OK with AuthResponse

### Important Security Concepts

**BCrypt Password Hashing:**

- One-way encryption (cannot decrypt)
- Includes random salt (prevents rainbow table attacks)
- Adaptive algorithm (can increase difficulty)
- Example: `password123` → `$2a$10$N9qo8uLOickgx2ZibSz5...`
- Salt is stored IN the hash string itself
- Same salt + same password = same hash (required for login to work!)
- BCrypt strength: 10 = 2^10 = 1024 rounds (configurable)

**Salt & Rainbow Tables:**

- **Salt** = Random data added before hashing
- Same password + different salt = different hash
- **Rainbow table** = Precomputed hash lookup table (e.g., "password123" → "5f4dcc3b5aa765d61d8327deb882cf99")
- Salt makes rainbow tables useless (attacker must brute-force each password individually)
- Salt doesn't prevent attacks but makes them VERY slow (years per password)
- Salt changes only when password changes

**Token Signing:**

- Secret key signs the token (like a wax seal)
- Prevents tampering with token contents
- Server verifies signature on every request
- Different from password salting (serves different purpose)
- JWT is encoded (Base64), NOT encrypted (anyone can decode and read payload)
- Signature ensures token wasn't modified

**AuthenticationManager How It Works:**

- Spring Security's central authentication component
- When you call `authenticationManager.authenticate(token)`:
  1. AuthenticationManager calls CustomUserDetailsService.loadUserByUsername(email)
  2. Gets CustomUserDetails with hashed password from database
  3. Compares submitted password (plain) with stored hash using PasswordEncoder
  4. BCrypt hashes submitted password with SAME salt from stored hash
  5. Compares both hashes - if match, authentication successful
  6. Returns authenticated Authentication object
  7. If no match, throws AuthenticationException

**SecurityContext:**

- Spring Security's "clipboard" for current request
- Stores authentication information (who is logged in)
- JwtAuthenticationFilter WRITES to it (sets authentication)
- Controllers READ from it (get current user with @AuthenticationPrincipal)
- Thread-local (each request has its own SecurityContext)
- Cleared after request completes

**Filter Chain Order:**

- Filters run in order before reaching controllers
- JwtAuthenticationFilter runs BEFORE UsernamePasswordAuthenticationFilter
- Order matters: JWT validation must happen first
- Filter chain continues even if token invalid (SecurityConfig blocks at end)

### Configuration

**application.yml JWT settings (IMPORTANT - Root Level!):**

```yaml
# JWT Configuration (at ROOT level, NOT inside spring:)
jwt:
  secret: <Base64-encoded-key> # 64+ characters
  expiration: 86400000 # 24 hours in milliseconds

spring:
  application:
    name: nexusfi
  # ... rest of spring config
```

**Common Mistake:** Putting JWT config inside `spring:` block causes "Could not resolve placeholder" error!

**Security Note:** In production, use environment variables:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

### Dependencies Added

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Library (JJWT) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### Key Annotations Learned

- `@EnableWebSecurity` - Enables Spring Security configuration
- `@Configuration` - Marks class as configuration (defines beans)
- `@Bean` - Creates singleton object managed by Spring
- `@Component` - Makes filter discoverable by Spring
- `@NonNull` - Method parameter cannot be null (from org.springframework.lang)

### Testing Workflow (Postman)

**1. Register a user:**

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

Expected: 201 Created
Response: { "token": "eyJhbGci...", "email": "test@example.com" }
```

**2. Login:**

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

Expected: 200 OK
Response: { "token": "eyJhbGci...", "email": "test@example.com" }
```

**3. Access protected endpoint:**

```http
GET http://localhost:8080/api/v1/categories
Authorization: Bearer eyJhbGci...

Expected: 200 OK (if token valid)
Expected: 401 Unauthorized (if no token or invalid)
```

---

## 🌳 Hierarchical Categories (Phase 5.1) ✅ COMPLETE

### Overview

Categories can have parent-child relationships, allowing for hierarchical budget organization:

```
📁 Gastos Fijos (50%)      ← Root Category (Level 1)
├── 🏠 Renta (60%)         ← Subcategory (Level 2)
├── 💡 Servicios (30%)     ← Subcategory (Level 2)
└── [10% remainder]        ← Stays in parent
📁 Ahorros (30%)           ← Root Category (Level 1)
📁 Inversiones (20%)       ← Root Category (Level 1)
```

### Key Rules

**Hierarchy Depth:**
- Maximum 2 levels allowed
- Level 1: Root categories (parent_id = NULL)
- Level 2: Subcategories (parent_id = root category ID)
- Level 3+: NOT ALLOWED → `MaxDepthExceededException` → HTTP 400

**Percentage Rules:**

| Level | Rule | Example |
|-------|------|---------|
| Level 1 (Root) | MUST sum to exactly 100% | 50% + 30% + 20% = 100% ✅ |
| Level 2 (Sub) | CAN sum to ≤ 100% | 60% + 30% = 90% ✅ (10% stays in parent) |

### Income Distribution Algorithm

When recording income, distribution is **recursive**:

```java
// Simplified algorithm (in IncomeService)
1. Get all root categories
2. For each root category:
   a. Calculate share = income * (percentage / 100)
   b. Get active subcategories
   c. If has subcategories:
      - Calculate subcategory distributions
      - Remainder = share - sum(subcategory amounts)
      - Create movement for parent (remainder)
      - Create movements for each subcategory
   d. If no subcategories:
      - Create movement for full share
3. Update all category balances
```

### Example: $10,000 Income

```
Root categories (100%):
  Gastos Fijos (50%) → $5,000
  Ahorros (30%) → $3,000
  Inversiones (20%) → $2,000

Gastos Fijos subcategories (90%):
  Renta (60%) → $3,000 (60% of $5,000)
  Servicios (30%) → $1,500 (30% of $5,000)
  [Remainder 10%] → $500 (stays in Gastos Fijos)

Final balances:
  Gastos Fijos: $500 (remainder only)
  Renta: $3,000
  Servicios: $1,500
  Ahorros: $3,000
  Inversiones: $2,000
  TOTAL: $10,000 ✅
```

### Technical Implementation

**Entity Changes (Category.java):**
```java
// FetchType.EAGER prevents LazyInitializationException
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "parent_id")
private Category parent;

@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
private List<Category> children = new ArrayList<>();
```

**Depth Validation (CategoryService.java):**
```java
// Check if parent already has a parent (would make this level 3)
if (parentId != null) {
    Category parent = categoryRepository.findById(parentId)
        .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
    
    if (parent.getParent() != null) {
        throw new MaxDepthExceededException(
            "Cannot create sub-subcategory. Maximum 2 levels allowed");
    }
}
```

**New Exception (MaxDepthExceededException.java):**
```java
public class MaxDepthExceededException extends NexusFiException {
    public MaxDepthExceededException(String message) {
        super(message);
    }
}
```

**GlobalExceptionHandler addition:**
```java
@ExceptionHandler({InvalidPercentageException.class, MaxDepthExceededException.class})
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponse handleBadRequest(NexusFiException ex) {
    return new ErrorResponse(400, ex.getMessage());
}
```

### New API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /categories/tree` | Full category hierarchy |
| `GET /categories/root` | Only root categories (Level 1) |
| `GET /categories/{id}/subcategories` | Children of a specific category |
| `POST /categories` with `parentId` | Create subcategory |

### LazyInitializationException

**Problem:** When accessing `category.getChildren()` outside a Hibernate session, you get:
```
org.hibernate.LazyInitializationException: failed to lazily initialize a collection
```

**Solution:** Use `FetchType.EAGER` for relationships that are always needed:
```java
@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
private List<Category> children;
```

**Trade-off:** EAGER loading can be slower for large datasets, but for NexusFi's category count (~10-50), it's acceptable.

### Testing Hierarchical Categories

All 36 tests passing via Postman:
1. ✅ Create 3 root categories (100%)
2. ✅ Create subcategories under a root
3. ✅ Attempt Level 3 → 400 Bad Request
4. ✅ Record income → Recursive distribution
5. ✅ Verify balances match expected amounts

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
