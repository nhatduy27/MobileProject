# Shop Module - Testing Guide

> Hướng dẫn test Shop API endpoints

## 🚀 Bước 1: Start Server

### Option A: Firebase Emulators (Khuyến nghị)

```bash
cd d:\MobileProject\Backend\functions
npm run serve
```

Server sẽ chạy tại: `http://localhost:5001/[project-id]/us-central1/api`

### Option B: Local Development Server

```bash
cd d:\MobileProject\Backend\functions
npm run build
npm run start:dev
```

Server sẽ chạy tại: `http://localhost:3000`

---

## 🔐 Bước 2: Lấy Firebase Auth Token

Để test các endpoint cần auth, bạn cần Firebase ID Token:

### Cách 1: Dùng script có sẵn

```bash
cd d:\MobileProject\Backend\functions
node get-id-token.js
```

### Cách 2: Manual với Firebase Auth

Đăng nhập vào app Flutter, copy token từ console logs

---

## 📋 Bước 3: Test APIs với Postman

### 1. **Create Shop** (Owner)

```
POST http://localhost:5001/[project-id]/us-central1/api/owner/shop
```

**Headers:**
```
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "Quán Phở Việt",
  "description": "Phở bò ngon nhất KTX ĐHQG",
  "address": "Tòa A, Tầng 1, Phòng 101",
  "phone": "0901234567",
  "openTime": "07:00",
  "closeTime": "21:00",
  "shipFeePerOrder": 5000,
  "minOrderAmount": 20000,
  "coverImageUrl": "https://example.com/cover.jpg",
  "logoUrl": "https://example.com/logo.jpg"
}
```

**Expected Response (201):**
```json
{
  "success": true,
  "message": "Tạo shop thành công",
  "data": {
    "id": "shop_abc123",
    "name": "Quán Phở Việt",
    "ownerId": "uid_xxx",
    "subscription": {
      "status": "TRIAL",
      "trialEndDate": "2026-01-16T00:00:00Z"
    },
    "isOpen": false,
    "createdAt": "..."
  }
}
```

**Error Cases:**
- **409 Conflict:** "Bạn đã có shop rồi" (nếu owner đã có shop)
- **400 Bad Request:** Validation errors (phone sai format, shipFee < 3000...)
- **401 Unauthorized:** Token không hợp lệ

---

### 2. **Get My Shop** (Owner)

```
GET http://localhost:5001/[project-id]/us-central1/api/owner/shop
```

**Headers:**
```
Authorization: Bearer <firebase-id-token>
```

**Expected Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "shop_abc123",
    "name": "Quán Phở Việt",
    "description": "Phở bò ngon nhất KTX ĐHQG",
    "address": "Tòa A, Tầng 1, Phòng 101",
    "phone": "0901234567",
    "openTime": "07:00",
    "closeTime": "21:00",
    "shipFeePerOrder": 5000,
    "minOrderAmount": 20000,
    "isOpen": false,
    "status": "OPEN",
    "rating": 0,
    "totalRatings": 0,
    "totalOrders": 0,
    "totalRevenue": 0,
    "subscription": {
      "status": "TRIAL",
      "startDate": "...",
      "trialEndDate": "...",
      "currentPeriodEnd": "...",
      "autoRenew": true
    }
  }
}
```

---

### 3. **Update Shop** (Owner)

```
PUT http://localhost:5001/[project-id]/us-central1/api/owner/shop
```

**Headers:**
```
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

**Body (JSON):** (tất cả fields đều optional)
```json
{
  "name": "Quán Phở Hà Nội",
  "description": "Cập nhật mô tả mới",
  "openTime": "06:00",
  "closeTime": "22:00",
  "shipFeePerOrder": 7000
}
```

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Cập nhật shop thành công",
  "data": {
    "id": "shop_abc123",
    "name": "Quán Phở Hà Nội",
    "...": "..."
  }
}
```

---

### 4. **Toggle Shop Status** (Owner)

```
PUT http://localhost:5001/[project-id]/us-central1/api/owner/shop/status
```

**Headers:**
```
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "isOpen": true
}
```

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Mở shop thành công"
}
```

**Error Case:**
- **400 Bad Request:** "Subscription chưa active" (nếu trial hết hạn)

---

### 5. **Get Shop Dashboard** (Owner)

```
GET http://localhost:5001/[project-id]/us-central1/api/owner/shop/dashboard
```

**Headers:**
```
Authorization: Bearer <firebase-id-token>
```

