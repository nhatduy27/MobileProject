# Error Handling Architecture

## 📋 Tổng Quan

**Error Handling (Xử lý lỗi)** là một phần quan trọng của backend architecture. Hệ thống cần:
- **Chuẩn hóa** format của errors
- **Mapping** errors nội bộ sang HTTP error codes
- **Logging** errors với đầy đủ context để debug
- **Trả về** error messages rõ ràng cho client

---

## 🏗️ Kiến Trúc Xử Lý Lỗi

### Mô Hình 3 Lớp

```
┌─────────────────────────────────┐
│    Internal Error               │  Lớp 1: Application throws
│    (Service/Repository)         │  Error("Restaurant not found")
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│    toHttpsError()               │  Lớp 2: Convert & Map
│    (Error Mapping)              │  Map message → Firebase code
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│    HttpsError                   │  Lớp 3: Response to client
│    (Firebase Standard)          │  {code, message}
└─────────────────────────────────┘
```

**Flow:**
1. **Service/Repository** throw Error với descriptive message
2. **Trigger** bắt error, gọi `toHttpsError()` để convert
3. **toHttpsError()** map error message → Firebase error code
4. **HttpsError** được throw ra, Firebase tự động format response
5. **Client** nhận error với code và message chuẩn

---

## 🔧 Standardized Error Format

### Hàm toHttpsError()

**File: `src/utils/error.utils.ts`**

```typescript
import { HttpsError } from "firebase-functions/v2/https";

/**
 * Chuyển đổi internal error thành HttpsError chuẩn
 * 
 * Mapping pattern:
 * - Error message chứa keywords → Firebase error code tương ứng
 * - Default: "internal" nếu không match pattern nào
 * 
 * @param error - Any error object (Error, string, HttpsError, etc.)
 * @returns HttpsError với code và message phù hợp
 */
export function toHttpsError(error: any): HttpsError {
  // Nếu đã là HttpsError, return nguyên
  if (error instanceof HttpsError) {
    return error;
  }

  // Extract message
  const message = error?.message || String(error);

  // Pattern 1: Not Found (404)
  if (
    message.includes("not found") ||
    message.includes("does not exist") ||
    message.includes("not exist")
  ) {
    return new HttpsError("not-found", message);
  }

  // Pattern 2: Unauthenticated (401)
  if (
    message.includes("Unauthenticated") ||
    message.includes("not authenticated") ||
    message.includes("auth required") ||
    message.includes("No authentication")
  ) {
    return new HttpsError("unauthenticated", message);
  }

  // Pattern 3: Permission Denied (403)
  if (
    message.includes("Permission denied") ||
    message.includes("not authorized") ||
    message.includes("access denied") ||
    message.includes("Unauthorized") ||
    message.includes("forbidden")
  ) {
    return new HttpsError("permission-denied", message);
  }

  // Pattern 4: Invalid Argument (400)
  if (
    message.includes("Invalid") ||
    message.includes("required") ||
    message.includes("invalid format") ||
    message.includes("must be") ||
    message.includes("cannot be empty")
  ) {
    return new HttpsError("invalid-argument", message);
  }

  // Pattern 5: Already Exists (409)
  if (
    message.includes("already exists") ||
    message.includes("duplicate") ||
    message.includes("already registered")
  ) {
    return new HttpsError("already-exists", message);
  }

  // Pattern 6: Failed Precondition (400)
  if (
    message.includes("closed") ||
    message.includes("unavailable") ||
    message.includes("not available") ||
    message.includes("prerequisites not met")
  ) {
    return new HttpsError("failed-precondition", message);
  }

  // Pattern 7: Resource Exhausted (429)
  if (
    message.includes("rate limit") ||
    message.includes("too many requests") ||
    message.includes("quota exceeded")
  ) {
    return new HttpsError("resource-exhausted", message);
  }

  // Pattern 8: Database/Internal Errors (500)
  if (
    message.includes("Database error") ||
    message.includes("Firestore error") ||
    message.includes("Internal error")
  ) {
    return new HttpsError("internal", "Database operation failed");
  }

  // Default: Internal Error
  return new HttpsError("internal", "An unexpected error occurred");
}
```

---

## 📜 Firebase HttpsError Codes

### Bảng Mã Lỗi Chuẩn

