# Integration Testing Guide

## 🚀 Quick Start

### Prerequisites

```bash
# Install jq for JSON parsing
sudo apt-get install jq  # Ubuntu/Debian
brew install jq          # macOS
```

### Run Full Test Suite

```bash
cd Backend/scripts
chmod +x full-integration-test.sh

# Test against local emulator
./full-integration-test.sh

# Test against production
BASE_URL="https://asia-southeast1-foodappproject-7c136.cloudfunctions.net/api" \
./full-integration-test.sh

# Detailed output mode
DETAILED=true ./full-integration-test.sh

# Stop on first error
STOP_ON_ERROR=true ./full-integration-test.sh
```

---

## 📋 Test Coverage

### ✅ Modules Tested

1. **Authentication & Authorization**
   - User registration (all roles)
   - Login with Firebase Auth
   - Profile retrieval
   - Token validation

2. **Categories**
   - List categories
   - Create category (admin)
   - Get category details

3. **Shops**
   - Create shop (owner)
   - Get shop details
   - List all shops
   - Update shop info

4. **Products**
   - Create product (owner)
   - List shop products
   - Update product
   - Product availability toggle

5. **Shopping Cart**
   - Add items to cart
   - Update quantities
   - Remove items
   - Get cart total

6. **Orders**
   - Create order from cart
   - Order status transitions:
     - PENDING → CONFIRMED
     - CONFIRMED → PREPARING
     - PREPARING → READY
     - READY → SHIPPING (shipper accepts)
     - SHIPPING → DELIVERED

7. **Payments**
   - COD payment creation
   - Payment status verification
   - SEPAY QR generation
   - Payment polling (optional)

8. **Wallets**
   - Check wallet balance
   - View ledger entries
   - Payout tracking
   - Automatic payout on delivery

9. **Shippers**
   - Submit application
   - Owner approval
   - Accept orders
   - Delivery flow

10. **Favorites**
    - Add product to favorites
    - List favorites
    - Remove from favorites

11. **Admin**
    - Dashboard statistics
    - User management
    - Shop management
    - Payout management

12. **Notifications**
    - List notifications
    - Mark as read
    - Push notification tracking

---

## 🧪 Test Scenarios

### Scenario 1: Complete Order Flow (COD)

```
Customer creates order
  → Customer creates COD payment (instant PAID)
    → Owner confirms order
      → Owner prepares order
        → Shipper accepts
          → Shipper delivers
            → Automatic payout to wallets
```

### Scenario 2: Shop & Product Management

```
Owner creates shop
  → Owner creates products
    → Customer views products
      → Customer adds to cart
        → Cart calculates totals
```

### Scenario 3: Admin Operations

```
Admin views dashboard stats
  → Admin lists all users
    → Admin lists all shops
      → Admin manages payouts
```

---

## 🔧 Configuration

### Environment Variables

```bash
# Backend URL
export BASE_URL="http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api"

# Firebase Config
export FIREBASE_API_KEY="AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA"

# Test Accounts (auto-created if not exist)
export EMAIL_CUSTOMER="customer.test@ktx.com"
export PASS_CUSTOMER="Test123!@#"
export EMAIL_OWNER="owner.test@ktx.com"
export PASS_OWNER="Test123!@#"
export EMAIL_SHIPPER="shipper.test@ktx.com"
export PASS_SHIPPER="Test123!@#"
export EMAIL_ADMIN="admin@ktx.com"
export PASS_ADMIN="Admin123!@#"

# Test Options
export DETAILED="true"         # Show full responses
export STOP_ON_ERROR="false"   # Continue after failures
```

---

## 📊 Sample Output

```
========================================
SETUP: Test Accounts
========================================
[10:30:15] Logging in as CUSTOMER: customer.test@ktx.com
✓ Logged in as CUSTOMER
✓ All test accounts ready

========================================
MODULE: Authentication & Authorization
========================================

--- Get Current User Profile ---
✓ Profile endpoint working

========================================
MODULE: Categories
========================================

--- List Categories ---
✓ Categories list: 5 items

========================================
MODULE: Shops
========================================

--- Create/Get Owner Shop ---
✓ Shop ready: shop_abc123
--- List All Shops ---
✓ Found 3 shops

========================================
MODULE: Products
========================================

--- Create Product ---
✓ Product created: prod_xyz789
--- List Shop Products ---
✓ Owner has 2 products

========================================
MODULE: Shopping Cart
========================================

--- Add Item to Cart ---
✓ Added to cart: 1 items
--- Get Cart ---
✓ Cart total: 100000đ

========================================
MODULE: Orders & Payments (Full Flow)
========================================

--- Create Order from Cart ---
✓ Order created: order_123456
--- Create COD Payment ---
✓ COD payment created and marked PAID
--- Owner Confirms Order ---
✓ Order confirmed
--- Owner Prepares Order ---
✓ Order preparing
--- Owner Marks Ready ---
✓ Order ready for pickup

========================================
MODULE: Shippers & Delivery
========================================

--- Shipper Accepts Order ---
✓ Order accepted by shipper
--- Shipper Starts Shipping ---
✓ Order in shipping
--- Shipper Delivers Order ---
✓ Order delivered (payout triggered)

========================================
MODULE: Wallets & Payouts
========================================

--- Check Owner Wallet ---
✓ Owner wallet balance: 85000đ
--- Check Shipper Wallet ---
✓ Shipper wallet balance: 15000đ
--- Get Wallet Ledger ---
✓ Wallet ledger: 2 entries

========================================
TEST SUMMARY
========================================
Passed:  45
Failed:  0
Skipped: 3
========================================
✓ All tests PASSED
```

