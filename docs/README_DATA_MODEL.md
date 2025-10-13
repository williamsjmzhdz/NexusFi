# NexusFi - Complete Data Model

## 📋 Overview

This directory contains the complete data model implementation for NexusFi, a personal finance application with percentage-based budgeting.

## 🗂️ Files Generated

### JPA Entities (`src/main/java/com/nexusfi/model/`)

1. **User.java** - User authentication entity
   - Single-user application
   - Spring Security UserDetails implementation
   - BCrypt password hashing

2. **Category.java** - Budget categories with hierarchy
   - Self-referencing parent-child relationships
   - Enforces 100% allocation rule
   - Soft delete support
   - Balance tracking

3. **IncomeRecord.java** - Income entries
   - Triggers automatic distribution
   - Links to assignment movements

4. **ExpenseRecord.java** - Expense transactions
   - Records money leaving the system
   - Links to expense movements

5. **Transfer.java** - Category-to-category transfers
   - Zero-sum operations
   - Source and destination tracking
   - Validation to prevent same-category transfers

6. **Movement.java** - Central ledger
   - Records ALL balance changes
   - Four types: ASSIGNMENT, EXPENSE, TRANSFER, REBALANCE
   - Links to source records

7. **MovementType.java** (enum) - Movement type definitions

### Documentation Files

- **DATA_MODEL.md** - Comprehensive documentation with:
  - Entity relationship diagrams
  - Field specifications
  - Business rules
  - Query examples
  - Spring Boot configuration

- **database_schema.sql** - Complete PostgreSQL schema with:
  - Table definitions
  - Indexes
  - Constraints
  - Triggers
  - Helper functions
  - Views
  - Sample data

- **pom.xml** - Maven dependencies including:
  - Spring Boot Data JPA
  - Spring Security
  - PostgreSQL driver
  - Lombok
  - Flyway migrations

## 🏗️ Entity Relationships

```
User (1) ──< (N) Category
                 │
                 ├─ Self-reference (parent/child)
                 │
                 └──< (N) Movement
                         │
                         ├─< (N) IncomeRecord
                         ├─< (N) ExpenseRecord
                         └─< (N) Transfer
```

## 🎯 Key Features

### 1. Hierarchical Categories
- Unlimited depth parent-child relationships
- Each category can have subcategories
- Root categories have `parent_id = NULL`

### 2. Percentage-Based Allocation
- Sibling categories MUST sum to 100%
- Enforced at application level
- Rebalancing required before archiving

### 3. Comprehensive Movement Tracking
- **ASSIGNMENT**: Income → Categories (increases total)
- **EXPENSE**: Category → External (decreases total)
- **TRANSFER**: Category → Category (zero-sum)
- **REBALANCE**: Percentage adjustments (zero-sum)

### 4. Accounting Integrity
- All balance changes tracked in movements table
- Zero-sum operations verified
- No money creation or destruction (except income/expense)

### 5. Soft Deletes
- Categories never hard deleted
- Archive only when balance = 0 and percentage = 0
- Historical data preserved

## 🚀 Getting Started

### 1. Setup Database

```sql
-- Create database
CREATE DATABASE nexusfi;

-- Run schema
psql -U postgres -d nexusfi -f database_schema.sql
```

### 2. Configure Application

