# 09. Security & Permissions

> **Mục đích**: Giải thích model bảo mật, phân quyền, và Firestore Security Rules.  
> **Dành cho**: Bảo vệ đồ án – trả lời câu hỏi về authentication, authorization, data protection.

---

## 1. AUTHENTICATION MODEL

### 1.1 Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       AUTHENTICATION FLOW                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐            │
│   │  Mobile  │──▶│  Firebase│──▶│  Backend │──▶│ Firestore│            │
│   │   App    │   │   Auth   │   │  NestJS  │   │  Rules   │            │
│   └──────────┘   └──────────┘   └──────────┘   └──────────┘            │
│        │              │              │              │                    │
│        │  Login/      │  ID Token    │  Verify      │  Read/Write       │
│        │  SignUp      │  (JWT)       │  Token       │  Check            │
│        │              │              │              │                    │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Firebase Authentication

| Aspect | Implementation |
|--------|----------------|
| **Provider** | Firebase Authentication |
| **Methods** | Email/Password, Google Sign-In |
| **Token Type** | Firebase ID Token (JWT) |
| **Token Lifetime** | 1 hour (auto-refresh) |

### 1.3 Token Flow

```kotlin
// Mobile: Attach token to every API request
// AuthInterceptor.kt
class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authManager.getValidToken() // Auto-refresh if expired
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

```typescript
// Backend: Verify token in AuthGuard
// auth.guard.ts
async canActivate(context: ExecutionContext): Promise<boolean> {
    const token = this.extractTokenFromHeader(request);
    
    // Verify với Firebase Admin SDK
    const decodedToken = await this.firebaseService.auth.verifyIdToken(token);
    
    // Lấy custom claims cho role
    const userRecord = await this.firebaseService.auth.getUser(decodedToken.uid);
    const customClaims = userRecord.customClaims || {};
    
    // Attach user object vào request
    request.user = {
        uid: decodedToken.uid,
        email: decodedToken.email,
        role: customClaims.role || 'CUSTOMER',
        // ...
    };
    
    return true;
}
```

### 1.4 Custom Claims

Roles được lưu trong Firebase Auth custom claims (không phải chỉ Firestore):

```typescript
// Khi user chọn role lần đầu
await this.firebaseService.auth.setCustomUserClaims(uid, { role: 'OWNER' });
```

| Claim | Values | Purpose |
|-------|--------|---------|
| `role` | `CUSTOMER`, `OWNER`, `SHIPPER`, `ADMIN` | Role-based access control |

---

## 2. ROLE-BASED PERMISSIONS

### 2.1 Roles trong hệ thống

| Role | Internal Name | Quyền chính |
|------|---------------|-------------|
| **Buyer** | `CUSTOMER` | Đặt hàng, thanh toán, review, chat |
| **Seller** | `OWNER` | Quản lý shop, products, orders, vouchers |
| **Shipper** | `SHIPPER` | Nhận đơn, GPS tracking, giao hàng |
| **Admin** | `ADMIN` | Quản lý toàn hệ thống (Web panel) |

### 2.2 Backend: RolesGuard

```typescript
// roles.guard.ts
@Injectable()
export class RolesGuard implements CanActivate {
    canActivate(context: ExecutionContext): boolean {
        const requiredRoles = this.reflector.getAllAndOverride<UserRole[]>(
            ROLES_KEY,
            [context.getHandler(), context.getClass()]
        );

        if (!requiredRoles || requiredRoles.length === 0) {
            return true; // Không yêu cầu role → cho phép
        }

        const user: IUser = request.user;
        const hasRole = requiredRoles.includes(user.role);

        if (!hasRole) {
            throw new ForbiddenException(
                `Access denied. Required roles: ${requiredRoles.join(', ')}`
            );
        }

        return true;
    }
}
```

### 2.3 Permission Matrix

| Resource | CUSTOMER | OWNER | SHIPPER | ADMIN |
|----------|:--------:|:-----:|:-------:|:-----:|
| **Shops** |||||
| Browse shops | ✅ | ✅ | ✅ | ✅ |
| Create shop | ❌ | ✅ | ❌ | ✅ |
| Update own shop | ❌ | ✅ | ❌ | ✅ |
| Delete shop | ❌ | ✅ (own) | ❌ | ✅ |
| **Products** |||||
| Browse products | ✅ | ✅ | ✅ | ✅ |
| Create product | ❌ | ✅ | ❌ | ✅ |
| Update product | ❌ | ✅ (own shop) | ❌ | ✅ |
| **Orders** |||||
| Create order | ✅ | ❌ | ❌ | ❌ |
| View own orders | ✅ | ✅ (shop) | ✅ (assigned) | ✅ |
| Update order status | ❌ | ✅ (shop) | ✅ (assigned) | ✅ |
| Cancel order | ✅ (own) | ✅ (shop) | ❌ | ✅ |
| **GPS/Trips** |||||
| Create trip | ❌ | ❌ | ✅ | ❌ |
| View trip location | ✅ (own order) | ✅ (shop order) | ✅ (own) | ✅ |
| **Vouchers** |||||
| Use voucher | ✅ | ❌ | ❌ | ❌ |
| Create voucher | ❌ | ✅ (shop) | ❌ | ✅ (global) |
| **Wallets** |||||
| View balance | ❌ | ✅ | ✅ | ✅ |
| Request withdrawal | ❌ | ✅ | ✅ | ❌ |

### 2.4 Controller Annotations

```typescript
// Owner-only endpoint
@Controller('owner/shops')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
export class OwnerShopsController { }

