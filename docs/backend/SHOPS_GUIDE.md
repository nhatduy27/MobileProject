# Shops Module Guide - Backend (Extended)

> Module: Shop Management (Owner + Customer)
> Base Paths:
> - Owner: `/owner/shop`
> - Public: `/shops`
> Status: Implemented

---

## 1. Overview

Module Shops quan ly thong tin shop, van hanh cua hang, va cho phep khach duyet danh sach shop.
Tai lieu nay mo ta day du luong nghiep vu, validation, error codes, va cach test de phu hop voi code backend hien tai.

### 1.1 Feature Matrix

| Feature | Endpoint | Description |
|---|---|---|
| Owner create shop | `POST /owner/shop` | Tao shop moi (1 owner = 1 shop) |
| Owner get shop | `GET /owner/shop` | Lay thong tin shop cua owner |
| Owner update shop | `PUT /owner/shop` | Cap nhat thong tin shop + anh |
| Owner toggle open/close | `PUT /owner/shop/status` | Mo/dong shop |
| Owner dashboard | `GET /owner/shop/dashboard` | Doanh thu + thong ke |
| Public list shops | `GET /shops` | Danh sach shop (pagination/search) |
| Public shop detail | `GET /shops/{id}` | Chi tiet shop |

### 1.2 Scope / Not In Scope

- San pham, danh gia, va danh sach shipper thuoc module khac (Products, Reviews, Shippers).
- Shops module chi tra ve thong tin shop va thong ke (dashboard) cho owner.

---

## 2. Data Models

### 2.1 Shop Entity (Firestore)

```json
{
  "id": "shop_abc",
  "ownerId": "uid_owner",
  "ownerName": "Nguyen Van A",
  "name": "Quan Pho Viet",
  "description": "Pho ngon nhat KTX",
  "address": "Toa A, Tang 1",
  "phone": "0901234567",
  "coverImageUrl": "https://...",
  "logoUrl": "https://...",
  "openTime": "07:00",
  "closeTime": "21:00",
  "shipFeePerOrder": 5000,
  "minOrderAmount": 20000,
  "isOpen": true,
  "status": "OPEN",
  "rating": 4.5,
  "totalRatings": 50,
  "totalOrders": 150,
  "totalRevenue": 10000000,
  "subscription": {
    "status": "TRIAL",
    "startDate": "2026-01-11T10:00:00.000Z",
    "trialEndDate": "2026-01-18T10:00:00.000Z",
    "currentPeriodEnd": "2026-01-18T10:00:00.000Z",
    "nextBillingDate": null,
    "autoRenew": true
  },
  "createdAt": "2026-01-11T10:00:00.000Z",
  "updatedAt": "2026-01-11T10:00:00.000Z"
}
```

### 2.2 ShopStatus Enum

| Status | Y nghia |
|---|---|
| OPEN | Shop dang hoat dong |
| CLOSED | Shop tam dong |
| SUSPENDED | Shop bi admin khoa |

### 2.3 SubscriptionStatus Enum

| Status | Y nghia |
|---|---|
| TRIAL | Dung thu 7 ngay |
| ACTIVE | Dang tra phi |
| EXPIRED | Het han |
| SUSPENDED | Bi khoa |

### 2.4 Customer View Entities

Public endpoints khong tra ve toan bo fields. Du lieu tra ve gom:

- `GET /shops`: id, name, description, address, rating, totalRatings, isOpen,
  openTime, closeTime, shipFeePerOrder, minOrderAmount, logoUrl, coverImageUrl
- `GET /shops/{id}`: tuong tu tren, co them phone, totalOrders, ownerId, ownerName

---

## 3. Authentication & Authorization

### 3.1 Owner endpoints

Yeu cau Firebase ID Token + role OWNER:

```http
Authorization: Bearer <firebase-id-token>
```

Ap dung cho:
- `POST /owner/shop`
- `GET /owner/shop`
- `PUT /owner/shop`
- `PUT /owner/shop/status`
- `GET /owner/shop/dashboard`

