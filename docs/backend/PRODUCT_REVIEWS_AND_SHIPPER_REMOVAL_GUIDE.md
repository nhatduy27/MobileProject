# Product Reviews & Shipper Removal API Guide

Hướng dẫn tích hợp các API mới cho Frontend team:

1. **Product Reviews API** - Đánh giá sản phẩm riêng lẻ
2. **Shipper Removal Requests API** - Yêu cầu shipper rời shop

---

## 1. Product Reviews API

### 1.1. Tạo đánh giá (Customer)

**Endpoint:** `POST /api/reviews`  
**Role:** CUSTOMER  
**Auth:** Required (Bearer Token)

```json
// Request Body
{
  "orderId": "string (required)",
  "shopRating": 1-5,           // Đánh giá shop (required)
  "shopComment": "string",      // Nhận xét về shop (optional)
  "shipperRating": 1-5,         // Đánh giá shipper (optional)
  "shipperComment": "string",   // Nhận xét về shipper (optional)
  "productReviews": [           // Đánh giá từng sản phẩm (REQUIRED)
    {
      "productId": "string",
      "rating": 1-5,
      "comment": "string"       // optional
    }
  ]
}

// Response (201 Created)
{
  "success": true,
  "data": {
    "id": "review_abc123",
    "orderId": "order_xyz",
    "customerId": "user_123",
    "customerName": "Nguyễn Văn A",
    "shopId": "shop_456",
    "shopRating": 5,
    "shopComment": "Đồ ăn ngon",
    "shipperRating": 4,
    "shipperComment": "Giao hàng nhanh",
    "productReviews": [
      {
        "productId": "prod_789",
        "productName": "Mỳ ý",
        "rating": 5,
        "comment": "Rất ngon!"
      }
    ],
    "createdAt": "2026-01-29T08:00:00.000Z"
  }
}
```

**Validations:**

- Order phải ở trạng thái `DELIVERED`
- Order chưa được review
- `productReviews` phải bao gồm tất cả sản phẩm trong order
- Rating từ 1-5

**Side Effects:**

- Tự động cập nhật `rating` và `totalRatings` của từng product
- Tự động cập nhật rating của shop

---

### 1.2. Lấy danh sách review theo sản phẩm (Public)

**Endpoint:** `GET /api/reviews/product/:productId`  
**Auth:** NOT Required

```json
// Response (200 OK)
{
  "success": true,
  "data": [
    {
      "customerName": "Nguyễn Văn A",
      "rating": 5,
      "comment": "Rất ngon!",
      "createdAt": "2026-01-29T08:00:00.000Z"
    },
    {
      "customerName": "Trần Thị B",
      "rating": 4,
      "comment": "Tốt",
      "createdAt": "2026-01-28T10:00:00.000Z"
    }
  ]
}
```

---

### 1.3. Lấy danh sách review theo shop (Public)

**Endpoint:** `GET /api/reviews/shop/:shopId`  
**Auth:** NOT Required

```json
// Response (200 OK)
{
  "success": true,
  "data": [
    {
      "id": "review_abc123",
      "customerName": "Nguyễn Văn A",
      "shopRating": 5,
      "shopComment": "Đồ ăn ngon",
      "productReviews": [
        {
          "productId": "prod_789",
          "productName": "Mỳ ý",
          "rating": 5,
          "comment": "Rất ngon!"
        }
      ],
      "createdAt": "2026-01-29T08:00:00.000Z"
    }
  ]
}
```

---

### 1.4. Sản phẩm với rating (Public)

**Endpoint:** `GET /api/products?shopId=xxx`  
**Auth:** NOT Required

```json
// Response (200 OK)
{
  "success": true,
  "data": [
    {
      "id": "prod_789",
      "name": "Mỳ ý",
      "price": 35000,
      "rating": 4.5,          // ⭐ Rating trung bình
      "totalRatings": 6,       // ⭐ Tổng số đánh giá
      "imageUrl": "...",
      ...
    }
  ]
}
```

---

## 2. Shipper Removal Requests API

### 2.1. Tổng quan

