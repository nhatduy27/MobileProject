# Test Accounts - KTX Delivery

> 📅 **Updated:** 2026-01-08

## Available Test Accounts

| Email | Password | Role | Status | Notes |
|-------|----------|------|--------|-------|
| `customer1@test.com` | `Test123!` | CUSTOMER | ACTIVE | Default test customer |
| `customer2@test.com` | `Test123!` | CUSTOMER | ACTIVE | Secondary customer |
| `owner1@test.com` | `Test123!` | OWNER | ACTIVE | Shop owner (chưa có shop) |
| `shipper1@test.com` | `Test123!` | SHIPPER | ACTIVE | Shipper (chưa đăng ký shop) |
| `admin@ktx.com` | `Admin123!` | ADMIN | ACTIVE | Administrator |

---

## How to Get ID Token

### Option 1: Using Script

```bash
cd Backend/functions
node get-id-token.js customer1@test.com Test123!
```

### Option 2: Using Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select project "ktx-delivery"
3. Authentication > Users > Find user
4. Copy UID and use Admin SDK to generate custom token

---

## Create New Test Account

### Via API

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@test.com",
    "password": "Test123!",
    "displayName": "New User",
    "phone": "0901234567",
    "role": "CUSTOMER"
  }'
```

### Via Seed Script

```bash
cd Backend/functions
node scripts/seed-test-users.js
```

---

## Testing Protected APIs

```bash
# 1. Get token
TOKEN=$(node get-id-token.js customer1@test.com Test123! 2>/dev/null | tail -1)

# 2. Use token
curl -X GET http://localhost:3000/api/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Reset Test Data

```bash
# Clear Firestore emulator data
firebase emulators:start --only firestore --clear

# Or delete specific collections
# (Use Firebase Console or Admin SDK)
```

---

## Notes

- Passwords phải có ít nhất 6 ký tự
- Phone format: 10 digits (VN), sẽ được normalize thành +84
- Email phải unique trong hệ thống
- Role không thể thay đổi sau khi tạo (trừ Admin)