### 3.2 Public endpoints

Khong can auth:
- `GET /shops`
- `GET /shops/{id}`

---

## 4. Business Rules (Chuan theo code)

1) **1 Owner = 1 Shop**
- Kiem tra truoc khi tao shop.
- Neu da co shop, tra ve `SHOP_001` (409).

2) **Operating hours**
- `openTime` phai truoc `closeTime`.
- Sai gio tra ve `SHOP_002` (400).

3) **Toggle status**
- Chi duoc mo shop neu subscription status la TRIAL hoac ACTIVE.
- Neu khong, tra ve `SHOP_004` (400).

4) **Upload images**
- Tao shop bat buoc `coverImage` va `logo`.
- Update shop co the upload lai tung anh.
- Chi chap nhan JPG/JPEG/PNG, size toi da 5MB/anh.

5) **Shop visibility**
- `GET /shops` va `GET /shops/{id}` phu hop cho customer (loai bo sensitive fields).

---

## 5. API Endpoints (Owner)

### 5.1 Create Shop

```http
POST /owner/shop
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data

Fields:
- name (string)
- description (string)
- address (string)
- phone (string)
- openTime (HH:mm)
- closeTime (HH:mm)
- shipFeePerOrder (number)
- minOrderAmount (number)
- coverImage (file)  // required
- logo (file)        // required
```

**Success Response (201) - Example**
```json
{
  "success": true,
  "data": {
    "id": "shop_abc123",
    "ownerId": "uid_owner",
    "ownerName": "Nguyen Van A",
    "name": "Quan Pho Viet",
    "description": "Pho ngon nhat KTX",
    "address": "Toa A, Tang 1",
    "phone": "0901234567",
    "coverImageUrl": "https://...",
    "logoUrl": "https://...",
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
      "startDate": "2026-01-11T10:00:00.000Z",
      "trialEndDate": "2026-01-18T10:00:00.000Z",
      "currentPeriodEnd": "2026-01-18T10:00:00.000Z",
      "nextBillingDate": null,
      "autoRenew": true
    },
    "createdAt": "2026-01-11T10:00:00.000Z",
    "updatedAt": "2026-01-11T10:00:00.000Z"
  }
}
```

**Error Cases**
- `SHOP_001` (409): Owner da co shop
- `SHOP_002` (400): Gio dong cua phai sau gio mo cua
- Upload image fail: 400 (message tuong ung)

### 5.2 Get My Shop

```http
GET /owner/shop
Authorization: Bearer <ID_TOKEN>
```

**Success Response (200)**
- Tra ve `ShopEntity` day du.

**Error**
- `SHOP_003` (404): Owner chua co shop

### 5.3 Update Shop

```http
PUT /owner/shop
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data

Fields (optional):
- name, description, address, phone, openTime, closeTime,
  shipFeePerOrder, minOrderAmount
- coverImage (file, optional)
- logo (file, optional)
```

**Response (200)**
```json
{
  "message": "Cap nhat shop thanh cong"
}
```

**Rules**
- Neu update time, `openTime < closeTime`.
- Image validation nhu create.

### 5.4 Toggle Shop Status

```http
PUT /owner/shop/status
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json

{
  "isOpen": true
}
```

**Response (200)**
```json
{ "message": "Mo shop thanh cong" }
```

**Error**
- `SHOP_004` (400): Subscription chua active/trial.

### 5.5 Owner Dashboard (Analytics)

```http
GET /owner/shop/dashboard?from=2026-01-01&to=2026-01-31
Authorization: Bearer <ID_TOKEN>
```

**Behavior**
- Khi co `from` + `to` (bat buoc di cung nhau):
  - today = don giao trong ngay `to`
  - thisWeek = 7 ngay ket thuc tai `to`
  - thisMonth = tu ngay 1 cua thang `to` den `to`
  - moi bucket clamp theo [from, to]
- Khi khong co `from`/`to`:
  - Tinh theo thoi gian hien tai server
