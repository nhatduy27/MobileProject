# Products Module Guide - KTX Delivery Backend

## 1. Overview

Module Products quản lý toàn bộ sản phẩm của shop, bao gồm tạo, cập nhật, xóa, lấy danh sách, chi tiết sản phẩm, rating, review, trạng thái hiển thị, liên kết với shop và order.

| Feature         | Endpoints | Description                       |
|-----------------|-----------|-----------------------------------|
| Create          | 1         | Tạo sản phẩm mới                  |
| Get/List        | 2         | Lấy thông tin, danh sách sản phẩm |
| Update          | 1         | Cập nhật thông tin sản phẩm       |
| Delete          | 1         | Xóa sản phẩm                      |
| Reviews         | 1         | Lấy đánh giá sản phẩm             |
| Rating          | N/A       | Tự động cập nhật khi có review    |

**Total: 5+ endpoints**

---

# Products Module Guide - KTX Delivery Backend

## 1. Overview

## 1.1. Data Model Chi Tiết

### Product Entity
```json
{
  "id": "prod_1",
  "shopId": "shop_abc123",
  "name": "Mỳ Ý",
  "price": 35000,
  "description": "Mỳ Ý sốt bò bằm",
  "imageUrl": "https://...",
  "status": "ACTIVE", // ACTIVE | INACTIVE | DELETED
  "inventory": 100,
  "rating": 4.5,
  "totalRatings": 6,
  "categoryId": "cat_1",
  "categoryName": "Món chính",
  "createdAt": "2026-01-29T10:00:00Z",
  "updatedAt": "2026-01-29T12:00:00Z"
}
```

### ProductStatus Enum
| Status   | Ý nghĩa                |
|----------|------------------------|
| ACTIVE   | Hiển thị, đặt hàng     |
| INACTIVE | Ẩn khỏi menu           |
| DELETED  | Đã xoá, không truy cập |

### Liên kết
- Product thuộc 1 Shop (shopId)
- Product thuộc 1 Category (categoryId)
- Product có nhiều Review (reviews[])
- Product có thể nằm trong nhiều Order (orderItems[])

---
## 2. API Endpoints Reference

### 2.1. Create Product (Owner)
```http
POST /api/products
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json

{
  "shopId": "shop_abc123",
  "name": "Mỳ Ý",
  "price": 35000,
  "description": "Mỳ Ý sốt bò bằm",
  "imageUrl": "..."
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "id": "prod_1",
    "name": "Mỳ Ý",
    "shopId": "shop_abc123"
  }
}
```
**Validation:**
- Shop phải tồn tại, owner đúng quyền
- Tên sản phẩm không trùng trong shop
- Giá > 0

---

### 2.2. Get Product List (Public)
```http
GET /api/products?shopId=shop_abc123&page=1&limit=20&search=my y
```
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "prod_1",
      "name": "Mỳ Ý",
      "price": 35000,
      "rating": 4.5,
      "totalRatings": 6,
      "imageUrl": "..."
    }
  ],
  "pagination": { "page": 1, "limit": 20, "total": 5 }
}
```

---

### 2.3. Get Product Detail (Public)
```http
GET /api/products/{id}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "id": "prod_1",
    "name": "Mỳ Ý",
    "price": 35000,
    "description": "Mỳ Ý sốt bò bằm",
    "shopId": "shop_abc123",
    "shopName": "Hiệp Thập Cẩm",
    "rating": 4.5,
    "totalRatings": 6,
    "imageUrl": "...",
    "reviews": [...]
  }
}
```

---

### 2.4. Update Product (Owner)
```http
PUT /api/products/{id}
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "name": "Mỳ Ý đặc biệt",
  "price": 40000,
  "description": "Mỳ Ý sốt bò bằm đặc biệt",
  "imageUrl": "..."
}
```
**Validation:**
- Chỉ owner shop được update
- Không update khi product đã bị xóa

---

### 2.5. Delete Product (Owner)
```http
DELETE /api/products/{id}
Authorization: Bearer <ID_TOKEN>
```
**Validation:**
- Không còn order đang xử lý với product này
- Chỉ owner được xóa

---

### 2.6. Get Product Reviews (Public)
```http
GET /api/reviews/product/{productId}
```
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "customerName": "Nguyễn Văn A",
      "rating": 5,
      "comment": "Rất ngon!",
      "createdAt": "2026-01-29T08:00:00.000Z"
    }
  ]
}
```