Shipper có thể gửi yêu cầu rời shop với 2 loại:

- **QUIT**: Nghỉ làm shipper hoàn toàn → Trở thành CUSTOMER
- **TRANSFER**: Chuyển sang shop khác → Vẫn là SHIPPER, có thể apply shop mới

### 2.2. Shipper tạo yêu cầu rời shop

**Endpoint:** `POST /api/shippers/removal-requests`  
**Role:** SHIPPER  
**Auth:** Required (Bearer Token)

```json
// Request Body
{
  "shopId": "string (required)",           // ID shop hiện tại
  "type": "QUIT" | "TRANSFER" (default: "TRANSFER"),
  "reason": "string (optional, max 500)"   // Lý do rời shop
}

// Response (201 Created)
{
  "success": true,
  "data": {
    "id": "srr_abc123",
    "shipperId": "shipper_123",
    "shipperName": "Lê Văn Shipper",
    "shipperPhone": "+84901234567",
    "shopId": "shop_456",
    "shopName": "Quán ABC",
    "ownerId": "owner_789",
    "type": "TRANSFER",
    "reason": "Muốn chuyển sang shop gần nhà hơn",
    "status": "PENDING",
    "createdAt": "2026-01-29T08:00:00.000Z"
  }
}
```

**Validations:**

- Shipper must be working for the specified shop
- Shipper không có đơn hàng đang giao cho shop đó
- Không có request PENDING nào trước đó

**Type Explanation:**
| Type | Mô tả | Sau khi approve |
|------|-------|-----------------|
| `QUIT` | Shipper muốn nghỉ hoàn toàn | Role → CUSTOMER, shipperInfo bị xóa |
| `TRANSFER` | Shipper muốn đổi shop | Role vẫn là SHIPPER, shopId bị xóa, có thể apply shop mới |

---

### 2.3. Shipper xem danh sách yêu cầu của mình

**Endpoint:** `GET /api/shippers/removal-requests`  
**Role:** SHIPPER  
**Auth:** Required (Bearer Token)

**Query Params:**

- `status` (optional): `PENDING` | `APPROVED` | `REJECTED`

```json
// Response (200 OK)
{
  "success": true,
  "data": [
    {
      "id": "srr_abc123",
      "type": "TRANSFER",
      "reason": "Muốn đổi shop",
      "status": "PENDING",
      "createdAt": "2026-01-29T08:00:00.000Z"
    }
  ]
}
```

---

### 2.4. Owner xem danh sách yêu cầu của shop

**Endpoint:** `GET /api/owner/shops/:shopId/removal-requests`  
**Role:** OWNER  
**Auth:** Required (Bearer Token)

**Query Params:**

- `status` (optional): `PENDING` | `APPROVED` | `REJECTED`

```json
// Response (200 OK)
{
  "success": true,
  "data": [
    {
      "id": "srr_abc123",
      "shipperId": "shipper_123",
      "shipperName": "Lê Văn Shipper",
      "shipperPhone": "+84901234567",
      "type": "TRANSFER",
      "reason": "Muốn đổi shop",
      "status": "PENDING",
      "createdAt": "2026-01-29T08:00:00.000Z"
    }
  ]
}
```

---

### 2.5. Owner xử lý yêu cầu (Approve/Reject)

**Endpoint:** `PUT /api/owner/removal-requests/:requestId`  
**Role:** OWNER  
**Auth:** Required (Bearer Token)

```json
// Request Body - APPROVE
{
  "action": "APPROVE"
}

// Request Body - REJECT
{
  "action": "REJECT",
  "rejectionReason": "Đang mùa cao điểm, cần thêm shipper"  // Required khi reject
}

// Response (200 OK)
{
  "success": true,
  "data": {
    "id": "srr_abc123",
    "status": "APPROVED",  // hoặc "REJECTED"
    "processedAt": "2026-01-29T09:00:00.000Z",
    "processedBy": "owner_789",
    "rejectionReason": "..."  // Nếu rejected
  }
}
```

**Side Effects khi APPROVE:**

