# Test Scripts

## Available Test Scripts

### 🎯 Full Integration Test

**File:** `full-integration-test.sh`  
**Purpose:** Complete end-to-end testing of all implemented modules

Tests all major flows:

- User registration and authentication
- Shop and product management
- Shopping cart operations
- Order creation and lifecycle
- Payment processing (COD + SEPAY)
- Wallet and payout tracking
- Shipper workflows
- Admin operations

**Usage:**

```bash
./full-integration-test.sh

# With options
DETAILED=true STOP_ON_ERROR=true ./full-integration-test.sh
```

---

### 💨 Quick Test

**File:** `quick-test.sh`  
**Purpose:** Fast testing of individual modules

**Usage:**

```bash
./quick-test.sh <module>

# Examples
./quick-test.sh categories
./quick-test.sh shops
./quick-test.sh wallets
./quick-test.sh admin
./quick-test.sh health
```

**Available modules:**

- `categories` - List categories
- `shops` - Get shop details
- `products` - List products
- `cart` - Get shopping cart
- `orders` - List orders
- `wallets` - Check wallet balance
- `admin` - Dashboard stats
- `health` - Backend health check

---

### 🔥 Smoke Test

**File:** `smoke-test.sh`  
**Purpose:** Basic COD and SEPAY payment flow validation

Tests:

- COD: Order → Payment → Confirm → Deliver → Payout
- SEPAY: QR generation → Polling verification

**Usage:**

```bash
./smoke-test.sh

# Enable SEPAY test
SEPAY_ENABLED=true ./smoke-test.sh
```

---

## Environment Variables

```bash
# Backend URL (required)
export BASE_URL="http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api"

# Firebase config (required)
export FIREBASE_API_KEY="AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA"

# Test accounts (auto-created if not exist)
export EMAIL_CUSTOMER="customer.test@ktx.com"
export PASS_CUSTOMER="Test123!@#"
export EMAIL_OWNER="owner.test@ktx.com"
export PASS_OWNER="Test123!@#"
export EMAIL_SHIPPER="shipper.test@ktx.com"
export PASS_SHIPPER="Test123!@#"
export EMAIL_ADMIN="admin@ktx.com"
export PASS_ADMIN="Admin123!@#"

# Test options
export DETAILED="false"         # Show full responses
export STOP_ON_ERROR="false"    # Stop on first failure
export SEPAY_ENABLED="false"    # Enable SEPAY flow test
```

---

## Quick Start

### 1. Install Dependencies

```bash
# Ubuntu/Debian
sudo apt-get install jq curl

# macOS
brew install jq curl

# Windows (Git Bash)
# jq: https://stedolan.github.io/jq/download/
# curl: Built-in with Git Bash
```

### 2. Start Backend

```bash
cd ../functions
npm run serve
# Wait for: "All functions initialized"
```

### 3. Run Tests

```bash
cd ../scripts

# Quick health check
./quick-test.sh health

# Test specific module
./quick-test.sh categories

# Full integration test
./full-integration-test.sh

# Smoke test (payment flows)
./smoke-test.sh
```

---

## Test Output Examples

### Quick Test

```bash
$ ./quick-test.sh categories
[10:30:15] Testing Categories...
✓ Categories OK
{
  "categories": [
    {
      "id": "cat_123",
      "name": "Food",
      "icon": "🍔"
    }
  ],
  "total": 5
}
```

### Full Integration Test

```bash
$ ./full-integration-test.sh
========================================
KTX Delivery - Full Integration Test Suite
========================================

========================================
SETUP: Test Accounts
========================================
[10:30:15] Logging in as CUSTOMER: customer.test@ktx.com
✓ Logged in as CUSTOMER
✓ All test accounts ready

========================================
MODULE: Authentication & Authorization
========================================
✓ Profile endpoint working

========================================
MODULE: Categories
========================================
✓ Categories list: 5 items

...

========================================
TEST SUMMARY
========================================
Passed:  48
Failed:  0
Skipped: 2
========================================
✓ All tests PASSED
```

---

## Troubleshooting

### Backend not responding

```bash
# Check backend is running
curl $BASE_URL/health

# Check Firebase emulator
lsof -i :5001
```

### Login failures

```bash
# Verify API key
echo $FIREBASE_API_KEY

# Test Firebase Auth directly
curl -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!@#","returnSecureToken":true}'
```

### Test account creation fails

- Check Firebase Console → Authentication → Settings
- Ensure Email/Password provider is enabled
- Verify password requirements (min 6 chars)

### Module tests skipped

- Some tests depend on previous tests
- Run with `DETAILED=true` to see reasons
- Example: Cart needs product, product needs shop

---

## CI/CD Integration

### GitHub Actions

```yaml
name: Integration Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup
        run: |
          sudo apt-get install -y jq
          cd Backend/functions
          npm install
      - name: Run Tests
        env:
          BASE_URL: ${{ secrets.TEST_URL }}
          FIREBASE_API_KEY: ${{ secrets.FIREBASE_KEY }}
        run: |
          cd Backend/scripts
          ./full-integration-test.sh
```

---

## Performance Benchmarks

### Local Emulator (Expected)

- Health check: < 50ms
- Login: < 500ms
- List endpoints: < 200ms
- Create operations: < 300ms
- Full test suite: < 30s

### Production (Expected)

- Health check: < 200ms
- Login: < 1s
- List endpoints: < 500ms
- Create operations: < 800ms
- Full test suite: < 60s

---

## Next Steps

1. **Run quick health check:**

   ```bash
   ./quick-test.sh health
   ```

2. **Test authentication:**

   ```bash
   ./quick-test.sh categories
   ```

3. **Run full suite:**

   ```bash
   ./full-integration-test.sh
   ```

4. **Review logs:**

   ```bash
   firebase functions:log --limit 50
   ```

5. **Fix any failures and re-run**

---

For detailed documentation, see [TESTING_GUIDE.md](../TESTING_GUIDE.md)