---

### 2.7. Upload Product Image (Owner)
```http
POST /api/products/{id}/image
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data
image: <file>
```
**Constraints:**
- File type: JPEG, PNG only
- Max size: 5MB

**Response:**
```json
{
  "imageUrl": "https://..."
}
```

---
### 2.8. Change Product Status (Owner)
```http
PUT /api/products/{id}/status
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "status": "INACTIVE"
}
```
**Validation:**
- Chỉ owner shop được đổi trạng thái
- Không đổi khi product đã bị xoá

---
## 3. Data Model - Full Example
```json
{
  "id": "prod_1",
  "shopId": "shop_abc123",
  "name": "Mỳ Ý",
  "price": 35000,
  "description": "Mỳ Ý sốt bò bằm",
  "imageUrl": "...",
  "status": "ACTIVE", // ACTIVE | INACTIVE | DELETED
  "rating": 4.5,
  "totalRatings": 6,
  "createdAt": "2026-01-29T10:00:00Z",
  "updatedAt": "2026-01-29T12:00:00Z"
}
```

---

## 3.1. Product Review Data Model
```json
{
  "id": "review_abc123",
  "productId": "prod_1",
  "customerId": "user_123",
  "customerName": "Nguyễn Văn A",
  "rating": 5,
  "comment": "Rất ngon!",
  "createdAt": "2026-01-29T08:00:00.000Z"
}
```

---
## 4. Flow thực tế & UI/UX

### 4.1. Product Management Flow
1. Owner vào "Quản lý Shop" → "Sản phẩm"
2. Thêm mới, cập nhật, xóa sản phẩm
3. Xem danh sách sản phẩm, rating, đánh giá
4. Khách hàng xem chi tiết, đặt hàng, review

### 4.2. UI Mockup: Product List
```
┌───────────────────────────────┐
│  Danh sách Sản phẩm           │
│  ───────────────────────────  │
│  Mỳ Ý - 35,000đ               │
│  ⭐ 4.5 (6 đánh giá)           │
│  [Xem chi tiết] [Sửa] [Xóa]   │
└───────────────────────────────┘
```

### 4.3. UI Mockup: Product Detail
```
┌───────────────────────────────┐
│  Mỳ Ý                         │
│  35,000đ                      │
│  ⭐ 4.5 (6 đánh giá)           │
│  [Hình ảnh]                   │
│  Mô tả: Mỳ Ý sốt bò bằm       │
│  [Đặt hàng] [Đánh giá]        │
└───────────────────────────────┘
```

---

### 4.4. Product Inventory Flow
1. Khi tạo/cập nhật product, nhập số lượng tồn kho (inventory)
2. Khi khách đặt hàng thành công, inventory tự động trừ
3. Nếu inventory = 0, product sẽ tự động chuyển INACTIVE
4. Khi cập nhật inventory > 0, product có thể ACTIVE lại

### 4.5. UI Mockup: Upload Image
```
┌───────────────────────────────┐
│  [Chọn ảnh] [Tải lên]         │
│  [Xem trước ảnh]              │
│  [Lưu sản phẩm]               │
└───────────────────────────────┘
```

### 4.6. UI Mockup: Product Status
| Trạng thái | UI/UX | Quyền truy cập |
|------------|-------|----------------|
| ACTIVE     | Hiển thị, đặt hàng    | Chủ shop, khách |
| INACTIVE   | Ẩn khỏi menu          | Chỉ chủ shop    |
| DELETED    | Không truy cập        | Không ai        |

---
## 5. Error Codes & Negative Test Cases

