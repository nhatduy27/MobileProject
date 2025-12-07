# Hướng Dẫn Debugging Backend

> **Tài liệu này hướng dẫn cách debug và troubleshoot các vấn đề trong Firebase Cloud Functions.**

---

## 📖 Mục Lục

- [Debugging Authentication trong Emulator](#-debugging-authentication-trong-emulator)
- [Debugging Cloud Functions](#-debugging-cloud-functions)
- [Tips cho Tracing Async Errors](#-tips-cho-tracing-async-errors)
- [Interactive Debugging](#-interactive-debugging)
- [Log Levels](#-log-levels)

---

## 🔐 Debugging Authentication trong Emulator

### 1. Tạo Test Users

#### Cách 1: Qua Firebase Emulator UI

```bash
# Khởi động emulator
firebase emulators:start

# Mở UI: http://localhost:4000
# Vào tab "Authentication"
# Click "Add user" và điền thông tin:
```

| Field | Example Value |
|-------|---------------|
| Email | `buyer@test.com` |
| Password | `password123` |
| UID | `test_buyer_001` (auto-generate) |

#### Cách 2: Programmatically (Script)

```typescript
// scripts/seed-users.ts
import * as admin from "firebase-admin";

admin.initializeApp();

async function createTestUsers() {
  const testUsers = [
    {
      uid: "test_buyer_001",
      email: "buyer@test.com",
      password: "password123",
      role: "BUYER",
    },
    {
      uid: "test_seller_001",
      email: "seller@test.com",
      password: "password123",
      role: "SELLER",
    },
    {
      uid: "test_shipper_001",
      email: "shipper@test.com",
      password: "password123",
      role: "SHIPPER",
    },
  ];

  for (const user of testUsers) {
    try {
      // Tạo user trong Firebase Auth
      await admin.auth().createUser({
        uid: user.uid,
        email: user.email,
        password: user.password,
        emailVerified: true,
      });

      // Set custom claims (role)
      await admin.auth().setCustomUserClaims(user.uid, {
        role: user.role,
      });

      // Tạo user document trong Firestore
      await admin.firestore().collection("users").doc(user.uid).set({
        email: user.email,
        role: user.role,
        displayName: user.role + " Test User",
        isActive: true,
        isVerified: true,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`✅ Created user: ${user.email} (${user.role})`);
    } catch (error) {
      console.error(`❌ Error creating ${user.email}:`, error);
    }
  }
}

createTestUsers().then(() => {
  console.log("✅ All test users created!");
  process.exit(0);
});
```

**Chạy script:**

```bash
# Build script
npx ts-node scripts/seed-users.ts

# Hoặc thêm vào package.json
npm run seed:users
```

### 2. Verify Auth Token từ Emulator

**Lấy Auth Token:**

1. Truy cập Firebase Emulator UI: http://localhost:4000/auth
2. Chọn user và copy ID Token

**Test với cURL:**

```bash
# Test function với auth token
curl -X POST http://localhost:5001/PROJECT_ID/us-central1/placeOrder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ID_TOKEN" \
  -d '{
    "data": {
      "restaurantId": "rest_001",
      "items": [{"menuItemId": "item_1", "quantity": 2}]
    }
  }'
```

### 3. Test Authentication với Android App

**Android Configuration (MainActivity.kt):**

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Connect to emulators (development only)
        if (BuildConfig.DEBUG) {
            // Auth Emulator
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            
            // Firestore Emulator
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
            
            // Functions Emulator
            FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
        }
    }
}
```

**⚠️ Lưu ý quan trọng:**

```
📱 Android Emulator: Dùng IP 10.0.2.2 thay vì localhost
💻 Physical Device: Dùng IP máy host (e.g., 192.168.1.10)
```

**Test Authentication trong Android:**

```kotlin
// Sign in test user
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword("buyer@test.com", "password123")
    .addOnSuccessListener { result ->
        Log.d("Auth", "✅ Signed in: ${result.user?.uid}")
    }
    .addOnFailureListener { error ->
        Log.e("Auth", "❌ Sign in failed: ${error.message}")
    }

// Get custom claims (role)
FirebaseAuth.getInstance().currentUser?.getIdToken(false)
    ?.addOnSuccessListener { tokenResult ->
        val role = tokenResult.claims["role"]
        Log.d("Auth", "✅ User role: $role")
    }
```

### 4. Verify Auth Trigger

**Kiểm tra Auth Trigger hoạt động:**

```bash
# 1. Tạo user qua Emulator UI hoặc script
# 2. Check logs trong terminal
npm run dev

# Output sẽ hiện:
# ℹ️ functions[us-central1-onUserCreated]: function triggered
# ℹ️ Creating user document for uid: test_buyer_001

# 3. Verify user document trong Firestore Emulator UI
# http://localhost:4000/firestore
# Collection: users
# Document ID: test_buyer_001
```

**Check Custom Claims:**

```bash
# Sử dụng Firebase Admin SDK trong Functions shell
npm run shell

> admin.auth().getUser('test_buyer_001').then(u => console.log(u.customClaims))
# Output: { role: 'BUYER' }
```

---

## ⚙️ Debugging Cloud Functions

### 1. Sử dụng Emulator Logs

**Real-time Logs trong Terminal:**

```bash
# Start emulator với logs
npm run dev

# Output sẽ hiện:
# ⚡  functions: Loaded functions: placeOrder, updateOrderStatus...
# ℹ️  functions[us-central1-placeOrder]: http://localhost:5001/...
```

**Structured Logging:**

```typescript
import { logger } from "firebase-functions/v2";

export const processOrder = onCall(async (request) => {
  // INFO level
  logger.info("Processing order", {
    userId: request.auth?.uid,
    orderId: request.data.orderId,
  });
  
  // DEBUG level (only in development)
  logger.debug("Order details", {
    items: request.data.items,
    totalAmount: request.data.totalAmount,
  });
  
  try {
    const result = await orderService.process(request.data);
    
    // SUCCESS log
    logger.info("Order processed successfully", {
      orderId: result.orderId,
      processingTime: Date.now() - startTime,
    });
    
    return result;
  } catch (error) {
    // ERROR log
    logger.error("Order processing failed", {
      error: error.message,
      stack: error.stack,
      userId: request.auth?.uid,
    });
    
    throw error;
  }
});
```

**Filtering Logs:**

```bash
# Chỉ hiện errors
npm run dev 2>&1 | grep "ERROR"

# Chỉ hiện logs của 1 function
npm run dev 2>&1 | grep "placeOrder"

# Save logs to file
npm run dev > functions.log 2>&1
```

### 2. Firebase Console Logs (Deployed Functions)

**Xem logs của deployed functions:**

```bash
# View recent logs
npm run logs

# View logs của specific function
npm run logs -- --only placeOrder

# View logs trong time range
firebase functions:log --only placeOrder --since 1h

# Tail logs (real-time)
firebase functions:log --only placeOrder --tail
```

**Firebase Console UI:**

1. Truy cập: https://console.firebase.google.com
2. Chọn project
3. Functions → Logs tab
4. Filter by:
   - Function name
   - Severity (Info, Warning, Error)
   - Time range
   - Text search

**Cloud Logging (Advanced):**

1. Firebase Console → Functions → View in Cloud Logging
2. Query với Logging Query Language:

```
resource.type="cloud_function"
resource.labels.function_name="placeOrder"
severity="ERROR"
timestamp>"2025-12-07T00:00:00Z"
```

**Log Analysis:**

```typescript
// ✅ GOOD - Searchable structured logs
logger.info("Order placed", {
  orderId: order.id,
  userId: context.uid,
  restaurantId: order.restaurantId,
  totalAmount: order.totalAmount,
  itemCount: order.items.length,
});

// ❌ BAD - Unstructured text
console.log("Order placed: " + order.id);
```

---

## 🔍 Tips cho Tracing Async Errors

### Tip 1: Request Correlation ID

```typescript
import { v4 as uuidv4 } from "uuid";

export const placeOrder = onCall(async (request) => {
  // Generate unique request ID
  const requestId = uuidv4();
  
  logger.info("Request started", { requestId, userId: request.auth?.uid });
  
  try {
    // Pass requestId through service layers
    const result = await orderService.placeOrder(request.data, {
      userId: request.auth!.uid,
      requestId,
    });
    
    logger.info("Request completed", { requestId, orderId: result.orderId });
    return result;
  } catch (error) {
    logger.error("Request failed", { requestId, error: error.message });
    throw error;
  }
});

// Service layer
async placeOrder(data: PlaceOrderRequest, context: { userId: string; requestId: string }) {
  logger.info("Service: Processing order", { requestId: context.requestId });
  
  // Repository layer
  const orderId = await orderRepository.create({
    ...orderData,
    metadata: { requestId: context.requestId },
  });
  
  logger.info("Service: Order created", { requestId: context.requestId, orderId });
  return { orderId };
}
```

### Tip 2: Promise Chain Debugging

```typescript
// ✅ GOOD - Clear error stack
export const complexOperation = onCall(async (request) => {
  try {
    // Step 1
    logger.info("Step 1: Validating order");
    const order = await orderRepository.getById(request.data.orderId);
    if (!order) throw new Error("Order not found");
    
    // Step 2
    logger.info("Step 2: Processing payment");
    const payment = await paymentService.process(order.totalAmount);
    
    // Step 3
    logger.info("Step 3: Updating order status");
    await orderRepository.updateStatus(order.id, "PAID");
    
    // Step 4
    logger.info("Step 4: Sending notification");
    await notificationService.sendOrderConfirmation(order.userId);
    
    logger.info("Operation completed successfully");
    return { success: true };
  } catch (error) {
    logger.error("Operation failed at step", {
      error: error.message,
      stack: error.stack,
    });
    throw error;
  }
});

// ❌ BAD - Unclear error location
export const complexOperation = onCall(async (request) => {
  const order = await orderRepository.getById(request.data.orderId);
  const payment = await paymentService.process(order.totalAmount);
  await orderRepository.updateStatus(order.id, "PAID");
  await notificationService.sendOrderConfirmation(order.userId);
  return { success: true };
  // Nếu lỗi ở step nào? Không biết!
});
```

### Tip 3: Timeout Debugging

```typescript
// Add timeout wrapper
function withTimeout<T>(
  promise: Promise<T>,
  timeoutMs: number,
  operationName: string
): Promise<T> {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) =>
      setTimeout(() => reject(new Error(`${operationName} timeout after ${timeoutMs}ms`)), timeoutMs)
    ),
  ]);
}

// Usage
export const slowOperation = onCall(async (request) => {
  try {
    // Set timeout cho external API call
    const result = await withTimeout(
      externalApiService.call(),
      5000, // 5 seconds
      "External API call"
    );
    
    return result;
  } catch (error) {
    if (error.message.includes("timeout")) {
      logger.error("Operation timed out", {
        operation: "External API call",
        timeout: 5000,
      });
    }
    throw error;
  }
});
```

### Tip 4: Error Context Preservation

```typescript
// Custom error class với context
class AppError extends Error {
  constructor(
    message: string,
    public code: string,
    public context?: Record<string, any>
  ) {
    super(message);
    this.name = "AppError";
  }
}

// Service layer
async processPayment(orderId: string, amount: number) {
  try {
    const charge = await stripeService.createCharge(amount);
    return charge;
  } catch (error) {
    // Wrap error với context
    throw new AppError(
      "Payment processing failed",
      "PAYMENT_FAILED",
      {
        orderId,
        amount,
        originalError: error.message,
        provider: "Stripe",
      }
    );
  }
}

// Function layer
export const processPayment = onCall(async (request) => {
  try {
    const result = await paymentService.processPayment(
      request.data.orderId,
      request.data.amount
    );
    return result;
  } catch (error) {
    if (error instanceof AppError) {
      logger.error("Payment failed", {
        code: error.code,
        message: error.message,
        context: error.context,
      });
    }
    throw toHttpsError(error);
  }
});
```

### Tip 5: Async Stack Traces (Node.js)

```typescript
// Enable long stack traces in development
if (process.env.NODE_ENV === "development") {
  Error.stackTraceLimit = 50; // Default is 10
}