| Code | HTTP Status | Ý Nghĩa | Khi Nào Dùng | Ví Dụ Message |
|------|-------------|---------|--------------|---------------|
| `ok` | 200 | Thành công | Không bao giờ throw | "Operation successful" |
| `cancelled` | 499 | Request bị hủy | Client cancel request | "Request cancelled by user" |
| `unknown` | 500 | Lỗi không xác định | Unknown error | "Unknown error occurred" |
| `invalid-argument` | 400 | Input không hợp lệ | Validation fails | "restaurantId is required" |
| `deadline-exceeded` | 504 | Timeout | Operation quá lâu | "Request timeout after 60s" |
| `not-found` | 404 | Resource không tồn tại | Get by ID fail | "Restaurant not found" |
| `already-exists` | 409 | Resource đã tồn tại | Duplicate create | "Email already registered" |
| `permission-denied` | 403 | Không có quyền truy cập | Authorization fail | "Permission denied: Not owner" |
| `resource-exhausted` | 429 | Vượt giới hạn | Rate limit, quota | "Rate limit exceeded" |
| `failed-precondition` | 400 | Điều kiện không đủ | Business rule violation | "Restaurant is closed" |
| `aborted` | 409 | Transaction gián đoạn | Concurrent update | "Transaction conflict" |
| `out-of-range` | 400 | Giá trị ngoài phạm vi | Value validation | "Quantity must be 1-100" |
| `unimplemented` | 501 | Chưa implement | Feature not ready | "Feature not available" |
| `internal` | 500 | Lỗi server nội bộ | Server error | "Internal server error" |
| `unavailable` | 503 | Service không khả dụng | Service down | "Service temporarily unavailable" |
| `data-loss` | 500 | Mất dữ liệu | Data corruption | "Data corruption detected" |
| `unauthenticated` | 401 | Chưa xác thực | No auth token | "Unauthenticated" |

---

## 🎯 Pattern Sử Dụng

### 1. Trong Trigger Layer

**Trigger chỉ catch và convert error:**

```typescript
// api.order.ts
import { onCall } from "firebase-functions/v2/https";
import { orderService } from "../services/order.service";
import { toHttpsError, logError } from "../utils/error.utils";
import { PlaceOrderRequest, PlaceOrderResponse } from "../models/order.model";

/**
 * Place Order - Callable Function
 */
export const placeOrder = onCall<
  PlaceOrderRequest,
  Promise<PlaceOrderResponse>
>(async (request) => {
  try {
    const { data, auth } = request;

    // Validate input format (không business logic)
    if (!data.restaurantId) {
      throw new Error("restaurantId is required");
    }

    if (!data.items || data.items.length === 0) {
      throw new Error("items array cannot be empty");
    }

    // Gọi service layer
    const result = await orderService.placeOrder(data, auth);
    return result;
  } catch (error) {
    // Log error với context
    logError("placeOrder", error, {
      userId: request.auth?.uid,
      restaurantId: request.data?.restaurantId,
    });

    // Convert và throw HttpsError
    throw toHttpsError(error);
  }
});
```

**Key points:**
- ✅ Validate input format ở trigger
- ✅ Catch tất cả errors
- ✅ Log với context (trigger name, userId, etc.)
- ✅ Convert error với `toHttpsError()`
- ✅ Throw HttpsError để Firebase format response

---

### 2. Trong Service Layer

**Service throw descriptive errors:**