- Revenue/orderCount dua tren `DELIVERED` orders (deliveredAt)
- ordersByStatus dua tren createdAt
- recentOrders sort createdAt desc
- pendingOrders = don pending/confirmed/preparing/ready/shipping tao trong bucket "today"

**Response Example**
```json
{
  "success": true,
  "data": {
    "today": { "revenue": 500000, "orderCount": 25, "avgOrderValue": 20000, "pendingOrders": 3 },
    "thisWeek": { "revenue": 2500000, "orderCount": 120, "avgOrderValue": 20833 },
    "thisMonth": { "revenue": 10000000, "orderCount": 500 },
    "ordersByStatus": {
      "PENDING": 3, "CONFIRMED": 5, "PREPARING": 2, "READY": 1,
      "DELIVERING": 4, "COMPLETED": 140, "CANCELLED": 5
    },
    "topProducts": [
      { "id": "prod_1", "name": "Com suon", "soldCount": 50, "revenue": 1750000 }
    ],
    "recentOrders": [
      { "id": "order_1", "orderNumber": "ORD-20260111-001", "status": "COMPLETED", "total": 50000, "createdAt": "2026-01-11T10:00:00.000Z" }
    ]
  }
}
```

---

## 6. API Endpoints (Public)

### 6.1 List Shops

```http
GET /shops?page=1&limit=20&status=OPEN&search=pho
```

**Query Params**
- `page` (default 1)
- `limit` (default 20)
- `status` (OPEN/CLOSED/SUSPENDED)
- `search` (keyword)

**Response Example**
```json
{
  "success": true,
  "data": {
    "shops": [
      {
        "id": "shop_abc",
        "name": "Quan Pho Viet",
        "description": "Pho ngon nhat KTX",
        "address": "Toa A, Tang 1",
        "rating": 4.5,
        "totalRatings": 50,
        "isOpen": true,
        "openTime": "07:00",
        "closeTime": "21:00",
        "shipFeePerOrder": 5000,
        "minOrderAmount": 20000,
        "logoUrl": "https://...",
        "coverImageUrl": "https://..."
      }
    ],
    "total": 15,
    "page": 1,
    "limit": 20
  }
}
```

### 6.2 Shop Detail

```http
GET /shops/{id}
```

**Response Example**
```json
{
  "success": true,
  "data": {
    "id": "shop_abc",
    "name": "Quan Pho Viet",
    "description": "Pho ngon nhat KTX",
    "address": "Toa A, Tang 1",
    "phone": "0901234567",
    "coverImageUrl": "https://...",
    "logoUrl": "https://...",
    "rating": 4.5,
    "totalRatings": 50,
    "isOpen": true,
    "openTime": "07:00",
    "closeTime": "21:00",
    "shipFeePerOrder": 5000,
    "minOrderAmount": 20000,
    "totalOrders": 150,
    "ownerId": "owner_123",
    "ownerName": "Nguyen Van A"
  }
}
```

**Error**
- `SHOP_005` (404): Shop not found

---

## 7. Shipper Approval (Owner) - Thuoc Shippers Module

Phe duyet shipper khong thuoc Shops module. Dung endpoint:

```http
POST /owner/shippers/applications/{id}/approve
Authorization: Bearer <ID_TOKEN>
```

Cac endpoint lien quan:
- `GET /owner/shippers/applications`
- `POST /owner/shippers/applications/{id}/reject`
- `GET /owner/shippers`
- `DELETE /owner/shippers/{id}`

---

## 8. Error Codes (Shops)

| Code | Status | Message (summary) | Mo ta |
|---|---|---|---|
| SHOP_001 | 409 | Owner already has a shop | Moi owner chi co 1 shop |
| SHOP_002 | 400 | Close time must be after open time | Sai gio mo/dong |
| SHOP_003 | 404 | Owner has no shop | Chua tao shop |
| SHOP_004 | 400 | Subscription not active | Khong the mo shop |
| SHOP_005 | 404 | Shop not found | Khong tim thay shop |
| SHOP_006 | 403 | Not owner of this shop | Khong co quyen |

