# User Module Guide - KTX Delivery API

> **Module:** User Profile & Settings  
> **Base Path:** `/me`  
> **Status:** ✅ IMPLEMENTED

---

## 1. Overview

User module quản lý tất cả thông tin cá nhân của user sau khi đăng nhập:

| Feature | Endpoints | Description |
|---------|-----------|-------------|
| Profile | 3 | GET/PUT profile, avatar upload |
| Addresses | 5 | CRUD địa chỉ giao hàng |
| Settings | 2 | Notification preferences |
| Favorites | 4 | Sản phẩm yêu thích |
| FCM Token | 1 | Push notification token |
| Account | 1 | Xóa tài khoản |

**Total: 16 endpoints**

---

## 2. Authentication

Tất cả endpoints yêu cầu Firebase ID Token:

```http
Authorization: Bearer <firebase-id-token>
```

---

## 3. Endpoints

### 3.1 Profile

```bash
# Get current user profile
GET /me

# Update profile (displayName, phone)
PUT /me
Content-Type: application/json
{
  "displayName": "Nguyễn Văn A",
  "phone": "0901234567"
}

# Upload avatar (max 5MB, JPEG/PNG)
POST /me/avatar
Content-Type: multipart/form-data
avatar: <file>

# Delete account permanently
DELETE /me
```

### 3.2 Addresses

```bash
# List all addresses
GET /me/addresses

# Add new address
POST /me/addresses
{
  "label": "Nhà",
  "fullAddress": "Tòa A, Phòng 101, KTX Khu B",
  "building": "A",
  "room": "101",
  "note": "Gọi trước khi đến",
  "isDefault": true
}

# Update address
PUT /me/addresses/:id

# Delete address
DELETE /me/addresses/:id

# Set as default
PUT /me/addresses/:id/default
```

### 3.3 Settings

```bash
# Get settings
GET /me/settings

# Update settings
PUT /me/settings
{
  "notificationsEnabled": true,
  "language": "vi"
}
```

### 3.4 Favorites

```bash
# List favorite products
GET /me/favorites/products?page=1&limit=20

# Add to favorites
POST /me/favorites/products/:productId

# Remove from favorites
DELETE /me/favorites/products/:productId

# Check if favorited
GET /me/favorites/products/:productId
```

### 3.5 FCM Token

```bash
# Register/update FCM token for push notifications
PUT /me/fcm-token
{
  "fcmToken": "firebase_fcm_token...",
  "deviceId": "device_123"
}
```

---

## 4. Response Format

**Success:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Thành công"
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "USER_001",
    "message": "User không tồn tại"
  }
}
```

---

## 5. Error Codes

| Code | Status | Description |
|------|--------|-------------|
| USER_001 | 404 | User không tồn tại |
| USER_002 | 400 | Invalid phone format |
| USER_003 | 404 | Address không tồn tại |
| USER_004 | 409 | Already in favorites |
| USER_005 | 400 | File type not allowed |
| USER_006 | 400 | File size exceeds 5MB |

---

## 6. Testing với cURL

```bash
# Lấy token
cd Backend/functions
node get-id-token.js test@example.com

# Test GET /me
curl -X GET http://localhost:3000/api/me \
  -H "Authorization: Bearer <token>"

# Test thêm địa chỉ
curl -X POST http://localhost:3000/api/me/addresses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"label":"Nhà","fullAddress":"Tòa A, Phòng 101","building":"A","room":"101"}'
```

---

📖 **See also:** [OPENAPI.md](../common/OPENAPI.md) | [AUTH_GUIDE.md](AUTH_GUIDE.md)