| Code        | Status | Message                        | Mô tả                        |
|-------------|--------|--------------------------------|------------------------------|
| PROD_001    | 409    | Product name already exists    | Trùng tên sản phẩm trong shop|
| PROD_002    | 403    | Not owner of this shop         | Không phải chủ shop          |
| PROD_003    | 404    | Product not found              | Sản phẩm không tồn tại       |
| PROD_004    | 409    | Product has active orders      | Không thể xoá sản phẩm       |
| PROD_005    | 400    | Invalid price/name/desc        | Validate input               |
| PROD_006    | 403    | Product deleted                | Sản phẩm đã bị xoá           |

**Negative Test Checklist:**
- [ ] Tạo sản phẩm trùng tên trong shop
- [ ] Cập nhật/xóa sản phẩm không phải owner
- [ ] Xoá sản phẩm khi còn đơn hàng
- [ ] Gọi API với status = DELETED

---

| PROD_007    | 400    | Invalid image file              | File ảnh không hợp lệ         |
| PROD_008    | 400    | Inventory must be >= 0          | Số lượng tồn kho âm           |
| PROD_009    | 403    | Cannot update deleted product    | Sản phẩm đã bị xoá            |
| PROD_010    | 403    | Cannot order inactive product    | Sản phẩm đang tạm ngưng       |

**Negative Test Checklist (bổ sung):**
- [ ] Upload ảnh sai định dạng/size
- [ ] Đặt hàng khi inventory = 0
- [ ] Đặt hàng khi product INACTIVE
- [ ] Cập nhật/xoá product đã DELETED

---
## 6. Testing Checklist & cURL

- [ ] Tạo sản phẩm mới (role OWNER)
- [ ] Lấy danh sách sản phẩm
- [ ] Lấy thông tin sản phẩm cụ thể
- [ ] Cập nhật thông tin sản phẩm
- [ ] Xóa sản phẩm
- [ ] Test tạo sản phẩm trùng tên (bắt lỗi 409)
- [ ] Test xóa sản phẩm khi còn đơn hàng (bắt lỗi 409)

### cURL Example
```bash
# Lấy danh sách sản phẩm
curl -X GET "http://localhost:3000/api/products?shopId=shop_abc123"

# Tạo sản phẩm
curl -X POST http://localhost:3000/api/products \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"shopId":"shop_abc123","name":"Mỳ Ý","price":35000,"description":"Mỳ Ý sốt bò bằm"}'

# Cập nhật sản phẩm
curl -X PUT http://localhost:3000/api/products/prod_1 \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Mỳ Ý đặc biệt","price":40000,"description":"Mỳ Ý sốt bò bằm đặc biệt"}'
```

---

- [ ] Upload ảnh sản phẩm
- [ ] Đổi trạng thái sản phẩm (ACTIVE/INACTIVE)
- [ ] Đặt hàng khi inventory = 0 (bắt lỗi)
- [ ] Đặt hàng khi product INACTIVE (bắt lỗi)

### Test Data (mẫu)
| Product Name | Price  | Inventory | Status   |
|--------------|--------|-----------|----------|
| Mỳ Ý         | 35000  | 10        | ACTIVE   |
| Cơm gà       | 30000  | 0         | INACTIVE |

### Test Accounts
| Email                | Role   | Password   | Note         |
|----------------------|--------|------------|--------------|
| owner1@test.com      | OWNER  | Test123!   | Chủ shop     |
| admin1@test.com      | ADMIN  | Test123!   | Quản trị viên|

---
## 7. Best Practices & Security

- Chỉ OWNER mới được tạo/cập nhật/xóa sản phẩm
- Một shop không có 2 sản phẩm trùng tên
- Validate price > 0, name, description khi tạo/cập nhật
- Không cho phép xoá sản phẩm nếu còn đơn hàng đang xử lý
- Không cho phép cập nhật/xóa sản phẩm khi status = DELETED
- Khi tích hợp frontend:
  - Luôn kiểm tra trạng thái sản phẩm, quyền owner
  - Hiển thị rõ các trạng thái: ACTIVE, INACTIVE, DELETED
  - Khi cập nhật sản phẩm, validate input phía client trước khi gửi API
  - Khi hiển thị rating, lấy từ trường product.rating, product.totalRatings