**Expected Response (200):**
```json
{
  "success": true,
  "data": {
    "totalRevenue": 0,
    "todayRevenue": 0,
    "weekRevenue": 0,
    "monthRevenue": 0,
    "totalOrders": 0,
    "pendingOrders": 0,
    "completedOrders": 0,
    "cancelledOrders": 0,
    "topProducts": [],
    "averageRating": 0,
    "totalRatings": 0,
    "recentReviews": []
  }
}
```

---

### 6. **Get All Shops** (Customer - Public)

```
GET http://localhost:5001/[project-id]/us-central1/api/shops?page=1&limit=20&search=phở
```

**Headers:** (không cần auth)
```
Content-Type: application/json
```

**Query Parameters:**
- `page`: số trang (default: 1)
- `limit`: số items/trang (default: 20)
- `status`: filter theo status (OPEN, CLOSED, SUSPENDED)
- `search`: tìm kiếm theo tên hoặc description

**Expected Response (200):**
```json
{
  "success": true,
  "data": {
    "shops": [
      {
        "id": "shop_abc123",
        "name": "Quán Phở Việt",
        "description": "Phở ngon nhất KTX",
        "address": "Tòa A, Tầng 1",
        "rating": 4.5,
        "totalRatings": 50,
        "isOpen": true,
        "openTime": "07:00",
        "closeTime": "21:00",
        "shipFeePerOrder": 5000,
        "minOrderAmount": 20000
      }
    ],
    "total": 1,
    "page": 1,
    "limit": 20
  }
}
```

---

### 7. **Get Shop Detail** (Customer - Public)

```
GET http://localhost:5001/[project-id]/us-central1/api/shops/:shopId
```

**Headers:** (không cần auth)
```
Content-Type: application/json
```

**Example:**
```
GET http://localhost:5001/[project-id]/us-central1/api/shops/shop_abc123
```

**Expected Response (200):**
```json
{
  "success": true,
  "data": {
    "id": "shop_abc123",
    "name": "Quán Phở Việt",
    "description": "Phở bò ngon nhất KTX ĐHQG",
    "address": "Tòa A, Tầng 1, Phòng 101",
    "phone": "0901234567",
    "coverImageUrl": "https://...",
    "logoUrl": "https://...",
    "openTime": "07:00",
    "closeTime": "21:00",
    "shipFeePerOrder": 5000,
    "minOrderAmount": 20000,
    "isOpen": true,
    "rating": 4.5,
    "totalRatings": 50,
    "totalOrders": 150
  }
}
```

**Error Case:**
- **404 Not Found:** "Không tìm thấy shop"

---

## 🧪 Test Cases

### ✅ Happy Path
1. Owner tạo shop lần đầu → 201 Created
2. Owner xem shop của mình → 200 OK
3. Owner cập nhật thông tin shop → 200 OK
4. Owner mở shop (trong trial period) → 200 OK
5. Customer browse shops → 200 OK với list
6. Customer xem chi tiết shop → 200 OK

### ❌ Error Cases
1. Owner tạo shop lần 2 → 409 "Bạn đã có shop rồi"
2. Owner chưa có shop mà GET /owner/shop → 404 "Bạn chưa có shop nào"
3. Owner mở shop khi subscription hết hạn → 400 "Subscription chưa active"
4. Validation errors:
   - Phone không đúng format → 400
   - shipFeePerOrder < 3000 → 400
   - openTime >= closeTime → 400
5. Không có token hoặc token sai → 401 Unauthorized
6. Token hợp lệ nhưng role không phải OWNER → 403 Forbidden

---

## 🔍 Debug Tips

### 1. Check Firebase Token
```bash
# Verify token expiry
jwt.io → paste token → check exp field
```

### 2. Check Firestore Data
```bash
# View data in Firebase Console
https://console.firebase.google.com/project/[project-id]/firestore
```

### 3. Check Logs
```bash
# Emulator logs
cd d:\MobileProject\Backend\functions
npm run serve

# Production logs
firebase functions:log
```

### 4. Common Issues

**"Token không hợp lệ"**
- Token đã hết hạn (exp < now)
- Token không phải từ Firebase Auth
- Project ID không khớp

**"Role không phù hợp"**
- User role không phải OWNER
- Cần set custom claims trong Firebase Auth

**"Shop not found"**
- Shop chưa được tạo
- ShopId sai
- Owner ID không khớp

---

## 📊 Postman Collection

Tạo Collection với các request trên, lưu environment variables:

```
BASE_URL = http://localhost:5001/[project-id]/us-central1/api
AUTH_TOKEN = <your-firebase-id-token>
SHOP_ID = <created-shop-id>
```

Sau đó có thể export collection để share với team!
