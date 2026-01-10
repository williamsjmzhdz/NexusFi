# NexusFi Postman Collection

Professional API testing collection for NexusFi Personal Finance Management System.

## 📦 Import Collection

1. Open Postman
2. Click **Import** button (top-left)
3. Drag and drop `NexusFi_API_v1.postman_collection.json`
4. Click **Import**

## 🔧 Configuration

The collection includes built-in variables:

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `base_url` | `http://localhost:8080` | API server URL |
| `api_path` | `/api/v1` | API version path |

### Auto-populated Variables

These are set automatically by test scripts:

| Variable | Set By | Description |
|----------|--------|-------------|
| `token` | Login/Register | JWT authentication token |
| `user_email` | Login/Register | Current user email |
| `category_id` | Create Category | First category ID |
| `category_id_2` | Create Ahorros | Second category ID |
| `category_id_3` | Create Inversiones | Third category ID |
| `income_id` | Record Income | Last created income ID |
| `expense_id` | Record Expense | Last created expense ID |
| `transfer_id` | Execute Transfer | Last created transfer ID |
| `movement_id` | Get Movements | First movement ID |

## 🚀 Recommended Test Order

For a complete workflow test, run requests in this order:

### 1. Authentication
```
🔓 Auth
├── Register User          ✅ Creates account + saves token
└── Login User             ✅ Login existing user
```

### 2. Category Setup (must sum to 100%)
```
📁 Categories
├── Create Category        ✅ 50% - Gastos Fijos
├── Create Category - Ahorros     ✅ 30%
├── Create Category - Inversiones ✅ 20%
└── Get All Categories     ✅ Verify 3 categories
```

### 3. Record Income (triggers auto-distribution)
```
💰 Incomes
├── Record Income          ✅ 10,000.00 distributed to all categories
└── Get All Incomes        ✅ Verify income exists
```

### 4. Record Expense (deducts from category)
```
💸 Expenses
├── Record Expense         ✅ 150.00 from Gastos Fijos
└── Get Expenses by Category ✅ Verify expense exists
```

### 5. Transfer Between Categories
```
🔄 Transfers
├── Execute Transfer       ✅ 500.00 from Gastos Fijos to Ahorros
└── Get All Transfers      ✅ Verify transfer exists
```

### 6. Verify Movements (audit trail)
```
📊 Movements
├── Get All Movements      ✅ Should show all transactions
└── Get Movements by Type  ✅ Filter by ASSIGNMENT, EXPENSE, etc.
```

## 🏃 Running All Tests

### Using Postman Runner

1. Click **Run** button in collection menu (⋯)
2. Select **Run collection**
3. Ensure all requests are checked
4. Click **Run NexusFi API v1**

### Using Newman (CLI)

```bash
# Install Newman
npm install -g newman

# Run collection
newman run NexusFi_API_v1.postman_collection.json

# With HTML report
newman run NexusFi_API_v1.postman_collection.json -r html
```

## ✅ Test Coverage

| Endpoint | Tests | Description |
|----------|-------|-------------|
| `POST /auth/register` | 3 | Status, response structure, token save |
| `POST /auth/login` | 3 | Status, response structure, token save |
| `POST /categories` | 3 | Status, structure, ID save |
| `GET /categories` | 3 | Status, array type, field validation |
| `GET /categories/:id` | 2 | Status, structure |
| `PUT /categories/:id` | 2 | Status, name updated |
| `GET /categories/remaining` | 2 | Status, number type |
| `POST /incomes` | 3 | Status, structure, ID save |
| `GET /incomes` | 2 | Status, array type |
| `POST /expenses` | 3 | Status, structure, ID save |
| `GET /expenses` | 2 | Status, array type |
| `POST /transfers` | 3 | Status, structure, ID save |
| `GET /transfers` | 2 | Status, array type |
| `GET /movements` | 4 | Status, array, fields, ID save |
| `GET /movements/type/:type` | 3 | Status, array, type filter |

### Negative Tests Included

- `Login - Invalid Credentials`: Expects 401/403
- `Get Categories - No Token`: Expects 401/403

## 📁 Collection Structure

```
NexusFi API v1
├── 🔓 Auth (3 requests)
│   ├── Register User
│   ├── Login User
│   └── Login - Invalid Credentials
├── 📁 Categories (8 requests)
│   ├── Create Category (x3)
│   ├── Get All Categories
│   ├── Get Category by ID
│   ├── Update Category
│   ├── Get Remaining Percentage
│   └── Get Categories - No Token
├── 💰 Incomes (3 requests)
│   ├── Record Income
│   ├── Get All Incomes
│   └── Get Income by ID
├── 💸 Expenses (4 requests)
│   ├── Record Expense
│   ├── Get All Expenses
│   ├── Get Expense by ID
│   └── Get Expenses by Category
├── 🔄 Transfers (4 requests)
│   ├── Execute Transfer
│   ├── Get All Transfers
│   ├── Get Transfer by ID
│   └── Get Transfers by Category
└── 📊 Movements (5 requests)
    ├── Get All Movements
    ├── Get Movement by ID
    ├── Get Movements by Type - ASSIGNMENT
    ├── Get Movements by Type - EXPENSE
    └── Get Movements by Category
```

## 💡 Tips

1. **Fresh Start**: If tests fail, register a new user with a different email
2. **Categories**: Must create 3 categories summing to 100% before income
3. **Income First**: Record income before expenses (needs category balance)
4. **Token Expiry**: Tokens expire after 24 hours - run Login again
5. **Database Reset**: If needed, truncate tables and start fresh

---

**Version**: 1.0.0  
**API Version**: v1  
**Last Updated**: 2026-01-09