---

- Validate inventory >= 0 khi tạo/cập nhật
- Không cho phép đặt hàng khi inventory = 0 hoặc status != ACTIVE
- Khi upload ảnh, kiểm tra định dạng, kích thước, virus scan nếu cần
- Khi hiển thị sản phẩm, luôn kiểm tra status và inventory
- Khi cập nhật/xoá, kiểm tra trạng thái DELETED
- Khi tích hợp frontend:
  - Hiển thị rõ trạng thái sản phẩm, inventory
  - Disable nút đặt hàng nếu inventory = 0 hoặc INACTIVE
  - Validate input phía client trước khi gửi API
  - Khi hiển thị rating, lấy từ product.rating, product.totalRatings

---
## 8. FAQ & Real-world Issues

**Q: Một shop có thể có 2 sản phẩm trùng tên không?**
A: Không. Tên sản phẩm phải unique trong 1 shop.

**Q: Sản phẩm bị xoá có khôi phục được không?**
A: Không. Product DELETED sẽ bị ẩn hoàn toàn.

**Q: Làm sao lấy đánh giá sản phẩm?**
A: Gọi GET /api/reviews/product/{productId} (public).

**Q: Sản phẩm có thể tạm ngưng hiển thị không?**
A: Có. Đổi status = INACTIVE, khách không đặt được hàng.

**Q: Khi nào sản phẩm bị xoá vĩnh viễn?**
A: Khi owner xoá và không còn đơn hàng active.

---

**Q: Sản phẩm có thể cập nhật inventory tự động không?**
A: Có. Khi khách đặt hàng thành công, inventory sẽ tự động trừ.

**Q: Làm sao upload nhiều ảnh cho 1 sản phẩm?**
A: Hiện tại chỉ hỗ trợ 1 ảnh chính. Có thể mở rộng trường imageUrls[].

**Q: Sản phẩm có thể thuộc nhiều category không?**
A: Hiện tại 1 product chỉ thuộc 1 category.

**Q: Làm sao lấy sản phẩm bán chạy nhất?**
A: Gọi GET /api/products?sort=best_seller (nếu backend hỗ trợ).

**Q: Làm sao lấy sản phẩm mới nhất?**
A: Gọi GET /api/products?sort=newest.

**Q: Làm sao lấy sản phẩm theo rating?**
A: Gọi GET /api/products?sort=rating.

**Q: Khi nào nên dùng status INACTIVE?**
A: Khi muốn tạm ẩn sản phẩm khỏi menu mà không xoá.

**Q: Sản phẩm bị xoá có khôi phục được không?**
A: Không. Product DELETED sẽ bị ẩn hoàn toàn.

---
## 9. Related Docs & Files

- [PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md](PRODUCT_REVIEWS_AND_SHIPPER_REMOVAL_GUIDE.md) — Đánh giá sản phẩm
- [SHOPS_GUIDE.md](SHOPS_GUIDE.md) — Quản lý shop
- [USER_GUIDE.md](USER_GUIDE.md) — Quản lý chủ shop
- [ADMIN_GUIDE.md](ADMIN_GUIDE.md) — Quản trị viên duyệt sản phẩm
- [AUTH_GUIDE.md](AUTH_GUIDE.md) — Đăng nhập, xác thực

---

- [CHAT_GUIDE.md](CHAT_GUIDE.md) — Tích hợp chat hỏi về sản phẩm
- [VEHICLE_API_GUIDE.md](VEHICLE_API_GUIDE.md) — Liên quan shipper giao hàng

---
## 10. Support
5. Tham khảo thêm các file docs mẫu khác trong thư mục backend
Gặp vấn đề? Kiểm tra:
1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Authentication & Firestore tabs
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository
