# NexusFi - Data Model Documentation

## Overview

The NexusFi data model is designed to support a percentage-based personal finance system with strict accounting and allocation integrity. The model enforces two fundamental principles:

1. **Accounting Integrity**: Money cannot be created or destroyed. The total system balance only changes with income and expenses.
2. **Allocation Integrity**: Sibling categories must always sum to 100%.

## Entity Relationship Diagram

```
┌─────────────┐
│    User     │
└──────┬──────┘
       │
       │ 1:N
       │
   ┌───┴────────────────────────────────────┐
   │                                        │
   │ 1:N                                    │ 1:N
┌──▼────────────┐                    ┌─────▼────────┐
│  Categories   │◄───────────┐       │   Movements  │
└───────┬───────┘            │       └──────┬───────┘
        │                    │              │
        │ Self-Reference     │              │
        │ (Parent/Child)     │              │
        │                    │              │
        └────────────────────┘              │
                                            │
        ┌───────────────────────────────────┼───────────────────────┐
        │                                   │                       │
        │ 1:N                               │ 1:N                   │ 1:N
┌───────▼──────────┐              ┌─────────▼──────┐       ┌───────▼──────────┐
│  Income Records  │              │ Expense Records│       │    Transfers     │
└──────────────────┘              └────────────────┘       └──────────────────┘
```

## Entities

### 1. User
**Table**: `users`

Single-user application with Spring Security integration.

| Field       | Type          | Constraints                        | Description                    |
|-------------|---------------|------------------------------------|---------------------------------|
| id          | BIGINT        | PK, AUTO_INCREMENT                 | Primary key                    |
| email       | VARCHAR(255)  | NOT NULL, UNIQUE                   | User email (login username)    |
| password    | VARCHAR(60)   | NOT NULL                           | BCrypt hashed password         |
| first_name  | VARCHAR(100)  | NOT NULL                           | User's first name              |
| last_name   | VARCHAR(100)  | NOT NULL                           | User's last name               |
| enabled     | BOOLEAN       | NOT NULL, DEFAULT TRUE             | Account enabled flag           |
| created_at  | TIMESTAMP     | NOT NULL                           | Creation timestamp             |
| updated_at  | TIMESTAMP     | NOT NULL                           | Last update timestamp          |

### 2. Category
**Table**: `categories`

Budget categories with hierarchical structure supporting parent-child relationships.

| Field               | Type           | Constraints                    | Description                              |
|---------------------|----------------|--------------------------------|------------------------------------------|
| id                  | BIGINT         | PK, AUTO_INCREMENT             | Primary key                              |
| name                | VARCHAR(100)   | NOT NULL                       | Category name                            |
| description         | VARCHAR(500)   | NULL                           | Optional description                     |
| assigned_percentage | DECIMAL(5,2)   | NOT NULL, 0.00-100.00          | Percentage of parent's allocation        |
| current_balance     | DECIMAL(15,2)  | NOT NULL, DEFAULT 0.00         | Current category balance                 |
| is_active           | BOOLEAN        | NOT NULL, DEFAULT TRUE         | Soft delete flag                         |
| parent_id           | BIGINT         | FK → categories(id), NULL      | Parent category (NULL for root)          |
| user_id             | BIGINT         | FK → users(id), NOT NULL       | Category owner                           |
| created_at          | TIMESTAMP      | NOT NULL                       | Creation timestamp                       |
| updated_at          | TIMESTAMP      | NOT NULL                       | Last update timestamp                    |

**Indexes**:
- `idx_category_user` on `user_id`
- `idx_category_parent` on `parent_id`
- `idx_category_active` on `is_active`
- `idx_category_user_active` on `user_id, is_active`

**Unique Constraints**:
- `uk_category_name_parent_user` on `(name, parent_id, user_id)`

**Business Rules**:
- Maximum 2 levels of hierarchy (root → subcategory, no sub-subcategories)
- Root categories (parent_id = NULL) must sum to 100%
- Subcategories can sum to ≤ 100% (remainder stays in parent)
- Can only archive when `current_balance = 0` and `assigned_percentage = 0`
- Name must be unique among siblings

### 3. Income Record
**Table**: `income_records`

Records income entries that trigger automatic distribution to categories.

| Field        | Type           | Constraints                    | Description                     |
|--------------|----------------|--------------------------------|---------------------------------|
| id           | BIGINT         | PK, AUTO_INCREMENT             | Primary key                     |
| amount       | DECIMAL(15,2)  | NOT NULL, >= 0.01              | Income amount                   |
| source       | VARCHAR(255)   | NOT NULL                       | Income source description       |
| description  | VARCHAR(1000)  | NULL                           | Optional notes                  |
| income_date  | DATE           | NOT NULL                       | Date of income                  |
| user_id      | BIGINT         | FK → users(id), NOT NULL       | Income owner                    |
| created_at   | TIMESTAMP      | NOT NULL                       | Creation timestamp              |
| updated_at   | TIMESTAMP      | NOT NULL                       | Last update timestamp           |

