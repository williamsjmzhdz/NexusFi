-- NexusFi Database Schema
-- PostgreSQL 14+
-- This is the complete database schema based on the requirements

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL, -- BCrypt hash (always 60 chars)
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Single user for the application';
COMMENT ON COLUMN users.password IS 'BCrypt hashed password';
COMMENT ON COLUMN users.enabled IS 'Account enabled flag for Spring Security';

-- ============================================================================
-- 2. CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    assigned_percentage DECIMAL(5,2) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_assigned_percentage CHECK (assigned_percentage >= 0.00 AND assigned_percentage <= 100.00),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_category_name_parent_user UNIQUE (name, parent_id, user_id)
);

-- Indexes
CREATE INDEX idx_category_user ON categories(user_id);
CREATE INDEX idx_category_parent ON categories(parent_id);
CREATE INDEX idx_category_active ON categories(is_active);
CREATE INDEX idx_category_user_active ON categories(user_id, is_active);

COMMENT ON TABLE categories IS 'Budget categories with hierarchical structure (max 2 levels enforced at app level)';
COMMENT ON COLUMN categories.assigned_percentage IS 'Percentage of parent allocation. Root categories must sum to 100%, subcategories can sum to <= 100%';
COMMENT ON COLUMN categories.current_balance IS 'Current balance in this category';
COMMENT ON COLUMN categories.is_active IS 'Soft delete flag - can only archive when balance=0 and percentage=0';
COMMENT ON COLUMN categories.parent_id IS 'Parent category (NULL for root categories). Max depth: 2 levels (root and subcategory only)';

