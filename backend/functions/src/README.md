# Firebase Functions Skeleton Backend

## 📁 Structure

Complete skeleton structure cho Firebase Cloud Functions với TypeScript.

```
src/
├── index.ts                    # Entry point, export tất cả functions
├── params.ts                   # Environment parameters configuration
├── models/                     # Data models & types
│   ├── index.ts
│   ├── order.model.ts
│   ├── user.model.ts
│   ├── restaurant.model.ts
│   └── promotion.model.ts
├── repositories/               # Data access layer
│   ├── index.ts
│   ├── order.repository.ts
│   ├── user.repository.ts
│   ├── restaurant.repository.ts
│   └── promotion.repository.ts
├── services/                   # Business logic layer
│   ├── index.ts
│   ├── order.service.ts
│   ├── promotion.service.ts
│   └── notification.service.ts
├── triggers/                   # Cloud Functions triggers
│   ├── api.order.ts           # Callable: placeOrder, cancelOrder
│   ├── api.promotion.ts       # Callable: applyPromotion
│   ├── auth.trigger.ts        # Auth: onUserCreated
│   └── order.trigger.ts       # Firestore: onOrderCreated
└── utils/                      # Utility functions
    ├── index.ts
    ├── validation.utils.ts
    └── error.utils.ts
```

## 🚀 Deployed Functions

### Callable Functions (HTTPS)
- `placeOrder` - Đặt hàng mới
- `cancelOrder` - Hủy đơn hàng
- `applyPromotion` - Áp dụng mã khuyến mãi

### Auth Triggers
- `onUserCreated` - Trigger khi user đăng ký

### Firestore Triggers
- `onOrderCreated` - Trigger khi order được tạo

## 📝 Implementation Status

All files are **skeleton only** with:
- ✅ Complete type definitions
- ✅ Method signatures
- ✅ TODO comments for business logic
- ✅ Basic validation structure
- ✅ Error handling framework
- ⚠️ Stub implementations (throw "Not implemented")

## 🔧 Next Steps

1. Implement repository methods với Firebase Admin SDK
2. Add business logic trong services
3. Complete validation rules
4. Add authentication checks
5. Implement notification sending (FCM)
6. Add unit tests
7. Configure Firebase project parameters

## 📚 Architecture

**Layered Architecture:**
- **Triggers** → Entry points (HTTP, Auth, Firestore events)
- **Services** → Business logic
- **Repositories** → Data access
- **Models** → Type definitions

**Key Principles:**
- Dependency Injection via singletons
- Separation of concerns
- Type safety with TypeScript
- Clean error handling