```typescript
// order.service.ts
import { CallableRequest } from "firebase-functions/v2/https";
import { orderRepository } from "../repositories/order.repository";
import { restaurantRepository } from "../repositories/restaurant.repository";
import { notificationService } from "../services/notification.service";
import { PlaceOrderRequest, PlaceOrderResponse } from "../models/order.model";

type CallableRequestContext = CallableRequest["auth"];

export class OrderService {
  /**
   * Place order - Business logic layer
   */
  async placeOrder(
    data: PlaceOrderRequest,
    context: CallableRequestContext
  ): Promise<PlaceOrderResponse> {
    // 1. Validate authentication
    if (!context?.uid) {
      throw new Error("Unauthenticated");  // → unauthenticated (401)
    }

    const userId = context.uid;

    // 2. Fetch & validate restaurant
    const restaurant = await restaurantRepository.getById(data.restaurantId);
    
    if (!restaurant) {
      throw new Error(`Restaurant ${data.restaurantId} not found`);  // → not-found (404)
    }

    if (!restaurant.isOpen) {
      throw new Error("Permission denied: Restaurant is closed");  // → failed-precondition (400)
    }

    // 3. Validate menu items
    let totalAmount = 0;
    for (const item of data.items) {
      const menuItem = await restaurantRepository.getMenuItem(
        data.restaurantId,
        item.menuItemId
      );

      if (!menuItem) {
        throw new Error(`MenuItem ${item.menuItemId} not found`);  // → not-found (404)
      }

      if (!menuItem.isAvailable) {
        throw new Error(`MenuItem ${item.menuItemId} is not available`);  // → failed-precondition (400)
      }

      if (item.quantity < 1 || item.quantity > 100) {
        throw new Error("Invalid quantity: must be between 1 and 100");  // → invalid-argument (400)
      }

      totalAmount += menuItem.price * item.quantity;
    }

    // 4. Apply promotion if provided
    if (data.promotionCode) {
      const promotion = await promotionRepository.getByCode(
        data.promotionCode
      );

      if (!promotion || !promotion.isActive) {
        throw new Error(`Promotion code ${data.promotionCode} not found`);  // → not-found (404)
      }

      totalAmount *= (1 - promotion.discountPercent / 100);
    }

    // 5. Create order
    const orderId = await orderRepository.create({
      userId,
      restaurantId: data.restaurantId,
      items: data.items,
      status: "PENDING",
      totalAmount,
      deliveryAddress: data.deliveryAddress,
      notes: data.notes,
      promotionCode: data.promotionCode,
      createdAt: new Date().toISOString(),
    });

    // 6. Send notification (không block response nếu fail)
    notificationService
      .sendToSeller({
        restaurantId: data.restaurantId,
        message: `New order: ${orderId}`,
        orderId,
      })
      .catch((error) => {
        console.error("Failed to send notification:", error);
        // Không throw, notification fail không block order creation
      });

    return {
      orderId,
      status: "PENDING",
      totalAmount,
    };
  }
}

// Singleton export
export const orderService = new OrderService();
```

**Key points:**
- ✅ Throw Error với descriptive message
- ✅ Message chứa keywords để map được (not found, Permission denied, Invalid, etc.)
- ✅ Không catch error (để trigger catch)
- ✅ Business validation đầy đủ
- ✅ Non-critical operations (notification) không block

---

### 3. Trong Repository Layer

**Repository catch database errors:**

```typescript
// order.repository.ts
import * as admin from "firebase-admin";
import { Order } from "../models/order.model";

export class OrderRepository {
  private db = admin.firestore();
  private collection = "orders";

  /**
   * Create order document
   */
  async create(data: Omit<Order, "id">): Promise<string> {
    try {
      const docRef = await this.db.collection(this.collection).add({
        ...data,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      return docRef.id;
    } catch (error) {
      // Log chi tiết error
      console.error("[OrderRepository.create] Firestore error:", error);

      // Throw generic error (không expose internal details)
      throw new Error("Database error: Failed to create order");  // → internal (500)
    }
  }

  /**
   * Get order by ID
   */
  async getById(orderId: string): Promise<Order | null> {
    try {
      const doc = await this.db
        .collection(this.collection)
        .doc(orderId)
        .get();

      if (!doc.exists) {
        return null;  // Không throw, return null
      }

      return { id: doc.id, ...doc.data() } as Order;
    } catch (error) {
      console.error("[OrderRepository.getById] Firestore error:", error);
      throw new Error("Database error: Failed to fetch order");  // → internal (500)
    }
  }

  /**
   * Update order status
   */
  async updateStatus(orderId: string, status: string): Promise<void> {
    try {
      await this.db
        .collection(this.collection)
        .doc(orderId)
        .update({
          status,
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });
    } catch (error) {
      console.error("[OrderRepository.updateStatus] Firestore error:", error);

      // Check if document doesn't exist
      if (error.code === "not-found") {
        throw new Error(`Order ${orderId} not found`);  // → not-found (404)
      }

      throw new Error("Database error: Failed to update order");  // → internal (500)
    }
  }
}

// Singleton export
export const orderRepository = new OrderRepository();
```

**Key points:**
- ✅ Try-catch tất cả Firestore operations
- ✅ Log chi tiết error (có context)
- ✅ Throw generic "Database error" (không expose internals)
- ✅ Return null thay vì throw khi document không tồn tại (cho getById)

---

