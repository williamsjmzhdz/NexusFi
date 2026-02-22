# NexusFi Data Model - Quick Reference

## 📊 Database Tables Summary

| Table | Rows (Est.) | Purpose | Key Fields |
|-------|-------------|---------|-----------|
| **users** | 1 | Authentication | email, password |
| **categories** | 10-50 | Budget structure | name, assigned_percentage, current_balance |
| **income_records** | 100-1000/year | Income tracking | amount, source, income_date |
| **expense_records** | 1000-10000/year | Expense tracking | amount, merchant, expense_date |
| **transfers** | 100-500/year | Inter-category moves | amount, source_category_id, destination_category_id |
| **movements** | 2000-20000/year | Complete ledger | amount, type, movement_date, category_id |

## 🔗 Relationship Matrix

|  | User | Category | Income | Expense | Transfer | Movement |
|---|------|----------|--------|---------|----------|----------|
| **User** | - | 1:N | 1:N | 1:N | 1:N | 1:N |
| **Category** | N:1 | Self (Parent/Child) | - | N:1 | N:1 (2x) | N:1 |
| **Income** | N:1 | - | - | - | - | 1:N |
| **Expense** | N:1 | N:1 | - | - | - | 1:N |
| **Transfer** | N:1 | N:1 (2x) | - | - | - | 1:N |
| **Movement** | N:1 | N:1 | N:1 | N:1 | N:1 | - |

## 🎯 Movement Type Matrix

| Type | Amount | Source Table | Count per Transaction | System Total Impact |
|------|--------|--------------|----------------------|---------------------|
| ASSIGNMENT | Positive | income_records | 1 per active category | ⬆️ Increases |
| EXPENSE | Negative | expense_records | 1 | ⬇️ Decreases |
| TRANSFER | +/- | transfers | 2 (debit + credit) | ➡️ No change (zero-sum) |
| REBALANCE | +/- | (internal) | N (affected categories) | ➡️ No change (zero-sum) |

## 💰 Money Flow Examples

### Example 1: Income Distribution (with subcategories)
```
Income: $10,000
├─ Fixed Expenses (60%) → $6,000 (initial)
│  ├─ Rent (50%) → $3,000
│  ├─ Utilities (30%) → $1,800
│  └─ [Remainder 20%] → $1,200 stays in Fixed Expenses
└─ Savings (40%) → $4,000

Movements Created:
1. ASSIGNMENT: +$1,200 → Fixed Expenses (remainder)
2. ASSIGNMENT: +$3,000 → Rent
3. ASSIGNMENT: +$1,800 → Utilities
4. ASSIGNMENT: +$4,000 → Savings

System Total: +$10,000 ✓
```

### Example 2: Transfer
```
Transfer: $500 from Savings to Fixed Expenses

Movements Created:
1. TRANSFER: -$500 → Savings
2. TRANSFER: +$500 → Fixed Expenses

System Total: $0 (zero-sum) ✓
```

### Example 3: Expense
```
Expense: $100 from Utilities (Grocery Store)

Movements Created:
1. EXPENSE: -$100 → Utilities

System Total: -$100 ✓
```

## 📐 Percentage Rules

### Hierarchy Depth
```
Maximum: 2 levels
Level 1 (Root)  → parent_id = NULL
Level 2 (Sub)   → parent_id = root category ID
Level 3+        → NOT ALLOWED (400 Bad Request)
```

### Root Categories (Level 1)
```
Fixed Expenses:  50.00%
Savings:        30.00%
Investments:    20.00%
─────────────────────
TOTAL:         100.00% ✓ (MUST equal 100%)
```

### Subcategories (under Fixed Expenses) (Level 2)
```
Rent:           60.00%
Utilities:      25.00%
─────────────────────
TOTAL:          85.00% ✓ (Can be ≤ 100%)
REMAINDER:      15.00% → stays in Fixed Expenses
```

### Archiving Process
```
Before Archive:
Gym:            20.00%, Balance: $500 ❌

Step 1 - Transfer funds:
Gym:            20.00%, Balance: $0 ⚠️

Step 2 - Rebalance:
Gym:             0.00%, Balance: $0 ✓
Health:        100.00%

Now can archive! ✓
```

## 🔍 Common Queries Cheat Sheet

### Get all active categories for user
```sql
SELECT * FROM categories 
WHERE user_id = ? AND is_active = true 
ORDER BY parent_id, name;
```