| Type       | Role sau approve     | shipperInfo                                  |
| ---------- | -------------------- | -------------------------------------------- |
| `QUIT`     | CUSTOMER             | Bị xóa hoàn toàn                             |
| `TRANSFER` | SHIPPER (giữ nguyên) | shopId bị xóa, shipper có thể apply shop mới |

**Notifications:**

- Khi shipper tạo request → Owner nhận notification
- Khi owner approve/reject → Shipper nhận notification

---

## 3. Frontend Implementation Guide

### 3.1. Product Reviews Flow

```
[Customer] Order DELIVERED
    ↓
[Customer] Vào màn hình review order
    ↓
[Customer] Rate shop (1-5) + Rate từng product (1-5)
    ↓
[POST /api/reviews] Gửi review
    ↓
[Backend] Tự động cập nhật product.rating & shop.rating
    ↓
[Frontend] Hiển thị rating trên product list
```

### 3.2. Shipper Removal Flow

```
[Shipper] Vào Settings hoặc Profile
    ↓
[Shipper] Chọn "Rời shop" / "Đổi shop"
    ↓
[Shipper] Chọn type (QUIT hoặc TRANSFER) + Nhập lý do
    ↓
[POST /api/shippers/removal-requests] Gửi request
    ↓
[Owner] Nhận notification, vào danh sách pending requests
    ↓
[Owner] Approve hoặc Reject
    ↓
[Shipper] Nhận notification kết quả
    ↓
[Frontend] Nếu QUIT → Chuyển về màn hình Customer
           Nếu TRANSFER → Chuyển về màn hình apply shop mới
```

---

## 4. Error Codes

### Product Reviews

| Code    | Message                             | Mô tả                            |
| ------- | ----------------------------------- | -------------------------------- |
| RVW_001 | Không tìm thấy order                | Order ID không tồn tại           |
| RVW_002 | Bạn không có quyền review order này | Order không thuộc về customer    |
| RVW_003 | Order chưa được giao xong           | Order status chưa phải DELIVERED |
| RVW_004 | Order đã được review                | Không thể review 2 lần           |
| RVW_005 | Thiếu đánh giá sản phẩm             | productReviews không đầy đủ      |

### Shipper Removal

| Code    | Message                              | Mô tả                              |
| ------- | ------------------------------------ | ---------------------------------- |
| SRR_001 | Không tìm thấy thông tin shipper     | Shipper ID không tồn tại           |
| SRR_002 | Bạn không phải shipper của shop này  | shopId không khớp                  |
| SRR_003 | Bạn đang có đơn hàng chưa hoàn thành | Có order đang active               |
| SRR_004 | Không tìm thấy shop                  | Shop ID không tồn tại              |
| SRR_005 | Bạn không phải chủ shop này          | Owner không sở hữu shop            |
| SRR_006 | Không tìm thấy yêu cầu               | Request ID không tồn tại           |
| SRR_007 | Bạn không có quyền xử lý yêu cầu này | Request không thuộc shop của owner |
| SRR_008 | Yêu cầu đã được xử lý                | Status không phải PENDING          |

---

## 5. Test Data (Development)

### Test Accounts

| Email                    | Role     | Password     |
| ------------------------ | -------- | ------------ |
| testcustomer999@test.com | CUSTOMER | TestPass123! |
| hiepshipper@gmail.com    | SHIPPER  | TestPass123! |
| hiepowner@gmail.com      | OWNER    | TestPass123! |

### Test Shop

- **ID:** `nzIfau9GtqIPyWkmLyku`
- **Name:** Hiệp Thập Cẩm

### Test Product (has reviews)

- **ID:** `i9STmaAMR77WN9jLdBEN`
- **Name:** Mỳ ý
- **Rating:** 4.5
- **Total Ratings:** 6

---

## 6. Swagger Documentation

Truy cập Swagger UI để test API:

```
http://localhost:3000/api/docs
```

**Cách lấy token cho Swagger:**

```bash
cd Backend/functions
node get-id-token.js <email>
```

Copy token từ output và paste vào Authorize button trên Swagger.

---

**Last Updated:** 2026-01-29