## 📊 Unified Error Logging

### Hàm logError()

**File: `src/utils/error.utils.ts`**

```typescript
/**
 * Log error với context đầy đủ
 * 
 * @param context - Tên trigger/function nơi error xảy ra
 * @param error - Error object
 * @param metadata - Thông tin bổ sung (userId, orderId, etc.)
 */
export function logError(
  context: string,
  error: any,
  metadata?: Record<string, any>
): void {
  const timestamp = new Date().toISOString();
  const message = error?.message || String(error);
  const stack = error?.stack;

  // Structured log (dễ query trong Cloud Logging)
  console.error(
    JSON.stringify({
      timestamp,
      level: "ERROR",
      context,           // placeOrder, updateOrderStatus, etc.
      error: message,
      stack,
      metadata,          // userId, orderId, restaurantId, etc.
    })
  );
}
```

### Sử dụng logError()

```typescript
// Trong trigger
export const placeOrder = onCall(async (request) => {
  try {
    return await orderService.placeOrder(request.data, request.auth);
  } catch (error) {
    // Log với đầy đủ context
    logError("placeOrder", error, {
      userId: request.auth?.uid,
      restaurantId: request.data?.restaurantId,
      itemCount: request.data?.items?.length,
    });

    throw toHttpsError(error);
  }
});
```

**Log Output (Firebase Console):**

```json
{
  "timestamp": "2025-12-07T10:30:45.123Z",
  "level": "ERROR",
  "context": "placeOrder",
  "error": "Restaurant rest_123 not found",
  "stack": "Error: Restaurant rest_123 not found\n    at OrderService.placeOrder (...)",
  "metadata": {
    "userId": "user_456",
    "restaurantId": "rest_123",
    "itemCount": 2
  }
}
```

---

## 🎯 Error Handling Best Practices

### DO ✅

```typescript
// ✅ GOOD - Descriptive error message
if (!restaurant) {
  throw new Error("Restaurant not found");
}

// ✅ GOOD - Include context in error
if (!restaurant.isOpen) {
  throw new Error(`Permission denied: Restaurant ${restaurant.id} is closed`);
}

// ✅ GOOD - Validate early in service
async placeOrder(data, context) {
  if (!context?.uid) {
    throw new Error("Unauthenticated");
  }
  // ... business logic
}

// ✅ GOOD - Log với context đầy đủ
logError("placeOrder", error, {
  userId: request.auth?.uid,
  restaurantId: request.data?.restaurantId
});

// ✅ GOOD - Catch database errors trong repository
try {
  await this.db.collection("orders").add(data);
} catch (error) {
  console.error("[OrderRepository] Firestore error:", error);
  throw new Error("Database error: Failed to create order");
}

// ✅ GOOD - Non-critical operations không block
notificationService.sendToSeller(...)
  .catch(error => {
    console.error("Notification failed:", error);
    // Không throw
  });
```

### DON'T ❌

```typescript
// ❌ BAD - Generic error message
if (!restaurant) {
  throw new Error("Error");  // Không rõ ràng
}

// ❌ BAD - Không validate authentication
async placeOrder(data, context) {
  // Không check context.uid
  const order = await orderRepository.create({
    userId: context.uid  // Có thể undefined → lỗi sau
  });
}

// ❌ BAD - Return error thay vì throw
if (!restaurant) {
  return { error: "Not found" };  // Sai pattern
}

// ❌ BAD - Expose internal details
catch (error) {
  throw new Error(`Database connection failed: ${error.message}`);
  // Expose cấu trúc database
}

// ❌ BAD - Catch error nhưng không log
try {
  await orderRepository.create(data);
} catch (error) {
  throw error;  // Không log → khó debug
}

// ❌ BAD - Critical operation trong try-catch không throw
try {
  await orderRepository.create(data);
} catch (error) {
  console.error(error);
  // Không throw → function tiếp tục chạy với data lỗi
}
```

---

## 📈 Error Response Examples

### Example 1: Restaurant Not Found

**Service throws:**
```typescript
throw new Error("Restaurant rest_123 not found");
```

**toHttpsError() converts:**
```typescript
new HttpsError("not-found", "Restaurant rest_123 not found");
```

**Client receives:**
```json
{
  "error": {
    "code": "not-found",
    "message": "Restaurant rest_123 not found",
    "status": "NOT_FOUND"
  }
}
```