---

## 9. Validation & Constraints

### 9.1 Time Rules
- Dinh dang gio: `HH:mm`
- `openTime < closeTime`

### 9.2 Image Rules
- JPG/JPEG/PNG
- Toi da 5MB/anh
- Create bat buoc ca 2 anh

### 9.3 Business Rules
- 1 owner = 1 shop
- Chi mo shop khi subscription ACTIVE/TRIAL

---

## 10. Negative Test Cases

- Tao shop trung ten owner -> `SHOP_001` (409)
- Tao shop gio sai -> `SHOP_002` (400)
- Get my shop khi chua tao -> `SHOP_003` (404)
- Toggle open khi subscription EXPIRED -> `SHOP_004` (400)
- Get shop detail voi id sai -> `SHOP_005` (404)
- Update shop khi khong phai owner -> `SHOP_006` (403)

---

## 11. Testing With cURL

```bash
# Create shop (multipart)
curl -X POST http://localhost:3000/owner/shop \
  -H "Authorization: Bearer <token>" \
  -F "name=Quan Pho Viet" \
  -F "description=Pho ngon nhat KTX" \
  -F "address=Toa A, Tang 1" \
  -F "phone=0901234567" \
  -F "openTime=07:00" \
  -F "closeTime=21:00" \
  -F "shipFeePerOrder=5000" \
  -F "minOrderAmount=20000" \
  -F "coverImage=@cover.jpg" \
  -F "logo=@logo.jpg"

# Get my shop
curl -X GET http://localhost:3000/owner/shop \
  -H "Authorization: Bearer <token>"

# Update shop (optional images)
curl -X PUT http://localhost:3000/owner/shop \
  -H "Authorization: Bearer <token>" \
  -F "name=Quan Pho Viet Updated" \
  -F "coverImage=@new_cover.jpg"

# Toggle status
curl -X PUT http://localhost:3000/owner/shop/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"isOpen": true}'

# Dashboard
curl -X GET "http://localhost:3000/owner/shop/dashboard?from=2026-01-01&to=2026-01-31" \
  -H "Authorization: Bearer <token>"

# Public list
curl -X GET "http://localhost:3000/shops?page=1&limit=20&search=pho"

# Public detail
curl -X GET "http://localhost:3000/shops/shop_abc"
```

---

## 12. UI/UX Notes (Owner App)

### 12.1 Create Shop Flow
1. Owner dang nhap -> vao man "Tao Shop"
2. Nhap thong tin + upload 2 anh
3. Submit -> backend tao shop
4. Redirect sang man quan ly shop

### 12.2 Manage Shop Flow
1. Owner vao "Quan ly Shop"
2. Xem thong tin, doanh thu, thong ke
3. Cap nhat thong tin/anh
4. Mo/dong shop

---

## 13. FAQ

**Q: Mot owner co the tao nhieu shop khong?**  
A: Khong. Moi owner chi co 1 shop.

**Q: Shop bi khoa co the mo lai khong?**  
A: Chi mo duoc neu subscription TRIAL/ACTIVE va khong bi admin SUSPENDED.

**Q: Co endpoint xoa shop khong?**  
A: Khong. Hien tai khong co delete trong shops module.

**Q: Doanh thu lay o dau?**  
A: `GET /owner/shop/dashboard` (chi tinh don DELIVERED).

---

## 14. Related Files

- `D:\MobileProject\Backend\functions\src\modules\shops\controllers\owner-shops.controller.ts`
- `D:\MobileProject\Backend\functions\src\modules\shops\controllers\shops.controller.ts`
- `D:\MobileProject\Backend\functions\src\modules\shops\services\shops.service.ts`
- `D:\MobileProject\Backend\functions\src\modules\shops\entities\shop.entity.ts`
- `D:\MobileProject\Backend\functions\src\modules\shippers\owner-shippers.controller.ts`

---

## 15. Troubleshooting

1. Backend logs: terminal dang chay `npm start`
2. Firebase console: Firestore + Auth
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository
