# Các Lỗi Thường Gặp và Cách Khắc Phục

> **Tài liệu này liệt kê các lỗi backend phổ biến khi phát triển Firebase Functions.**

---

## 📖 Mục Lục

- [Cannot find module](#-cannot-find-module)
- [TypeScript compilation errors](#-typescript-compilation-errors)
- [Function timeout](#-function-timeout)
- [Firestore Rules error](#-firestore-rules-error)
- [Authentication errors](#-authentication-errors)
- [Deployment failures](#-deployment-failures)
- [Cloud Functions runtime errors](#-cloud-functions-runtime-errors)

---

## ❌ Cannot find module

### Lỗi

```
Error: Cannot find module '../models/order.model'
Error: Cannot find module 'firebase-admin'
```

### Giải pháp

```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Rebuild TypeScript
npm run build

# Clear cache
npm cache clean --force
npm install
```

---

## ❌ TypeScript compilation errors

### Lỗi

```
error TS2304: Cannot find name 'Order'
error TS2345: Argument of type 'string' is not assignable
error TS2339: Property 'userId' does not exist
```

### Giải pháp

```bash
# Rebuild
rm -rf lib/
npm run build

# Check errors
npx tsc --noEmit
```

**Fix import paths:**

```typescript
// ✅ GOOD
import { Order } from "../models/order.model";

// ❌ BAD
import { Order } from "./models/order.model";
```

**Restart TypeScript server in VS Code:**
- Command Palette (Ctrl+Shift+P)
- "TypeScript: Restart TS Server"

---

## ❌ Function timeout

### Lỗi

```
Error: Function execution took 60001 ms, exceeds timeout of 60000 ms
Error: Deadline exceeded
```

### Giải pháp 1: Increase timeout

```typescript
import { onCall } from "firebase-functions/v2/https";

export const longRunningFunction = onCall(
  { timeoutSeconds: 300 },  // 5 minutes
  async (request) => {
    // Long running operation
  }
);
```

### Giải pháp 2: Optimize queries

```typescript
// ❌ BAD - Fetch all
const allOrders = await orderRepository.getAll();

// ✅ GOOD - Limit & pagination
const orders = await orderRepository.getUserOrders(userId, 20);
```

### Giải pháp 3: Add timeout wrapper

```typescript
function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) =>
      setTimeout(() => reject(new Error("Timeout")), ms)
    ),
  ]);
}

// Usage
const result = await withTimeout(externalApiCall(), 5000);
```

### Giải pháp 4: Batch operations

```typescript
// ❌ BAD - Sequential
for (const item of items) {
  await processItem(item);
}

// ✅ GOOD - Parallel with batch
const chunks = chunkArray(items, 10);
for (const chunk of chunks) {
  await Promise.all(chunk.map(item => processItem(item)));
}
```

---

## ❌ Firestore Rules error

### Lỗi

```
Error: Missing or insufficient permissions
FirebaseError: 7 PERMISSION_DENIED
```

### Nguyên nhân

- Firestore Security Rules từ chối request
- Backend cần bypass rules với Admin SDK

### Giải pháp: Check Admin SDK usage

```typescript
// ✅ GOOD - Admin SDK bypasses rules
import * as admin from "firebase-admin";
admin.initializeApp();

const db = admin.firestore();
await db.collection("orders").add(data);

// ❌ BAD - Client SDK respects rules
import { getFirestore } from "firebase/firestore";
const db = getFirestore();
```

### Test Rules trong Emulator

```bash
# Start emulator
firebase emulators:start

# Test rules trong Emulator UI
http://localhost:4000
```

### Common Rules Issues

```
// ❌ BAD - Too restrictive
match /orders/{orderId} {
  allow read: if request.auth != null;
  allow write: if false;  // Nobody can write!
}

// ✅ GOOD
match /orders/{orderId} {
  allow read: if request.auth != null;
  allow write: if request.auth != null 
                 && request.auth.uid == resource.data.userId;
}
```

---

## ❌ Authentication errors

### Lỗi

```
Error: Unauthenticated
Error: Invalid token
FirebaseError: ID token has expired
```

### Giải pháp 1: Check auth context

```typescript
export const myFunction = onCall(async (request) => {
  const { auth } = request;
  
  // Check authenticated
  if (!auth?.uid) {
    throw new Error("Unauthenticated");
  }
  
  // Check custom claims
  if (auth.token.role !== "ADMIN") {
    throw new Error("Unauthorized - Admin only");
  }
  
  // Proceed
});
```

### Giải pháp 2: Verify token trong HTTP functions

```typescript
import { onRequest } from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

export const httpFunction = onRequest(async (req, res) => {
  const idToken = req.headers.authorization?.split("Bearer ")[1];
  
  if (!idToken) {
    res.status(401).send("Unauthorized");
    return;
  }
  
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    const userId = decodedToken.uid;
    
    // Process request
    res.status(200).send({ success: true });
  } catch (error) {
    res.status(401).send("Invalid token");
  }
});
```

### Giải pháp 3: Set custom claims

```typescript
// In auth trigger
export const onUserCreated = onDocumentCreated(
  "users/{userId}",
  async (event) => {
    const userData = event.data?.data();
    
    await admin.auth().setCustomUserClaims(event.params.userId, {
      role: userData?.role || "BUYER",
    });
  }
);
```

---

## ❌ Deployment failures

### Lỗi 1: Build failed

```
Error: Build failed
Error: TypeScript compilation error
```

**Solution:**

```bash
# Check build locally first
npm run build

# Fix TypeScript errors
npx tsc --noEmit

# Then deploy
firebase deploy --only functions
```

### Lỗi 2: Function not found

```
Error: Function 'placeOrder' not found
```

**Solution: Check export in index.ts**

```typescript
// src/index.ts
export * from "./triggers/api.order";  // Must export!
```

### Lỗi 3: Quota exceeded

```
Error: Quota exceeded for quota metric 'Function deployments'
```

**Solution:**

```bash
# Deploy only changed functions
firebase deploy --only functions:placeOrder

# Delete unused functions first
firebase functions:delete unusedFunction
```

### Lỗi 4: Permission denied

```
Error: HTTP Error: 403, The caller does not have permission
```

**Solution:**

```bash
# Check Firebase project
firebase use

# Re-login
firebase logout
firebase login

# Check IAM permissions trong Firebase Console
```

---

## ❌ Cloud Functions runtime errors

### Lỗi 1: Unhandled promise rejection

```
Error: Unhandled Promise Rejection
Warning: a promise was rejected but not handled
```

**Solution:**

```typescript
// ❌ BAD - No error handling
export const myFunction = onCall(async (request) => {
  const result = await someAsyncOperation();
  return result;
});

// ✅ GOOD - Proper error handling
export const myFunction = onCall(async (request) => {
  try {
    const result = await someAsyncOperation();
    return result;
  } catch (error) {
    logger.error("Function error", { error });
    throw toHttpsError(error);
  }
});
```

### Lỗi 2: Memory limit exceeded

```
Error: Memory limit of 256 MB exceeded
```

**Solution:**

```typescript
import { onCall } from "firebase-functions/v2/https";

// Increase memory
export const heavyFunction = onCall(
  { memory: "512MiB" },  // or "1GiB", "2GiB"
  async (request) => {
    // Heavy operation
  }
);
```

### Lỗi 3: Cold start timeout

```
Error: Function invocation was interrupted
```

**Solution 1: Increase timeout**

```typescript
export const myFunction = onCall(
  { 
    timeoutSeconds: 120,
    memory: "512MiB"
  },
  async (request) => { }
);
```

**Solution 2: Keep instances warm (paid feature)**

```typescript
export const myFunction = onCall(
  { 
    minInstances: 1  // Keep 1 instance always warm
  },
  async (request) => { }
);
```

### Lỗi 4: Firestore write too large

```
Error: Document too large (>1MB)
```

**Solution:**

```typescript
// Split large data into chunks
// Or use Cloud Storage for large files

// ❌ BAD - 2MB document
await db.collection("orders").doc(id).set({
  items: largeArray,  // 2MB
});

// ✅ GOOD - Split into subcollection
await db.collection("orders").doc(id).set({
  itemCount: items.length,
});

const batch = db.batch();
items.forEach((item, index) => {
  const ref = db.collection("orders").doc(id)
    .collection("items").doc(String(index));
  batch.set(ref, item);
});
await batch.commit();
```

### Lỗi 5: Cannot read property of undefined

```
TypeError: Cannot read property 'userId' of undefined
```

**Solution:**

```typescript
// ❌ BAD - No null checks
const userName = user.profile.name;

// ✅ GOOD - Null checks
const userName = user?.profile?.name || "Unknown";

// ✅ GOOD - Explicit check
if (!user || !user.profile) {
  throw new Error("User profile not found");
}
const userName = user.profile.name;
```

---

## 🛠️ Debugging Tips

### Enable verbose logging

```bash
# Local emulator
npm run dev

# Production logs
firebase functions:log --tail

# Specific function logs
firebase functions:log --only placeOrder --tail
```

### Add structured logs

```typescript
import { logger } from "firebase-functions/v2";

export const myFunction = onCall(async (request) => {
  logger.info("Function started", { 
    userId: request.auth?.uid,
    timestamp: Date.now()
  });
  
  try {
    const result = await process();
    logger.info("Function completed", { result });
    return result;
  } catch (error) {
    logger.error("Function failed", { 
      error: error.message,
      stack: error.stack
    });
    throw error;
  }
});
```

### Test trong Functions Shell

```bash
npm run shell

> placeOrder({ restaurantId: 'rest_001', items: [] })
```

---

## 📚 Tài Liệu Liên Quan

- [DEBUGGING.md](./DEBUGGING.md) - Chi tiết debugging workflow
- [EMULATOR_GUIDE.md](./EMULATOR_GUIDE.md) - Firebase Emulator setup
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Error handling patterns
- [Firebase Functions Troubleshooting](https://firebase.google.com/docs/functions/troubleshooting)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