// Customer-only endpoint
@Controller('vouchers')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.CUSTOMER)
export class VouchersController { }

// Admin-only endpoint
@Controller('admin/vouchers')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
export class AdminVouchersController { }

// Multiple roles allowed
@Get('my-wallet')
@Roles(UserRole.OWNER, UserRole.SHIPPER)
async getMyWallet(@CurrentUser() user: IUser) { }
```

---

## 3. FIRESTORE SECURITY RULES

### 3.1 Nguyên tắc

| Principle | Implementation |
|-----------|----------------|
| **Deny by default** | `match /{document=**} { allow read, write: if false; }` |
| **Ownership check** | `resource.data.ownerId == request.auth.uid` |
| **Role-based access** | `hasRole('OWNER')` helper function |
| **Least privilege** | Chỉ cấp quyền cần thiết |

### 3.2 Helper Functions

```javascript
// firestore.rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Check if user is the document owner
    function isCurrentUser(userId) {
      return request.auth.uid == userId;
    }
    
    // Check user role from Firestore
    function hasRole(role) {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == role;
    }
    
    function isAdmin() { return hasRole('ADMIN'); }
    function hasOwnerRole() { return hasRole('OWNER'); }
    function hasShipperRole() { return hasRole('SHIPPER'); }
  }
}
```

### 3.3 Collection Rules

#### Users Collection

```javascript
match /users/{userId} {
    // Anyone authenticated can read user profiles
    allow read: if isAuthenticated();
    
    // Only create own profile
    allow create: if isAuthenticated() && isCurrentUser(userId);
    
    // Only update own profile
    allow update: if isAuthenticated() && isCurrentUser(userId);
    
    // Only admin can delete
    allow delete: if isAdmin();
}
```

#### Shops Collection

```javascript
match /shops/{shopId} {
    // Public read (browse shops)
    allow read: if true;
    
    // Authenticated users can create
    allow create: if isAuthenticated();
    
    // Only owner or admin can update/delete
    allow update, delete: if resource.data.ownerId == request.auth.uid || isAdmin();
}
```

#### Orders Collection

```javascript
match /orders/{orderId} {
    // Authenticated users can read orders they're involved in
    allow read: if isAuthenticated();
    
    // Authenticated users can create orders
    allow create: if isAuthenticated();
    
    // Updates allowed for involved parties (validated in backend)
    allow update: if isAuthenticated();
}
```

#### Wallets Collection

```javascript
match /wallets/{walletId} {
    // Only owner or admin can read
    allow read: if isCurrentUser(walletId) || isAdmin();
    
    // NO direct writes - only via Cloud Functions
    allow write: if false;
}
```

#### GPS/Trips Collection

```javascript
match /shipperTrips/{tripId} {
    // Only trip owner or admin can read
    allow read: if isAuthenticated() && 
                   (resource.data.shipperId == request.auth.uid || isAdmin());
    
    // Only shipper can create their own trip
    allow create: if isAuthenticated() && 
                     hasShipperRole() && 
                     request.resource.data.shipperId == request.auth.uid;
    
    // Only trip owner can update
    allow update: if isAuthenticated() && 
                     resource.data.shipperId == request.auth.uid;
    
    // Trips should never be deleted
    allow delete: if false;
}
```

#### Chat Collections

```javascript
match /conversations/{conversationId} {
    // Only participants can read/write
    allow read: if isAuthenticated() && 
                   request.auth.uid in resource.data.participants;
    
    allow create: if isAuthenticated() && 
                     request.auth.uid in request.resource.data.participants;
    
    allow update: if isAuthenticated() && 
                     request.auth.uid in resource.data.participants;

    // Messages subcollection
    match /messages/{messageId} {
        // Only participants can read
        allow read: if isAuthenticated() && 
                       request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        
        // Only participants can create, must be sender
        allow create: if isAuthenticated() && 
                         request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants &&
                         request.resource.data.senderId == request.auth.uid;
        
        // Only participants can update (mark as read)
        allow update: if isAuthenticated() && 
                         request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
    }
}
```

---

## 4. DATA INTEGRITY RULES

### 4.1 Order Status Transitions

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ORDER STATE MACHINE                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐             │
│   │ PENDING │───▶│CONFIRMED│───▶│PREPARING│───▶│  READY  │             │
│   └────┬────┘    └────┬────┘    └────┬────┘    └────┬────┘             │
│        │              │              │              │                    │
│        │              │              │              ▼                    │
│        │              │              │         ┌─────────┐              │
│        │              │              │         │SHIPPING │              │
│        │              │              │         └────┬────┘              │
│        │              │              │              │                    │
│        ▼              ▼              ▼              ▼                    │
│   ┌─────────┐                                 ┌─────────┐              │
│   │CANCELLED│◀─────(before READY only)───────│DELIVERED│              │
│   └─────────┘                                 └─────────┘              │
│                                                                          │
│   Legend:                                                               │
│   ───▶ Valid transition                                                 │
│   Terminal states: DELIVERED, CANCELLED (no transitions out)            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Backend Implementation:**

```typescript
// order-state-machine.service.ts
const ALLOWED_TRANSITIONS: Record<OrderStatus, OrderStatus[]> = {
    [OrderStatus.PENDING]: [OrderStatus.CONFIRMED, OrderStatus.CANCELLED],
    [OrderStatus.CONFIRMED]: [OrderStatus.PREPARING, OrderStatus.CANCELLED],
    [OrderStatus.PREPARING]: [OrderStatus.READY, OrderStatus.CANCELLED],
    [OrderStatus.READY]: [OrderStatus.SHIPPING],  // Cannot cancel after READY
    [OrderStatus.SHIPPING]: [OrderStatus.DELIVERED],
    [OrderStatus.DELIVERED]: [],  // Terminal
    [OrderStatus.CANCELLED]: [],  // Terminal
};