// Or use async-hook library
import { createHook, executionAsyncId } from "async_hooks";

const asyncHook = createHook({
  init(asyncId, type, triggerAsyncId) {
    // Track async operation context
  },
});

if (process.env.NODE_ENV === "development") {
  asyncHook.enable();
}
```

---

## 🧪 Interactive Debugging

### Cách 1: Logs trong Dev Console

```bash
npm run dev

# Logs sẽ hiển thị trong terminal
```

### Cách 2: Interactive Shell

```bash
npm run shell

> placeOrder({
    restaurantId: 'rest_001',
    items: [{menuItemId: 'item_1', quantity: 1}]
  })

# Response sẽ hiển thị ngay
```

### Cách 3: Debugging Code với Breakpoints

```typescript
// Thêm console.log strategically
export const myFunction = onCall(async (request) => {
  console.log("Request received:", request.data);
  
  try {
    const result = await service.process(request.data);
    console.log("Result:", result);
    return result;
  } catch (error) {
    console.error("Error:", error);
    throw error;
  }
});
```

**Xem logs:**

```bash
# Dev console (real-time)
npm run dev

# Firebase remote logs
npm run logs
```

### Cách 4: Error Handling Best Practices

```typescript
// ✅ GOOD - Descriptive error với context
if (!user) {
  throw new Error(`User not found with id: ${userId}`);
}

// ❌ BAD - Generic error
if (!user) {
  throw new Error("Error");
}

// ✅ GREAT - Error với actionable info
if (!user) {
  throw new AppError(
    "User not found",
    "USER_NOT_FOUND",
    { userId, operation: "getProfile" }
  );
}
```

---

## 📊 Log Levels

### Sử dụng Log Levels

```typescript
import { logger } from "firebase-functions/v2";

// INFO - Thông tin quan trọng
logger.info("User created:", { userId });

// WARN - Cảnh báo
logger.warn("Order processing slow:", { processingTime: processingTime + "ms" });

// ERROR - Lỗi
logger.error("Payment failed:", { error });

// DEBUG - Chi tiết (có thể remove sau)
logger.debug("Processing items:", { items });
```

### Best Practices

```typescript
// ✅ GOOD - Rõ ràng, có context
logger.info("[placeOrder] Order created:", {
  orderId: order.id,
  userId: context.uid,
  restaurantId: order.restaurantId,
  totalAmount: order.totalAmount,
});

// ❌ BAD - Không rõ ràng
console.log("Order:", order);
console.log("Done");
```

---

## 📚 Tài Liệu Liên Quan

- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Hướng dẫn phát triển chính
- [EMULATOR_GUIDE.md](./EMULATOR_GUIDE.md) - Hướng dẫn sử dụng Firebase Emulator
- [COMMON_ERRORS.md](./COMMON_ERRORS.md) - Các lỗi thường gặp và cách khắc phục
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Chiến lược xử lý lỗi trong hệ thống
- [Firebase Functions Logging](https://firebase.google.com/docs/functions/writing-and-viewing-logs)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
