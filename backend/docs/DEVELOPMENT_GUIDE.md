# Hướng Dẫn Phát Triển Backend

> **Hướng dẫn onboarding cho developers mới và workflow phát triển Firebase Cloud Functions.**

---

## 📖 Mục Lục

- [Bắt Đầu Nhanh](#-bắt-đầu-nhanh)
- [Cách Tạo Cloud Function Mới](#-cách-tạo-cloud-function-mới)
- [Tổ Chức Thư Mục](#-tổ-chức-thư-mục)
- [Useful Commands](#-useful-commands)
- [Workflow Làm Việc](#-workflow-làm-việc)
- [Tài Liệu Liên Quan](#-tài-liệu-liên-quan)

---

## 🚀 Bắt Đầu Nhanh

### 1. Setup môi trường lần đầu

```bash
# Clone repo
git clone https://github.com/nhatduy27/MobileProject.git
cd MobileProject/backend/functions

# Install dependencies
npm install

# Login to Firebase (nếu chưa)
firebase login

# Set active project
firebase use --add
```

### 2. Chạy dev server (hot reload)

```bash
# Start Functions emulator với hot reload
npm run dev
```

**Output:**

```
⚡  functions: Loaded functions definitions from source.
⚡  functions[us-central1-placeOrder]: http function initialized.

✔  functions: Emulator started at http://localhost:5001
i  functions: Watching "..." for Functions...
```

**Benefits:**
- ✅ Tự động compile TypeScript
- ✅ Tự động reload functions
- ✅ Fast feedback loop

---

## 🔨 Cách Tạo Cloud Function Mới

**Quy trình:** Model → Repository → Service → Trigger → Export

### Step 1: Tạo Model

**File: `src/models/payment.model.ts`**

```typescript
export interface Payment {
  id: string;
  orderId: string;
  amount: number;
  method: "CASH" | "CARD" | "WALLET";
  status: "PENDING" | "COMPLETED" | "FAILED";
  createdAt: string;
}

export interface ProcessPaymentRequest {
  orderId: string;
  amount: number;
  method: "CASH" | "CARD" | "WALLET";
}

export interface ProcessPaymentResponse {
  paymentId: string;
  status: "COMPLETED" | "FAILED";
  message: string;
}
```

### Step 2: Tạo Repository

**File: `src/repositories/payment.repository.ts`**

```typescript
import * as admin from "firebase-admin";
import { Payment } from "../models/payment.model";

export class PaymentRepository {
  private db = admin.firestore();

  async create(data: Omit<Payment, "id">): Promise<string> {
    const docRef = await this.db.collection("payments").add(data);
    return docRef.id;
  }

  async getById(id: string): Promise<Payment | null> {
    const doc = await this.db.collection("payments").doc(id).get();
    if (!doc.exists) return null;
    return { id: doc.id, ...doc.data() } as Payment;
  }

  async updateStatus(id: string, status: string): Promise<void> {
    await this.db.collection("payments").doc(id).update({ status });
  }
}

export const paymentRepository = new PaymentRepository();
```

**💡 Xem template đầy đủ tại [CODING_GUIDELINES.md](./CODING_GUIDELINES.md#repository-template)**

### Step 3: Tạo Service

**File: `src/services/payment.service.ts`**

```typescript
import { CallableRequest } from "firebase-functions/v2/https";
import { ProcessPaymentRequest, ProcessPaymentResponse } from "../models/payment.model";
import { paymentRepository } from "../repositories/payment.repository";

export class PaymentService {
  async processPayment(
    data: ProcessPaymentRequest,
    context: CallableRequest["auth"]
  ): Promise<ProcessPaymentResponse> {
    // 1. Validate auth
    if (!context?.uid) throw new Error("Unauthenticated");

    // 2. Process logic
    const paymentId = await paymentRepository.create({
      orderId: data.orderId,
      amount: data.amount,
      method: data.method,
      status: "COMPLETED",
      createdAt: new Date().toISOString(),
    });

    return {
      paymentId,
      status: "COMPLETED",
      message: "Payment successful",
    };
  }
}

export const paymentService = new PaymentService();
```

**💡 Xem template đầy đủ tại [CODING_GUIDELINES.md](./CODING_GUIDELINES.md#service-template)**

### Step 4: Tạo Trigger

**File: `src/triggers/api.payment.ts`**

```typescript
import { onCall } from "firebase-functions/v2/https";
import { paymentService } from "../services/payment.service";
import { ProcessPaymentRequest, ProcessPaymentResponse } from "../models/payment.model";
import { toHttpsError, logError } from "../utils/error.utils";

export const processPayment = onCall<ProcessPaymentRequest, Promise<ProcessPaymentResponse>>(
  async (request) => {
    try {
      const { data, auth } = request;

      // Validate input
      if (!data.orderId) throw new Error("orderId is required");

      return await paymentService.processPayment(data, auth);
    } catch (error) {
      logError("processPayment", error);
      throw toHttpsError(error);
    }
  }
);
```

**💡 Xem template đầy đủ tại [CODING_GUIDELINES.md](./CODING_GUIDELINES.md#trigger-template)**

### Step 5: Export từ Index

**File: `src/index.ts`**

```typescript
export * from "./triggers/api.order";
export * from "./triggers/api.promotion";
export * from "./triggers/api.payment";  // NEW
export * from "./triggers/auth.trigger";
export * from "./triggers/order.trigger";
```

**Cập nhật các index files:**

```typescript
// src/models/index.ts
export * from "./payment.model";

// src/repositories/index.ts
export * from "./payment.repository";

// src/services/index.ts
export * from "./payment.service";
```

### Step 6: Build & Test

```bash
# Rebuild
npm run build

# Test trong emulator
npm run dev
```

---

## 📁 Tổ Chức Thư Mục

```
backend/functions/
├── src/                      # TypeScript source
│   ├── index.ts             # Export all functions
│   ├── params.ts            # Environment config
│   ├── models/              # Interfaces và types
│   ├── repositories/        # Firestore operations
│   ├── services/            # Business logic
│   ├── triggers/            # Function entry points
│   └── utils/               # Helper functions
└── lib/                      # Compiled JS (git ignored)
```

**Quy tắc:**
- **models/** - Chỉ interfaces, không có logic
- **repositories/** - Chỉ Firestore ops, không có business logic
- **services/** - Business logic, không trực tiếp access Firestore
- **triggers/** - Minimal logic, gọi services
- **utils/** - Pure functions, reusable

---

## 📚 Useful Commands

### Development

```bash
npm run dev               # Hot reload dev server
npm run build             # Compile TypeScript
npm run build:watch       # Watch mode
npm run shell             # Interactive testing
```

### Testing

```bash
# Full emulator suite
firebase emulators:start

# With seed data
firebase emulators:start --import=./seed-data

# Clear data on exit
firebase emulators:start --clear-on-exit
```

### Deployment

```bash
# Deploy all functions
firebase deploy --only functions

# Deploy specific function
firebase deploy --only functions:placeOrder

# View logs
npm run logs
firebase functions:log --only placeOrder --tail
```

### Linting & Type Check

```bash
npm run lint              # Run ESLint
npm run lint -- --fix     # Auto-fix issues
npx tsc --noEmit          # Check types
```

---

## 🎯 Workflow Làm Việc

### 1. Feature Development

```bash
# 1. Tạo branch
git checkout test
git pull origin test
git checkout -b feature/payment

# 2. Develop
npm run dev

# 3. Commit
git add .
git commit -m "feat(payment): Add payment service"

# 4. Push và create PR
git push origin feature/payment
```

### 2. Testing Checklist

- ✅ Build succeeds: `npm run build`
- ✅ Linter passes: `npm run lint`
- ✅ Types valid: `npx tsc --noEmit`
- ✅ Tested in emulator
- ✅ Manual test with app
- ✅ No debug code left

### 3. Deployment

```bash
# 1. Merge PR to test

# 2. Deploy to staging
firebase use staging
firebase deploy --only functions

# 3. Test staging

# 4. Merge to main

# 5. Deploy to production
firebase use production
firebase deploy --only functions
```

---

## 📚 Tài Liệu Liên Quan

### Kiến Trúc

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc
- [LAYERED_ARCHITECTURE.md](./LAYERED_ARCHITECTURE.md) - Kiến trúc 5 lớp
- [EVENTS.md](./EVENTS.md) - Event-driven architecture
- [ERROR_HANDLING.md](./ERROR_HANDLING.md) - Error handling
- [ADR/](./ADR/) - Architecture decisions

### Development

- [CODING_GUIDELINES.md](./CODING_GUIDELINES.md) - **Templates & patterns chi tiết**
- [DEBUGGING.md](./DEBUGGING.md) - Debugging guide
- [EMULATOR_GUIDE.md](./EMULATOR_GUIDE.md) - Emulator setup
- [COMMON_ERRORS.md](./COMMON_ERRORS.md) - Troubleshooting

### External

- [Firebase Functions Docs](https://firebase.google.com/docs/functions)
- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

---

**Cập nhật lần cuối:** 7 Tháng 12, 2025
