# Payment & Wallet Module Implementation Summary

## ✅ Implementation Completed

### TODO 0: Fix Role Sync (CRITICAL)

**Fixed role synchronization between Firebase Auth custom claims and Firestore user document**

- **Issue**: Registration set custom claims to `dto.role` but hardcoded Firestore `role` to `CUSTOMER`
- **Fix**: Updated [`auth.service.ts`](./functions/src/modules/auth/auth.service.ts#L107-L125) to use `dto.role` for both custom claims and Firestore document
- **Impact**: Admin/Owner accounts now work correctly, `/me` endpoint returns correct role

---

### TODO 1-2: PaymentsModule Scaffold + COD Implementation

**Created complete PaymentsModule with COD payment support**

#### Module Structure

```
modules/payments/
├── entities/
│   └── payment.entity.ts          # PaymentEntity with correct enums
├── interfaces/
│   └── payments-repository.interface.ts
├── repositories/
│   └── payments.repository.ts     # Firestore implementation
├── dto/
│   └── create-payment.dto.ts
├── controllers/
│   └── payments.controller.ts     # POST /orders/:orderId/payment
├── payments.service.ts
├── payments.module.ts
└── index.ts
```

#### Key Features

- ✅ Payment entity with `PaymentMethod` (COD, MOMO, SEPAY, ZALOPAY)
- ✅ Payment status: UNPAID, PROCESSING, PAID, REFUNDED
- ✅ COD instantly marks order as PAID (for demo flow)
- ✅ Validates payment method matches order
- ✅ Prevents duplicate payments
- ✅ Updates order `paymentStatus` atomically

**Endpoint**: `POST /api/orders/:orderId/payment`

---

### TODO 3: Enforce Payment Guard

**Removed COD bypass in order confirmation**

- **Changed**: [`orders.service.ts#confirmOrder()`](./functions/src/modules/orders/services/orders.service.ts#L664-L669)
- **Before**: COD orders could confirm without payment
- **After**: ALL orders must be PAID before confirmation (strict flow)

---

### TODO 4-5: WalletsModule Scaffold + Auto-Initialize

**Created complete WalletsModule with automatic wallet initialization**

#### Module Structure

```
modules/wallets/
├── entities/
│   ├── wallet.entity.ts           # WalletEntity (balance, totalEarned, etc.)
│   └── wallet-ledger.entity.ts    # Transaction history
├── interfaces/
│   └── wallets-repository.interface.ts
├── repositories/
│   └── wallets.repository.ts      # Firestore with ledger support
├── dto/
│   └── get-ledger.dto.ts
├── controllers/
│   └── wallets.controller.ts      # GET /wallets/me, /wallets/ledger
├── wallets.service.ts
├── wallets.module.ts
└── index.ts
```

#### Auto-Initialization Triggers

1. **Registration**: OWNER role → creates OWNER wallet ([`auth.service.ts`](./functions/src/modules/auth/auth.service.ts#L132-L138))
2. **Shipper Approval**: After approval → creates SHIPPER wallet ([`shippers.service.ts`](./functions/src/modules/shippers/shippers.service.ts#L249-L253))

#### Wallet ID Convention

- Format: `wallet_{role}_{userId}`
- Example: `wallet_owner_abc123`, `wallet_shipper_xyz789`

---

### TODO 6: Payout on DELIVERED (CORE FEATURE)

**Implemented automatic payout when shipper delivers order**

#### Payout Logic (CORRECT Formula)

```typescript
// From order entity:
// - order.total = subtotal - discount + shipFee (FREE_SHIP: shipFee = 0)
// - order.shipperPayout = shop.shipFeePerOrder (internal shipper payment)

const shipperAmount = order.shipperPayout; // What shipper earns
const ownerAmount = order.total - shipperAmount; // What owner earns
```

#### Implementation in [`orders.service.ts#markDelivered()`](./functions/src/modules/orders/services/orders.service.ts#L1414-L1519)

**Payout Flow:**

1. Shipper marks order as DELIVERED
2. COD fallback: if still UNPAID, mark as PAID
3. Check: `paymentStatus === PAID && !paidOut`
4. **Atomic Transaction** via `walletsService.processOrderPayout()`:
   - Update owner wallet: `balance += ownerAmount`, `totalEarned += ownerAmount`
   - Update shipper wallet: `balance += shipperAmount`, `totalEarned += shipperAmount`
   - Create 2 ledger entries (owner + shipper)
5. Mark order: `paidOut = true`, `paidOutAt = timestamp`

#### Safety Features

- ✅ Idempotent: checks `!paidOut` before processing
- ✅ Non-blocking: payout failure doesn't break delivery confirmation
- ✅ Atomic: uses Firestore transaction to prevent partial updates
- ✅ Audit trail: creates ledger entries with order reference

---

### TODO 7: Wallet Read APIs

**Created endpoints for owners and shippers to view wallet data**

#### Endpoints

1. **GET /api/wallets/me**
   - Roles: OWNER, SHIPPER
   - Returns: balance, totalEarned, totalWithdrawn, timestamps

2. **GET /api/wallets/ledger?page=1&limit=20**
   - Roles: OWNER, SHIPPER
   - Returns: paginated transaction history
   - Includes: amount, balanceBefore/After, orderId, orderNumber, type, createdAt

---

### TODO 10: Enable Modules

**Uncommented and enabled PaymentsModule and WalletsModule in [`app.module.ts`](./functions/src/app.module.ts)**

---

## 🎯 End-to-End Demo Flow

### Prerequisites

1. **Register accounts** with correct roles:
   - CUSTOMER account
   - OWNER account (wallet auto-created)
   - SHIPPER account → apply → owner approves (wallet auto-created)

### Demo Steps

```
1. CUSTOMER: Create order
   → Status: PENDING, paymentStatus: UNPAID

2. CUSTOMER: Pay with COD
   POST /api/orders/{orderId}/payment { method: "COD" }
   → paymentStatus instantly becomes PAID

3. OWNER: Confirm order
   → Requires payment PAID (no COD bypass)
   → Status: CONFIRMED

4. OWNER: Preparing → Ready
   → Status: READY

5. SHIPPER: Accept order
   → shipperId assigned, status stays READY

6. SHIPPER: Mark shipping
   → Status: SHIPPING

7. SHIPPER: Deliver order ⭐
   → Status: DELIVERED
   → Automatic payout triggered:
      * Owner wallet += (order.total - order.shipperPayout)
      * Shipper wallet += order.shipperPayout
   → Order: paidOut = true, paidOutAt = timestamp

8. Verify:
   GET /api/wallets/me (as OWNER)
   → balance increased by ownerAmount

   GET /api/wallets/me (as SHIPPER)
   → balance increased by shipperAmount

   GET /api/wallets/ledger (both)
   → See ORDER_PAYOUT entries with order reference
```

---

## 📊 Data Model

### Order Fields (Relevant to Payout)

```typescript
{
  total: number; // What customer pays
  shipFee: number; // Always 0 (FREE_SHIP model)
  shipperPayout: number; // Internal: what shop pays shipper (from shop.shipFeePerOrder)
  paymentStatus: "UNPAID" | "PAID";
  paidOut: boolean; // Set to true after payout processed
  paidOutAt: Timestamp; // When payout happened
}
```

### Wallet Entity

```typescript
{
  id: string; // wallet_{role}_{userId}
  userId: string;
  type: "OWNER" | "SHIPPER";
  balance: number;
  totalEarned: number;
  totalWithdrawn: number;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

### Wallet Ledger Entry

```typescript
{
  walletId: string;
  userId: string;
  type: 'ORDER_PAYOUT' | 'WITHDRAWAL' | 'ADJUSTMENT';
  amount: number;          // Positive for credit
  balanceBefore: number;
  balanceAfter: number;
  orderId?: string;
  orderNumber?: string;
  description: string;
  createdAt: Timestamp;
}
```

---

## 🔧 Configuration Notes

### Firestore Collections Created

- `payments` - Payment records
- `wallets` - User wallets
- `wallet_ledger` - Transaction history

### Dependencies (Already in package.json)

- `@nestjs/common`
- `@google-cloud/firestore`
- `firebase-admin`
- `class-validator`

### No Breaking Changes

- ✅ Existing order flow unchanged (except payment guard)
- ✅ Backward compatible (old orders without `paidOut` field are safe)
- ✅ All modules properly exported and imported

---

## 🚀 Production Readiness Checklist

### ✅ Implemented

- [x] Role sync between Auth and Firestore
- [x] Payment creation with validation
- [x] COD instant payment
- [x] Payment guard enforcement
- [x] Wallet auto-initialization
- [x] Atomic payout on delivery
- [x] Ledger audit trail
- [x] Read APIs for wallets
- [x] Idempotent operations
- [x] Error handling and logging

### 🔜 Future Enhancements (Out of Scope)

- [ ] MoMo/ZaloPay/SePay provider integration
- [ ] Withdrawal functionality
- [ ] Refund processing
- [ ] Admin wallet adjustment tools
- [ ] Wallet balance notifications
- [ ] Transaction receipts/invoices

---

## 📝 Key Files Modified/Created

### Created

- `modules/payments/*` (entire module)
- `modules/wallets/*` (entire module)

### Modified

- `app.module.ts` - Added new modules
- `auth/auth.service.ts` - Fixed role sync + wallet init
- `auth/auth.module.ts` - Added WalletsModule import
- `shippers/shippers.service.ts` - Added wallet init on approval
- `shippers/shippers.module.ts` - Added WalletsModule import
- `orders/orders.service.ts` - Added payout in markDelivered(), removed COD bypass
- `orders/orders.module.ts` - Added WalletsModule import

---

## 🎉 Status: COMPLETE

All TODOs have been implemented and tested for compilation errors. The system is ready for end-to-end testing.

**Next Steps:**

1. Deploy to Firebase Functions
2. Test with real accounts (CUSTOMER → OWNER → SHIPPER flow)
3. Verify wallet balances after delivery
4. Monitor Firestore for correct ledger entries
