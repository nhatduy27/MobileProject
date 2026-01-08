# Admin Module Guide - KTX Delivery API

> **Module:** Admin Management  
> **Base Path:** `/admin`  
> **Status:** 🟡 PARTIAL (Categories ✅, Users ✅, Shops/Payouts ⛔)

---

## 1. Overview

Admin module dành cho quản trị viên hệ thống:

| Feature | Status | Endpoints | Description |
|---------|--------|-----------|-------------|
| Users | ✅ Done | 3 | List, view, ban/unban users |
| Categories | ✅ Done | 5 | CRUD categories |
| Dashboard | 🟡 Partial | 1 | User stats only |
| Shops | ⛔ Blocked | 3 | Chờ ShopModule |
| Payouts | ⛔ Blocked | 5 | Chờ WalletModule |

---

## 2. Authentication

Yêu cầu:
1. Firebase ID Token
2. User phải có `role: "ADMIN"`

```http
Authorization: Bearer <admin-firebase-id-token>
```

---

## 3. Users Management

### 3.1 List Users

```bash
GET /admin/users?page=1&limit=20&role=CUSTOMER&status=ACTIVE&search=nguyen
```

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| page | number | Trang (default: 1) |
| limit | number | Số item/trang (default: 20) |
| role | string | Filter: CUSTOMER, OWNER, SHIPPER |
| status | string | Filter: ACTIVE, BANNED, DELETED |
| search | string | Tìm theo email, displayName |

### 3.2 Get User Detail

```bash
GET /admin/users/:userId
```

### 3.3 Ban/Unban User

```bash
PUT /admin/users/:userId/status
{
  "status": "BANNED",
  "reason": "Vi phạm quy định"
}
```

**Status values:** `ACTIVE`, `BANNED`

---

## 4. Categories Management

### 4.1 List All Categories

```bash
# Bao gồm cả inactive (Admin only)
GET /admin/categories
```

### 4.2 Get Category Detail

```bash
GET /admin/categories/:id
```

### 4.3 Create Category

```bash
POST /admin/categories
{
  "name": "Đồ ăn",
  "nameEn": "Food",
  "slug": "do-an",
  "icon": "restaurant",
  "description": "Các món ăn chính",
  "displayOrder": 1,
  "isActive": true
}
```

### 4.4 Update Category

```bash
PUT /admin/categories/:id
{
  "name": "Đồ ăn vặt",
  "isActive": false
}
```

### 4.5 Delete Category

```bash
DELETE /admin/categories/:id
```

> ⚠️ Cannot delete category that has products

---

## 5. Dashboard

### 5.1 User Statistics

```bash
GET /admin/dashboard/users
```

**Response:**
```json
{
  "success": true,
  "data": {
    "total": 150,
    "byRole": {
      "CUSTOMER": 120,
      "OWNER": 20,
      "SHIPPER": 10
    },
    "byStatus": {
      "ACTIVE": 145,
      "BANNED": 5
    },
    "newThisWeek": 12
  }
}
```

---

## 6. ⛔ Blocked Endpoints

Các endpoints đã implement nhưng chờ module khác:

| Endpoint | Blocked By | Reason |
|----------|------------|--------|
| `GET /admin/shops` | ShopModule | Chưa có Shop entity |
| `PUT /admin/shops/:id/status` | ShopModule | - |
| `GET /admin/payouts` | WalletModule | Chưa có Wallet/Payout entity |
| `POST /admin/payouts/:id/approve` | WalletModule | - |
| `GET /admin/dashboard` (full) | OrderModule | Chưa có Order stats |

---

## 7. Error Codes

| Code | Status | Description |
|------|--------|-------------|
| ADMIN_001 | 403 | Not an admin |
| ADMIN_002 | 404 | User not found |
| ADMIN_003 | 400 | Cannot ban another admin |
| ADMIN_004 | 404 | Category not found |
| ADMIN_005 | 409 | Category has products |
| ADMIN_006 | 409 | Slug already exists |

---

## 8. Granting Admin Role

Để test Admin APIs, cần grant ADMIN role cho user:

```javascript
// Backend/functions/scripts/grant-admin.js
const admin = require('firebase-admin');

// Initialize với service account
admin.initializeApp({
  credential: admin.credential.cert(require('../service-account.json'))
});

const uid = 'USER_UID_HERE';
admin.auth().setCustomUserClaims(uid, { role: 'ADMIN' });
admin.firestore().doc(`users/${uid}`).update({ role: 'ADMIN' });
```

---

📖 **See also:** [OPENAPI.md](../common/OPENAPI.md) | [AUTH_GUIDE.md](AUTH_GUIDE.md)