Create `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nexusfi
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 3. Build Project

```bash
mvn clean install
```

## 📊 Data Types

### Monetary Values
- Type: `DECIMAL(15,2)`
- Precision: 15 digits total, 2 after decimal
- Range: -9,999,999,999,999.99 to 9,999,999,999,999.99

### Percentages
- Type: `DECIMAL(5,2)`
- Range: 0.00 to 100.00
- Validation: Must be within bounds

### Dates
- `LocalDate` for transaction dates
- `LocalDateTime` for audit timestamps

## 🔒 Constraints and Validations

### Database Level
- Foreign key constraints
- Check constraints on amounts and percentages
- Unique constraints on category names within same parent
- NOT NULL constraints on required fields

### Application Level (JPA Annotations)
- `@NotNull` - Required fields
- `@NotBlank` - Non-empty strings
- `@DecimalMin` / `@DecimalMax` - Numeric ranges
- `@Email` - Email validation

## 📈 Performance Optimizations

### Indexes Created
1. User-based queries: `idx_*_user`
2. Date-based queries: `idx_*_date`
3. Composite: `idx_*_user_date`
4. Category hierarchy: `idx_category_parent`
5. Movement types: `idx_movement_type`
6. Foreign key columns for join performance

### Query Optimization Tips
- Use covering indexes for common queries
- Leverage recursive CTEs for hierarchy queries
- Batch operations when creating multiple movements
- Use `@EntityGraph` to avoid N+1 queries

## 🧪 Testing Data

The schema includes a sample user:
- **Email**: user@nexusfi.com
- **Password**: password123 (hashed)

Use the application to create categories and transactions to ensure business rules are enforced.

## 📝 Business Rules Implementation

### Category Creation
1. UI forces 100% allocation
2. Validates name uniqueness among siblings
3. Creates with initial balance = 0

### Income Distribution
1. Create IncomeRecord
2. For each active root category:
   - Create ASSIGNMENT movement
   - Update current_balance
   - Recursively distribute to children

### Category Archiving
```java
// Pseudo-code
if (category.currentBalance != 0) {
    throw new Exception("Transfer funds first");
}
if (category.assignedPercentage != 0) {
    throw new Exception("Rebalance percentages first");
}
category.setIsActive(false);
```

### Transfer Execution
```java
// Creates 2 movements
Movement debit = new Movement(
    amount = -transferAmount,
    type = TRANSFER,
    category = sourceCategory
);

Movement credit = new Movement(
    amount = +transferAmount,
    type = TRANSFER,
    category = destinationCategory
);
```

## 🔍 Useful Queries

### Get Category Tree
```java
// JPA Repository method
@Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.isActive = true ORDER BY c.parent.id, c.name")
List<Category> findActiveByUserId(@Param("userId") Long userId);
```

### Verify Allocation Integrity
```java
@Query("""
    SELECT c.parent, SUM(c.assignedPercentage) 
    FROM Category c 
    WHERE c.isActive = true AND c.user.id = :userId 
    GROUP BY c.parent 
    HAVING SUM(c.assignedPercentage) <> 100.00
    """)
List<Object[]> findInvalidAllocations(@Param("userId") Long userId);
```

### Get Movement History
```java
@Query("""
    SELECT m FROM Movement m 
    WHERE m.category.id = :categoryId 
    ORDER BY m.movementDate DESC, m.createdAt DESC
    """)
Page<Movement> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
```

## 🛠️ Maintenance

### Database Migrations
Use Flyway for version control:
```
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_indexes.sql
└── V3__add_views.sql
```

### Backup Strategy
```bash
# Backup
pg_dump -U postgres nexusfi > backup_$(date +%Y%m%d).sql

# Restore
psql -U postgres -d nexusfi < backup_20251012.sql
```

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Lombok Documentation](https://projectlombok.org/)

## ⚠️ Important Notes

1. **Never bypass the application logic** for data modifications
2. **Always use transactions** for multi-table operations
3. **Validate percentage sums** before committing category changes
4. **Backup regularly** - especially before schema changes
5. **Monitor query performance** as data grows
6. **Use connection pooling** (HikariCP included in Spring Boot)

## 🤝 Contributing

When extending the data model:

1. Update all entity classes
2. Update documentation
3. Create migration scripts
4. Add validation logic
5. Update tests
6. Verify integrity constraints

## 📄 License

This data model is part of the NexusFi application.

---

**Generated**: October 12, 2025  
**Version**: 1.0.0  
**Database**: PostgreSQL 14+  
**Framework**: Spring Boot 3.2.0  
**Java Version**: 17