**Indexes**:
- `idx_income_user` on `user_id`
- `idx_income_date` on `income_date`
- `idx_income_user_date` on `user_id, income_date`

### 4. Expense Record
**Table**: `expense_records`

Records expenses from categories (money leaving the system).

| Field        | Type           | Constraints                    | Description                     |
|--------------|----------------|--------------------------------|---------------------------------|
| id           | BIGINT         | PK, AUTO_INCREMENT             | Primary key                     |
| amount       | DECIMAL(15,2)  | NOT NULL, >= 0.01              | Expense amount                  |
| merchant     | VARCHAR(255)   | NOT NULL                       | Merchant/payee name             |
| description  | VARCHAR(1000)  | NULL                           | Optional notes                  |
| expense_date | DATE           | NOT NULL                       | Date of expense                 |
| category_id  | BIGINT         | FK → categories(id), NOT NULL  | Category charged                |
| user_id      | BIGINT         | FK → users(id), NOT NULL       | Expense owner                   |
| created_at   | TIMESTAMP      | NOT NULL                       | Creation timestamp              |
| updated_at   | TIMESTAMP      | NOT NULL                       | Last update timestamp           |

**Indexes**:
- `idx_expense_category` on `category_id`
- `idx_expense_user` on `user_id`
- `idx_expense_date` on `expense_date`
- `idx_expense_user_date` on `user_id, expense_date`

### 5. Transfer
**Table**: `transfers`

Records zero-sum transfers between categories.

| Field                   | Type           | Constraints                    | Description                     |
|-------------------------|----------------|--------------------------------|---------------------------------|
| id                      | BIGINT         | PK, AUTO_INCREMENT             | Primary key                     |
| amount                  | DECIMAL(15,2)  | NOT NULL, >= 0.01              | Transfer amount                 |
| description             | VARCHAR(1000)  | NULL                           | Optional notes                  |
| transfer_date           | DATE           | NOT NULL                       | Date of transfer                |
| source_category_id      | BIGINT         | FK → categories(id), NOT NULL  | Source category (debit)         |
| destination_category_id | BIGINT         | FK → categories(id), NOT NULL  | Destination category (credit)   |
| user_id                 | BIGINT         | FK → users(id), NOT NULL       | Transfer owner                  |
| created_at              | TIMESTAMP      | NOT NULL                       | Creation timestamp              |
| updated_at              | TIMESTAMP      | NOT NULL                       | Last update timestamp           |

**Indexes**:
- `idx_transfer_source` on `source_category_id`
- `idx_transfer_destination` on `destination_category_id`
- `idx_transfer_user` on `user_id`
- `idx_transfer_date` on `transfer_date`
- `idx_transfer_user_date` on `user_id, transfer_date`

**Business Rules**:
- Source and destination must be different categories
- Creates 2 movements: debit (source) and credit (destination)

### 6. Movement
**Table**: `movements`

Central ledger table recording all money movements in the system.

| Field             | Type           | Constraints                       | Description                          |
|-------------------|----------------|-----------------------------------|--------------------------------------|
| id                | BIGINT         | PK, AUTO_INCREMENT                | Primary key                          |
| amount            | DECIMAL(15,2)  | NOT NULL                          | Movement amount (+ or -)             |
| type              | VARCHAR(20)    | NOT NULL, ENUM                    | Movement type (see below)            |
| description       | VARCHAR(1000)  | NULL                              | Optional notes                       |
| movement_date     | DATE           | NOT NULL                          | Date of movement                     |
| category_id       | BIGINT         | FK → categories(id), NOT NULL     | Affected category                    |
| user_id           | BIGINT         | FK → users(id), NOT NULL          | Movement owner                       |
| income_record_id  | BIGINT         | FK → income_records(id), NULL     | Reference if type = ASSIGNMENT       |
| expense_record_id | BIGINT         | FK → expense_records(id), NULL    | Reference if type = EXPENSE          |
| transfer_id       | BIGINT         | FK → transfers(id), NULL          | Reference if type = TRANSFER         |
| created_at        | TIMESTAMP      | NOT NULL                          | Creation timestamp                   |
| updated_at        | TIMESTAMP      | NOT NULL                          | Last update timestamp                |

**Indexes**:
- `idx_movement_category` on `category_id`
- `idx_movement_user` on `user_id`
- `idx_movement_type` on `type`
- `idx_movement_date` on `movement_date`
- `idx_movement_income` on `income_record_id`
- `idx_movement_expense` on `expense_record_id`
- `idx_movement_transfer` on `transfer_id`
- `idx_movement_user_date` on `user_id, movement_date`

**Movement Types**:

| Type       | Amount Sign | Description                                          | Creates Balance Change |
|------------|-------------|------------------------------------------------------|------------------------|
| ASSIGNMENT | Positive    | Income distribution to categories                    | Increases total        |
| EXPENSE    | Negative    | Money leaving the system                             | Decreases total        |
| TRANSFER   | +/-         | Money moving between categories (2 movements)        | Zero-sum               |
| REBALANCE  | +/-         | Adjustments from percentage changes (N movements)    | Zero-sum               |