### Verify allocation integrity
```sql
SELECT parent_id, SUM(assigned_percentage) as total
FROM categories 
WHERE is_active = true 
GROUP BY parent_id 
HAVING SUM(assigned_percentage) != 100.00;
-- Should return 0 rows
```

### Get total system balance
```sql
SELECT SUM(current_balance) 
FROM categories 
WHERE user_id = ? AND is_active = true;
```

### Get category hierarchy
```sql
WITH RECURSIVE tree AS (
  SELECT *, 0 as level FROM categories WHERE parent_id IS NULL
  UNION ALL
  SELECT c.*, tree.level + 1 FROM categories c 
  JOIN tree ON c.parent_id = tree.id
)
SELECT * FROM tree ORDER BY level, name;
```

### Get recent movements
```sql
SELECT m.*, c.name as category_name
FROM movements m
JOIN categories c ON m.category_id = c.id
WHERE m.user_id = ?
ORDER BY m.movement_date DESC, m.created_at DESC
LIMIT 50;
```

## 🏗️ Index Strategy

### Primary Indexes (Performance Critical)
- ✅ All foreign keys
- ✅ Date columns (for range queries)
- ✅ user_id (for multi-tenant pattern)
- ✅ is_active (for filtering)

### Composite Indexes
- ✅ (user_id, movement_date) - Common filter combination
- ✅ (user_id, is_active) - Active categories query

### When to Add More Indexes
- Query taking > 100ms consistently
- Table scan in EXPLAIN output
- Frequently joined columns
- WHERE clause columns in slow queries

## 🛡️ Data Integrity Checklist

### Before Deploying
- [ ] All foreign keys have indexes
- [ ] All CHECK constraints in place
- [ ] All UNIQUE constraints defined
- [ ] All NOT NULL constraints set
- [ ] Triggers for updated_at working
- [ ] Sample data loads successfully
- [ ] Percentage sum validation works

### Before Going Live
- [ ] Backup strategy in place
- [ ] Connection pooling configured
- [ ] Slow query monitoring enabled
- [ ] Database credentials secured
- [ ] SSL connection enabled (production)
- [ ] Regular maintenance schedule

## 📱 Field Size Guidelines

### Strings
- Email: 255 chars (max per RFC)
- Names: 100 chars (first/last name)
- Merchant/Source: 255 chars
- Description: 1000 chars
- Password hash: 60 chars (BCrypt fixed)

### Numbers
- Money: DECIMAL(15,2) = $9,999,999,999,999.99 max
- Percentage: DECIMAL(5,2) = 0.00 to 100.00
- IDs: BIGINT = 9,223,372,036,854,775,807 max

## 🚨 Common Pitfalls to Avoid

### ❌ Don't Do This
```java
// Updating balance directly
category.setCurrentBalance(newAmount); 
// ⚠️ Bypasses movement tracking!

// Changing percentages without validation
category.setAssignedPercentage(newPercent);
// ⚠️ May break 100% rule!

// Hard deleting categories
categoryRepository.delete(category);
// ⚠️ Loses history!
```

### ✅ Do This Instead
```java
// Create movement and update balance
Movement movement = new Movement(...);
movementRepository.save(movement);
category.setCurrentBalance(category.getCurrentBalance().add(amount));
// ✓ Proper audit trail

// Validate percentage sum before saving
validateSiblingPercentages(siblings);
categoryRepository.saveAll(siblings);
// ✓ Maintains integrity

// Soft delete
category.setIsActive(false);
// ✓ Preserves history
```

## 📈 Scaling Considerations

### Up to 1K movements/month: ✅ Current design OK
- Single database instance
- Standard indexes sufficient
- Query cache effective

### 10K+ movements/month: ⚠️ Consider
- Partitioning movements table by date
- Archive old data to separate table
- Add materialized views for reports
- Read replicas for queries

### 100K+ movements/month: 🔧 Requires
- Time-series database for movements
- Data warehouse for analytics
- Advanced caching strategy
- Microservices architecture

## 🎓 Learning Resources

### Understanding the Model
1. Study `DATA_MODEL.md` - Complete documentation
2. Review `database_schema.sql` - See SQL implementation
3. Examine JPA entities - See Java implementation
4. Test with sample data - Learn by doing

### Next Steps
1. Set up local database
2. Run schema creation
3. Create sample user
4. Build category hierarchy
5. Test income distribution
6. Try transfers and expenses
7. Verify integrity queries

---

**Quick Reference Version**: 1.0.0  
**Last Updated**: October 12, 2025  
**For**: NexusFi Personal Finance Application
