# Payment Quick Reference

## 🚀 How to Use

### COD Payment

```typescript
// 1. Customer creates order
POST /api/orders
{
  shopId: "shop_123",
  items: [...],
  paymentMethod: "cod"  // or "sepay"
}

// 2. Customer creates payment
POST /api/orders/{orderId}/payment
{
  method: "cod"
}
// → Instantly returns status="PAID"

// 3. Owner confirms order
POST /api/orders-owner/{orderId}/confirm
// → Requires paymentStatus="PAID" (automatically set by COD)
```

### SEPAY Payment

```typescript
// 1-2. Same as COD but method="sepay"
POST /api/orders/{orderId}/payment
{
  method: "sepay"
}
// → Returns:
{
  payment: {
    status: "PROCESSING",
    providerData: {
      qrCodeUrl: "https://qr.sepay.vn/img?...",
      sepayContent: "KTX12345",
      accountNumber: "00012112005000",
      accountName: "TONG DUONG THAI HOA",
      bankCode: "MB"
    }
  }
}

// 3. Display QR to customer → customer transfers money

// 4. Poll verify endpoint (every 3 seconds)
POST /api/orders/{orderId}/payment/verify
// → Returns matched=true when payment detected

// 5. Owner confirms order (now PAID)
POST /api/orders-owner/{orderId}/confirm
```

---

## 📦 What Changed

### ✅ Added

- SEPAY QR generation with deterministic content
- SEPAY polling verify endpoint (no webhook)
- COD instant PAID flow
- SEPAY config in ConfigService
- Smoke test script

### ✅ Verified

- confirmOrder requires PAID status (no COD bypass)
- deliverOrder triggers wallet payout atomically
- Payout uses correct formula (order.shipperPayout)

### ❌ Blocked

- MOMO payments → 400 error
- ZALOPAY payments → 400 error
- Other methods not implemented

---

## 🧪 Testing

```bash
# Build
cd Backend/functions
npm run build  # ✅ PASSED

# Run smoke test
cd Backend/scripts
chmod +x smoke-test.sh
export BASE_URL="http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api"
export FIREBASE_API_KEY="AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA"
./smoke-test.sh
```

---

## 🔧 Environment Setup

### Local Development (.env)

Already configured in `Backend/.env`:

```bash
SEPAY_API_URL=https://my.sepay.vn/userapi
SEPAY_SECRET_KEY=L075N8U3SCD5QCGGE4QYBCQFFWDJBAI1NGHOVYFW6VQIV9WKAMAP2MRG4HXZ16DP
SEPAY_ACCOUNT_NUMBER=00012112005000
SEPAY_ACCOUNT_NAME=TONG DUONG THAI HOA
SEPAY_BANK_CODE=MB
```

### Firebase Functions (Production)

```bash
firebase functions:config:set \
  sepay.api_url="https://my.sepay.vn/userapi" \
  sepay.secret_key="YOUR_KEY" \
  sepay.account_number="YOUR_ACCOUNT"
```

---

## 📝 Commit Ready

```bash
git add .
git commit -m "feat(payment): add sepay QR + polling verify; enforce paid-before-confirm; payout wallets on delivered"
git push
```

See `PAYMENT_IMPLEMENTATION_COMPLETE.md` for full documentation.
