# Error Codes & API Testing

---

## Error Codes

_(dán nguyên phần “Error Codes” vào đây, gồm bảng standard error codes + custom format)_


### Standard Firebase Error Codes

| Code                  | HTTP Status | Khi nào xảy ra                     |
| --------------------- | ----------- | ---------------------------------- |
| `ok`                  | 200         | Request thành công                 |
| `cancelled`           | 499         | Request bị cancel                  |
| `unknown`             | 500         | Lỗi không xác định                 |
| `invalid-argument`    | 400         | Input không hợp lệ                 |
| `deadline-exceeded`   | 504         | Request timeout                    |
| `not-found`           | 404         | Resource không tồn tại             |
| `already-exists`      | 409         | Resource đã tồn tại                |
| `permission-denied`   | 403         | Không có quyền truy cập            |
| `resource-exhausted`  | 429         | Vượt quá quota/rate limit          |
| `failed-precondition` | 400         | Điều kiện tiên quyết không đáp ứng |
| `aborted`             | 409         | Operation bị hủy (conflict)        |
| `out-of-range`        | 400         | Giá trị ngoài phạm vi              |
| `unimplemented`       | 501         | Chức năng chưa implement           |
| `internal`            | 500         | Lỗi server nội bộ                  |
| `unavailable`         | 503         | Service tạm thời không khả dụng    |
| `data-loss`           | 500         | Mất dữ liệu không thể khôi phục    |
| `unauthenticated`     | 401         | Chưa đăng nhập                     |

### Custom Error Messages

Mỗi error response có format:

```json
{
  "code": "invalid-argument",
  "message": "restaurantId is required",
  "details": {
    "field": "restaurantId",
    "value": null
  }
}
```

---
## Testing APIs

_(dán phần “Testing APIs” vào đây, gồm: Emulator, curl, shell, Android sample, response time, v.v.)_

### 1. Testing với Firebase Emulator

```bash
# Start emulator
firebase emulators:start

# Emulator UI: http://localhost:4000
```

### 2. Testing với cURL

```bash
# Get ID token từ Emulator UI (Authentication tab)
export TOKEN="eyJhbGc..."

# Test placeOrder
curl -X POST http://localhost:5001/PROJECT_ID/us-central1/placeOrder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "data": {
      "restaurantId": "rest_001",
      "items": [{"menuItemId": "item_1", "quantity": 2}],
      "deliveryAddress": {
        "street": "123 Test St",
        "district": "District 1",
        "city": "Ho Chi Minh"
      },
      "paymentMethod": "CASH"
    }
  }'
```

### 3. Testing với Firebase Functions Shell

```bash
npm run shell

> placeOrder({
    restaurantId: 'rest_001',
    items: [{menuItemId: 'item_1', quantity: 2}],
    deliveryAddress: {
      street: '123 Test St',
      district: 'District 1',
      city: 'Ho Chi Minh'
    },
    paymentMethod: 'CASH'
  })
```

### 4. Testing từ Android App

```kotlin
// Get callable function
val functions = Firebase.functions
val placeOrder = functions.getHttpsCallable("placeOrder")

// Prepare data
val data = hashMapOf(
    "restaurantId" to "rest_001",
    "items" to listOf(
        hashMapOf(
            "menuItemId" to "item_1",
            "quantity" to 2
        )
    ),
    "deliveryAddress" to hashMapOf(
        "street" to "123 Test St",
        "district" to "District 1",
        "city" to "Ho Chi Minh"
    ),
    "paymentMethod" to "CASH"
)

// Call function
placeOrder.call(data)
    .addOnSuccessListener { result ->
        val response = result.data as HashMap<*, *>
        val orderId = response["orderId"]
        Log.d("API", "Order created: $orderId")
    }
    .addOnFailureListener { error ->
        Log.e("API", "Error: ${error.message}")
    }
```

### 5. Testing Response Time

```bash
# Measure response time
time curl -X POST http://localhost:5001/.../placeOrder \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"data": {...}}'

# Expected: < 1s for local emulator
# Expected: < 3s for production
```

---

## Related Docs

### 🏗️ Architecture Details
- **[LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md)** - Chi tiết implementation của từng layer với code examples, patterns, và best practices
- **[ADR/](./ADR/)** - Architecture Decision Records
  - [ADR-001: Why Firebase Functions?](./ADR/ADR-001-Why-Firebase-Functions.md)
  - [ADR-002: Layered Architecture](./ADR/ADR-002-Layered-Architecture.md)
  - [ADR-003: No Logic In Triggers](./ADR/ADR-003-No-Logic-In-Triggers.md)

### 📊 Patterns & Practices
- **[EVENTS.md](./EVENTS.md)** - Event-driven architecture, triggers, handlers
- **[ERROR_HANDLING.md](./ERROR_HANDLING.md)** - Error handling patterns, HttpsError mapping

### 🔧 Development
- **[DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)** - Setup, emulator, debugging, CI/CD
- **[FIRESTORE_SCHEMA.md](./FIRESTORE_SCHEMA.md)** - Database schema, indexes, relationships
- **[RULES.md](./RULES.md)** - Firestore Security Rules, role-based access control