**HTTP Status:** 404

---

### Example 2: Unauthenticated

**Service throws:**
```typescript
throw new Error("Unauthenticated");
```

**toHttpsError() converts:**
```typescript
new HttpsError("unauthenticated", "Unauthenticated");
```

**Client receives:**
```json
{
  "error": {
    "code": "unauthenticated",
    "message": "Unauthenticated",
    "status": "UNAUTHENTICATED"
  }
}
```

**HTTP Status:** 401

---

### Example 3: Invalid Input

**Trigger throws:**
```typescript
throw new Error("restaurantId is required");
```

**toHttpsError() converts:**
```typescript
new HttpsError("invalid-argument", "restaurantId is required");
```

**Client receives:**
```json
{
  "error": {
    "code": "invalid-argument",
    "message": "restaurantId is required",
    "status": "INVALID_ARGUMENT"
  }
}
```

**HTTP Status:** 400

---

### Example 4: Permission Denied

**Service throws:**
```typescript
throw new Error("Permission denied: Not restaurant owner");
```

**toHttpsError() converts:**
```typescript
new HttpsError("permission-denied", "Permission denied: Not restaurant owner");
```

**Client receives:**
```json
{
  "error": {
    "code": "permission-denied",
    "message": "Permission denied: Not restaurant owner",
    "status": "PERMISSION_DENIED"
  }
}
```

**HTTP Status:** 403

---

## 🐛 Debugging Errors

### 1. Xem Logs Trong Firebase Console

```bash
# View recent logs
npm run logs

# View logs của specific function
npm run logs -- --only placeOrder

# Tail logs real-time
firebase functions:log --only placeOrder --tail

# Filter by severity
firebase functions:log --only placeOrder --min-log-level error
```

### 2. Structured Logging Query

**Cloud Logging Query:**

```
resource.type="cloud_function"
resource.labels.function_name="placeOrder"
severity="ERROR"
jsonPayload.context="placeOrder"
timestamp>"2025-12-07T00:00:00Z"
```

### 3. Add Request Correlation ID

```typescript
import { v4 as uuidv4 } from "uuid";

export const placeOrder = onCall(async (request) => {
  const requestId = uuidv4();  // Generate unique ID

  console.log("[REQUEST]", { requestId, userId: request.auth?.uid });

  try {
    const result = await orderService.placeOrder(request.data, request.auth);
    console.log("[SUCCESS]", { requestId, orderId: result.orderId });
    return result;
  } catch (error) {
    logError("placeOrder", error, {
      requestId,  // Include in error log
      userId: request.auth?.uid,
      restaurantId: request.data?.restaurantId,
    });
    throw toHttpsError(error);
  }
});
```

**Benefits:**
- Trace toàn bộ request từ đầu đến cuối
- Dễ tìm logs related bằng requestId
- Debug async operations

---

## 📚 Common Error Scenarios

### Scenario 1: User Chưa Đăng Nhập

```typescript
// Service check
if (!context?.uid) {
  throw new Error("Unauthenticated");
}

// → unauthenticated (401)
// Client: Redirect to login page
```

### Scenario 2: Resource Không Tồn Tại

```typescript
// Service check
const restaurant = await restaurantRepository.getById(restaurantId);
if (!restaurant) {
  throw new Error(`Restaurant ${restaurantId} not found`);
}

// → not-found (404)
// Client: Show "Restaurant not found" message
```

### Scenario 3: Không Có Quyền

```typescript
// Service check
if (order.userId !== context.uid) {
  throw new Error("Permission denied: Not your order");
}

// → permission-denied (403)
// Client: Show "Access denied" message
```

### Scenario 4: Input Validation Fail

```typescript
// Trigger validation
if (!data.restaurantId) {
  throw new Error("restaurantId is required");
}

// → invalid-argument (400)
// Client: Highlight input field error
```

### Scenario 5: Business Rule Violation

```typescript
// Service check
if (!restaurant.isOpen) {
  throw new Error("Permission denied: Restaurant is closed");
}

// → failed-precondition (400)
// Client: Show "Restaurant closed" message
```

---

## 🔗 Tài Liệu Liên Quan

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc layered
- [EVENTS.md](./EVENTS.md) - Event-driven architecture
- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển
- [Firebase HttpsError Documentation](https://firebase.google.com/docs/reference/node/firebase.functions.https.HttpsError) - Official docs

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