---

## 🐛 Troubleshooting

### Common Issues

**1. "jq: command not found"**

```bash
# Ubuntu/Debian
sudo apt-get install jq

# macOS
brew install jq

# Windows (Git Bash)
# Download from https://stedolan.github.io/jq/download/
```

**2. "Login failed"**

```bash
# Check Firebase API key
echo $FIREBASE_API_KEY

# Verify backend URL
curl $BASE_URL/health
```

**3. "Account already exists"**

```bash
# Script will auto-login if account exists
# Or manually delete test accounts:
# Firebase Console → Authentication → Users
```

**4. "Test account creation failed"**

```bash
# Check Firebase Auth config
# Ensure email/password auth is enabled
# Verify password requirements (min 6 chars)
```

**5. "Module test skipped"**

- Some tests depend on previous tests succeeding
- Run with `DETAILED=true` to see why tests are skipped
- Example: Cart test needs product, product needs shop

---

## 🎯 Manual Testing (Individual Endpoints)

### Quick Test: Check Backend Health

```bash
curl $BASE_URL/health
```

### Test Authentication

```bash
# Login
curl -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@test.com",
    "password": "Test123!@#",
    "returnSecureToken": true
  }' | jq '.idToken'

# Get profile
TOKEN="<your_token>"
curl -X GET "$BASE_URL/users/me" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

### Test Categories

```bash
curl -X GET "$BASE_URL/categories" | jq '.categories | length'
```

### Test Shop Creation

```bash
curl -X POST "$BASE_URL/shops/owner" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Shop",
    "description": "Testing",
    "phone": "0123456789",
    "address": "123 Test St",
    "categoryId": "cat_123"
  }' | jq '.shop.id'
```

### Test Order Creation

```bash
curl -X POST "$BASE_URL/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": "shop_123",
    "items": [
      {
        "productId": "prod_456",
        "quantity": 2,
        "price": 50000,
        "name": "Test Product"
      }
    ],
    "deliveryAddress": {
      "recipientName": "Test User",
      "phone": "0987654321",
      "address": "456 Test Ave",
      "city": "Ho Chi Minh",
      "district": "District 1"
    },
    "paymentMethod": "cod"
  }' | jq '.order.id'
```

---

## 📈 Performance Benchmarks

### Expected Response Times (Local)

- Auth login: < 500ms
- Get categories: < 100ms
- List shops: < 200ms
- Create order: < 300ms
- Payment creation: < 200ms

### Expected Response Times (Production)

- Auth login: < 1s
- Get categories: < 300ms
- List shops: < 500ms
- Create order: < 800ms
- Payment creation: < 500ms

---

## 🔐 Security Testing

Test scripts automatically verify:

- ✅ Authentication required for protected endpoints
- ✅ Role-based authorization (customer vs owner vs admin)
- ✅ Ownership checks (user can only access own data)
- ✅ Input validation
- ✅ CORS headers

---

## 📝 Test Data Cleanup

After testing, you may want to clean up:

```bash
# Delete test orders (Firebase Console)
# Delete test shops (Firebase Console)
# Delete test users (Firebase Console → Authentication)

# Or use admin API (if implemented)
curl -X DELETE "$BASE_URL/admin/users/test-cleanup" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install dependencies
        run: sudo apt-get install -y jq
      - name: Run integration tests
        env:
          BASE_URL: ${{ secrets.TEST_BASE_URL }}
          FIREBASE_API_KEY: ${{ secrets.FIREBASE_API_KEY }}
        run: |
          cd Backend/scripts
          chmod +x full-integration-test.sh
          ./full-integration-test.sh
```

---

## 📞 Support

If tests fail:

1. Check backend logs: `firebase functions:log`
2. Enable detailed mode: `DETAILED=true`
3. Test individual modules first
4. Verify Firestore indexes: `firebase deploy --only firestore:indexes`
5. Check Firebase Auth configuration

For issues or questions, check:

- Backend deployment status
- Firebase quotas and limits
- Network connectivity
- API key validity