-- ============================================================================
-- 3. INCOME RECORDS TABLE
-- ============================================================================
CREATE TABLE income_records (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    source VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    income_date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_income_amount CHECK (amount >= 0.01),
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_income_user ON income_records(user_id);
CREATE INDEX idx_income_date ON income_records(income_date);
CREATE INDEX idx_income_user_date ON income_records(user_id, income_date);

COMMENT ON TABLE income_records IS 'Income entries that trigger automatic distribution to categories';
COMMENT ON COLUMN income_records.amount IS 'Total income amount to be distributed';
COMMENT ON COLUMN income_records.source IS 'Source of income (e.g., Salary, Freelance, etc.)';

-- ============================================================================
-- 4. EXPENSE RECORDS TABLE
-- ============================================================================
CREATE TABLE expense_records (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    expense_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_expense_amount CHECK (amount >= 0.01),
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_expense_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_expense_category ON expense_records(category_id);
CREATE INDEX idx_expense_user ON expense_records(user_id);
CREATE INDEX idx_expense_date ON expense_records(expense_date);
CREATE INDEX idx_expense_user_date ON expense_records(user_id, expense_date);

COMMENT ON TABLE expense_records IS 'Expenses from categories (money leaving the system)';
COMMENT ON COLUMN expense_records.merchant IS 'Merchant or payee name';
COMMENT ON COLUMN expense_records.category_id IS 'Category from which expense is made';

-- ============================================================================
-- 5. TRANSFERS TABLE
-- ============================================================================
CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(1000),
    transfer_date DATE NOT NULL,
    source_category_id BIGINT NOT NULL,
    destination_category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_transfer_amount CHECK (amount >= 0.01),
    CONSTRAINT chk_transfer_different_categories CHECK (source_category_id != destination_category_id),
    CONSTRAINT fk_transfer_source FOREIGN KEY (source_category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_destination FOREIGN KEY (destination_category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_transfer_source ON transfers(source_category_id);
CREATE INDEX idx_transfer_destination ON transfers(destination_category_id);
CREATE INDEX idx_transfer_user ON transfers(user_id);
CREATE INDEX idx_transfer_date ON transfers(transfer_date);
CREATE INDEX idx_transfer_user_date ON transfers(user_id, transfer_date);

COMMENT ON TABLE transfers IS 'Zero-sum transfers between categories';
COMMENT ON COLUMN transfers.source_category_id IS 'Category from which money is transferred (debit)';
COMMENT ON COLUMN transfers.destination_category_id IS 'Category to which money is transferred (credit)';

-- ============================================================================
-- 6. MOVEMENTS TABLE (Central Ledger)
-- ============================================================================
CREATE TABLE movements (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(1000),
    movement_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    income_record_id BIGINT,
    expense_record_id BIGINT,
    transfer_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_movement_type CHECK (type IN ('ASSIGNMENT', 'EXPENSE', 'TRANSFER', 'REBALANCE')),
    CONSTRAINT fk_movement_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_movement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_movement_income FOREIGN KEY (income_record_id) REFERENCES income_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_movement_expense FOREIGN KEY (expense_record_id) REFERENCES expense_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_movement_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_movement_category ON movements(category_id);
CREATE INDEX idx_movement_user ON movements(user_id);
CREATE INDEX idx_movement_type ON movements(type);
CREATE INDEX idx_movement_date ON movements(movement_date);
CREATE INDEX idx_movement_income ON movements(income_record_id);
CREATE INDEX idx_movement_expense ON movements(expense_record_id);
CREATE INDEX idx_movement_transfer ON movements(transfer_id);
CREATE INDEX idx_movement_user_date ON movements(user_id, movement_date);

COMMENT ON TABLE movements IS 'Central ledger tracking all money movements';
COMMENT ON COLUMN movements.amount IS 'Positive for credits, negative for debits';
COMMENT ON COLUMN movements.type IS 'ASSIGNMENT: income distribution, EXPENSE: money out, TRANSFER: between categories, REBALANCE: percentage adjustments';

-- ============================================================================
-- TRIGGER: Update timestamp on UPDATE
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_income_records_updated_at BEFORE UPDATE ON income_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_records_updated_at BEFORE UPDATE ON expense_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transfers_updated_at BEFORE UPDATE ON transfers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_movements_updated_at BEFORE UPDATE ON movements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- HELPER VIEWS
-- ============================================================================

-- View: Category hierarchy with full path
CREATE OR REPLACE VIEW v_category_hierarchy AS
WITH RECURSIVE category_tree AS (
    -- Root categories
    SELECT 
        id,
        name,
        parent_id,
        assigned_percentage,
        current_balance,
        is_active,
        user_id,
        0 as level,
        CAST(name AS VARCHAR(500)) as full_path,
        ARRAY[id] as path_ids
    FROM categories
    WHERE parent_id IS NULL
    
    UNION ALL
    
    -- Child categories
    SELECT 
        c.id,
        c.name,
        c.parent_id,
        c.assigned_percentage,
        c.current_balance,
        c.is_active,
        c.user_id,
        ct.level + 1,
        CAST(ct.full_path || ' > ' || c.name AS VARCHAR(500)),
        ct.path_ids || c.id
    FROM categories c
    INNER JOIN category_tree ct ON c.parent_id = ct.id
)
SELECT * FROM category_tree;

COMMENT ON VIEW v_category_hierarchy IS 'Hierarchical view of categories with full path';

-- View: Category balance summary
CREATE OR REPLACE VIEW v_category_balance_summary AS
SELECT 
    c.id,
    c.name,
    c.parent_id,
    c.assigned_percentage,
    c.current_balance,
    c.is_active,
    c.user_id,
    COUNT(DISTINCT m.id) as movement_count,
    COALESCE(SUM(CASE WHEN m.amount > 0 THEN m.amount ELSE 0 END), 0) as total_credits,
    COALESCE(SUM(CASE WHEN m.amount < 0 THEN ABS(m.amount) ELSE 0 END), 0) as total_debits
FROM categories c
LEFT JOIN movements m ON c.id = m.category_id
GROUP BY c.id, c.name, c.parent_id, c.assigned_percentage, c.current_balance, c.is_active, c.user_id;

COMMENT ON VIEW v_category_balance_summary IS 'Summary of category balances with movement statistics';

-- ============================================================================
-- INTEGRITY VERIFICATION FUNCTIONS
-- ============================================================================

-- Function to verify sibling percentage sum
CREATE OR REPLACE FUNCTION verify_sibling_percentages(p_category_id BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
    v_parent_id BIGINT;
    v_user_id BIGINT;
    v_total_percentage DECIMAL(5,2);
BEGIN
    -- Get parent_id and user_id
    SELECT parent_id, user_id INTO v_parent_id, v_user_id
    FROM categories WHERE id = p_category_id;
    
    -- Sum percentages of siblings
    SELECT COALESCE(SUM(assigned_percentage), 0) INTO v_total_percentage
    FROM categories
    WHERE (parent_id = v_parent_id OR (parent_id IS NULL AND v_parent_id IS NULL))
      AND user_id = v_user_id
      AND is_active = TRUE;
    
    -- Check if sum equals 100
    RETURN v_total_percentage = 100.00;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION verify_sibling_percentages IS 'Verifies that sibling categories sum to 100%';

-- Function to get total system balance for a user
CREATE OR REPLACE FUNCTION get_total_system_balance(p_user_id BIGINT)
RETURNS DECIMAL(15,2) AS $$
DECLARE
    v_total DECIMAL(15,2);
BEGIN
    SELECT COALESCE(SUM(current_balance), 0) INTO v_total
    FROM categories
    WHERE user_id = p_user_id AND is_active = TRUE;
    
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_total_system_balance IS 'Returns total system balance for a user';

-- ============================================================================
-- SAMPLE DATA (for testing)
-- ============================================================================

-- Insert sample user (password is 'password123' hashed with BCrypt)
INSERT INTO users (email, password, first_name, last_name, enabled, created_at, updated_at)
VALUES ('user@nexusfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Note: You should create categories and other test data via the application
-- to ensure all business rules and automatic distributions are properly handled

-- ============================================================================
-- USEFUL QUERIES
-- ============================================================================

-- Query: Find categories with invalid percentage sums
/*
SELECT parent_id, SUM(assigned_percentage) as total_percentage
FROM categories
WHERE is_active = TRUE
GROUP BY parent_id
HAVING SUM(assigned_percentage) != 100.00;
*/

-- Query: Get all root categories for a user
/*
SELECT * FROM categories
WHERE parent_id IS NULL AND user_id = ? AND is_active = TRUE
ORDER BY name;
*/

-- Query: Get category with all children
/*
WITH RECURSIVE category_subtree AS (
    SELECT * FROM categories WHERE id = ?
    UNION ALL
    SELECT c.* FROM categories c
    INNER JOIN category_subtree cs ON c.parent_id = cs.id
)
SELECT * FROM category_subtree;
*/

-- Query: Get movement history with related information
/*
SELECT 
    m.*,
    c.name as category_name,
    CASE 
        WHEN m.type = 'ASSIGNMENT' THEN i.source
        WHEN m.type = 'EXPENSE' THEN e.merchant
        WHEN m.type = 'TRANSFER' THEN 
            CASE 
                WHEN m.amount > 0 THEN 'From: ' || sc.name
                ELSE 'To: ' || dc.name
            END
    END as related_info
FROM movements m
INNER JOIN categories c ON m.category_id = c.id
LEFT JOIN income_records i ON m.income_record_id = i.id
LEFT JOIN expense_records e ON m.expense_record_id = e.id
LEFT JOIN transfers t ON m.transfer_id = t.id
LEFT JOIN categories sc ON t.source_category_id = sc.id
LEFT JOIN categories dc ON t.destination_category_id = dc.id
WHERE m.user_id = ?
ORDER BY m.movement_date DESC, m.created_at DESC
LIMIT 50;
*/