## Data Integrity Rules

### Accounting Integrity

1. **Total Balance Calculation**:
   ```
   System Total = Sum of all category current_balances
   ```

2. **Balance Change Rules**:
   - Only ASSIGNMENT (income) increases system total
   - Only EXPENSE decreases system total
   - TRANSFER and REBALANCE are zero-sum operations

3. **Movement Sum Verification**:
   ```
   For each TRANSFER: movement[source].amount + movement[destination].amount = 0
   For each REBALANCE operation: Sum of all related movements = 0
   ```

### Allocation Integrity

1. **Hierarchy Depth Limit**:
   - Maximum 2 levels allowed
   - Level 1: Root categories (parent_id = NULL)
   - Level 2: Subcategories (parent_id = root category)
   - Level 3+: NOT ALLOWED (throws MaxDepthExceededException → 400 Bad Request)

2. **Root Category Percentage Rule**:
   ```sql
   SELECT SUM(assigned_percentage) 
   FROM categories 
   WHERE parent_id IS NULL AND user_id = ? AND is_active = true
   -- Must equal 100.00
   ```

3. **Subcategory Percentage Rule**:
   ```sql
   SELECT SUM(assigned_percentage) 
   FROM categories 
   WHERE parent_id = ? AND is_active = true
   -- Can be 0.00 to 100.00 (remainder stays in parent)
   ```

4. **Archive Prerequisites**:
   - `current_balance = 0.00`
   - `assigned_percentage = 0.00`
   - All sibling percentages must be rebalanced first

## Common Queries

### Get Category Hierarchy
```sql
WITH RECURSIVE category_tree AS (
    SELECT id, name, parent_id, assigned_percentage, current_balance, 0 as level
    FROM categories
    WHERE parent_id IS NULL AND user_id = ? AND is_active = true
    
    UNION ALL
    
    SELECT c.id, c.name, c.parent_id, c.assigned_percentage, c.current_balance, ct.level + 1
    FROM categories c
    INNER JOIN category_tree ct ON c.parent_id = ct.id
    WHERE c.is_active = true
)
SELECT * FROM category_tree ORDER BY level, name;
```

### Verify Allocation Integrity
```sql
SELECT parent_id, SUM(assigned_percentage) as total_percentage
FROM categories
WHERE is_active = true AND user_id = ?
GROUP BY parent_id
HAVING SUM(assigned_percentage) != 100.00;
-- Should return 0 rows
```

### Get Total System Balance
```sql
SELECT SUM(current_balance) as total_balance
FROM categories
WHERE user_id = ? AND is_active = true;
```

### Get Category Movement History
```sql
SELECT m.*, 
       CASE 
           WHEN m.type = 'ASSIGNMENT' THEN i.source
           WHEN m.type = 'EXPENSE' THEN e.merchant
           WHEN m.type = 'TRANSFER' THEN 
               CASE 
                   WHEN m.amount > 0 THEN CONCAT('From: ', sc.name)
                   ELSE CONCAT('To: ', dc.name)
               END
       END as related_info
FROM movements m
LEFT JOIN income_records i ON m.income_record_id = i.id
LEFT JOIN expense_records e ON m.expense_record_id = e.id
LEFT JOIN transfers t ON m.transfer_id = t.id
LEFT JOIN categories sc ON t.source_category_id = sc.id
LEFT JOIN categories dc ON t.destination_category_id = dc.id
WHERE m.category_id = ? AND m.user_id = ?
ORDER BY m.movement_date DESC, m.created_at DESC;
```

## Spring Boot Configuration

### application.yml Example
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  datasource:
    url: jdbc:postgresql://localhost:5432/nexusfi
    username: nexusfi_user
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

### Required Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Migration Strategy

### Flyway Migration Example (V1__initial_schema.sql)
```sql
-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    assigned_percentage DECIMAL(5,2) NOT NULL CHECK (assigned_percentage >= 0.00 AND assigned_percentage <= 100.00),
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_category_name_parent_user UNIQUE (name, parent_id, user_id)
);

-- Create indexes for categories
CREATE INDEX idx_category_user ON categories(user_id);
CREATE INDEX idx_category_parent ON categories(parent_id);
CREATE INDEX idx_category_active ON categories(is_active);
CREATE INDEX idx_category_user_active ON categories(user_id, is_active);

-- Continue with other tables...
```

## Notes

- All monetary amounts use `DECIMAL(15,2)` for precision
- Percentages use `DECIMAL(5,2)` allowing values from 0.00 to 100.00
- All tables include audit timestamps (`created_at`, `updated_at`)
- Soft deletes are used for categories (never hard delete)
- Foreign key constraints ensure referential integrity
- Indexes are optimized for common query patterns
- The model supports multi-level category hierarchies (unlimited depth)