async validateTransition(from: OrderStatus, to: OrderStatus): Promise<void> {
    if (!ALLOWED_TRANSITIONS[from].includes(to)) {
        throw new ConflictException(
            `Invalid transition: ${from} → ${to}`
        );
    }
}
```

### 4.2 Who Can Change Order Status?

| From | To | Who |
|------|-----|-----|
| PENDING | CONFIRMED | Owner |
| PENDING | CANCELLED | Customer, Owner |
| CONFIRMED | PREPARING | Owner |
| CONFIRMED | CANCELLED | Customer, Owner |
| PREPARING | READY | Owner |
| PREPARING | CANCELLED | Customer, Owner |
| READY | SHIPPING | Shipper |
| SHIPPING | DELIVERED | Shipper |

### 4.3 Voucher Validation

```typescript
// vouchers.service.ts
async validateVoucher(code: string, userId: string, orderAmount: number) {
    const voucher = await this.findByCode(code);
    
    // Check expiration
    if (voucher.expirationDate < new Date()) {
        throw new BadRequestException('Voucher expired');
    }
    
    // Check usage limit (global)
    if (voucher.usageCount >= voucher.usageLimit) {
        throw new BadRequestException('Voucher usage limit reached');
    }
    
    // Check per-user limit
    const userUsage = await this.getUserUsageCount(userId, voucher.id);
    if (userUsage >= voucher.usageLimitPerUser) {
        throw new BadRequestException('You have reached usage limit for this voucher');
    }
    
    // Check minimum order amount
    if (orderAmount < voucher.minOrderAmount) {
        throw new BadRequestException(`Minimum order: ${voucher.minOrderAmount}`);
    }
    
    return voucher;
}
```

---

## 5. SECURITY BEST PRACTICES

### 5.1 Implemented

| Practice | Implementation |
|----------|----------------|
| ✅ Token-based auth | Firebase ID Token (JWT) |
| ✅ Role-based access | Custom claims + RolesGuard |
| ✅ Firestore rules | Deny-by-default, ownership checks |
| ✅ Input validation | class-validator DTOs |
| ✅ Sensitive operations via backend | Wallets write only via Cloud Functions |

### 5.2 Security Checklist

| Item | Status |
|------|--------|
| Passwords hashed | ✅ Firebase Auth handles |
| Token expiration | ✅ 1 hour, auto-refresh |
| HTTPS only | ✅ Firebase hosting |
| SQL injection | ✅ N/A (NoSQL) |
| XSS protection | ✅ React/Compose auto-escape |
| CORS configured | ✅ Backend config |

---

## 6. VẤN ĐÁP THƯỜNG GẶP

**Q: Tại sao dùng Firebase Auth thay vì tự build?**  
A: Firebase Auth cung cấp bảo mật enterprise-grade, hỗ trợ nhiều providers (Google, Apple, Phone), tự động handle token refresh, không cần maintain infrastructure.

**Q: Custom claims khác gì với lưu role trong Firestore?**  
A: Custom claims được include trong ID token, backend verify mà không cần query Firestore. Firestore rules có thể check cả hai.

**Q: Tại sao Wallets không cho direct write?**  
A: Tiền là sensitive data. Mọi thay đổi phải qua business logic ở backend để đảm bảo integrity (atomic transactions, audit trail).

**Q: Firestore rules có đủ bảo mật không?**  
A: Firestore rules là layer đầu tiên. Backend NestJS có thêm validation, business rules, và logging. Defense in depth.

**Q: Làm sao prevent user sửa order của người khác?**  
A: 
1. Firestore rules check `isAuthenticated()`
2. Backend AuthGuard verify token
3. Business logic check `order.customerId === user.uid`
