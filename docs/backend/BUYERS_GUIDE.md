# BUYERS_MODULE_GUIDE.md

# Buyers Module Guide - KTX Delivery Backend

## 1. Overview

Module Buyers quản lý các API và logic liên quan đến người mua hàng (buyer/user cuối). Bao gồm: đăng ký, xác thực, quản lý thông tin cá nhân, lịch sử đơn hàng, đánh giá, ví, và các flow liên quan đến trải nghiệm mua sắm.

| Feature                | Endpoints | Description                                 |
|------------------------|-----------|---------------------------------------------|
| Register/Login         | 2         | Đăng ký, đăng nhập                          |
| Profile Management     | 2         | Xem/sửa thông tin cá nhân                   |
| Order History          | 1         | Lịch sử đơn hàng                            |
| Wallet/Balance         | 1         | Xem số dư, giao dịch ví                     |
| Product Review         | 1         | Đánh giá sản phẩm                           |
| Address Book           | 2         | Thêm/xóa/sửa địa chỉ giao hàng              |

| Avatar Upload         | 1         | Upload ảnh đại diện (avatar)                |
| FCM Token             | 1         | Đăng ký token nhận thông báo push           |
| Delete Account        | 1         | Xóa tài khoản buyer                         |

**Total: 12+ endpoints**

**Total: 9+ endpoints**

---

## 2. API Endpoints Reference

### 2.8. Upload Avatar
```http
POST /api/buyers/avatar
Authorization: Bearer <ID_TOKEN>
Content-Type: multipart/form-data
avatar: <file>
```
**Response:**
```json
{
  "success": true,
  "avatarUrl": "https://..."
}
```

### 2.9. Register FCM Token
```http
POST /api/buyers/fcm-token
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "token": "dF3K2mPxQ8y..."
}
```

### 2.10. Delete Account
```http
DELETE /api/buyers/profile
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "success": true
}
```

### 2.1. Register Buyer
```http
POST /api/buyers/register
Content-Type: application/json
{
  "email": "user@example.com",
  "password": "string",
  "name": "Nguyen Van A"
}
```
**Response:**
```json
{
  "success": true,
  "data": {
    "id": "buyer_abc123",
    "email": "user@example.com",
    "name": "Nguyen Van A"
  }
}
```

### 2.2. Login Buyer
```http
POST /api/buyers/login
Content-Type: application/json
{
  "email": "user@example.com",
  "password": "string"
}
```
**Response:**
```json
{
  "success": true,
  "token": "<ID_TOKEN>",
  "data": {
    "id": "buyer_abc123",
    "email": "user@example.com",
    "name": "Nguyen Van A"
  }
}
```

### 2.3. Get/Update Profile
```http
GET /api/buyers/profile
Authorization: Bearer <ID_TOKEN>
```
```http
PUT /api/buyers/profile
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "name": "Nguyen Van B",
  "phone": "0901234567"
}
```

### 2.4. Get Order History
```http
GET /api/buyers/orders?page=1&limit=10
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "orders": [
    {
      "id": "order_abc123",
      "shopName": "Hiệp Thập Cẩm",
      "total": 120000,
      "status": "DELIVERED",
      "createdAt": "2026-01-29T10:00:00Z"
    }
  ],
  "page": 1,
  "limit": 10,
  "total": 5
}
```

### 2.5. Wallet/Balance
```http
GET /api/buyers/wallet
Authorization: Bearer <ID_TOKEN>
```
**Response:**
```json
{
  "balance": 50000,
  "transactions": [
    {
      "id": "txn_abc123",
      "type": "DEBIT",
      "amount": 120000,
      "description": "Thanh toán đơn hàng order_abc123",
      "createdAt": "2026-01-29T10:00:00Z"
    }
  ]
}
```

### 2.6. Product Review
```http
POST /api/buyers/reviews
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "productId": "product_abc123",
  "rating": 5,
  "comment": "Sản phẩm rất tốt!"
}
```

### 2.7. Address Book
```http
GET /api/buyers/addresses
Authorization: Bearer <ID_TOKEN>
```
```http
POST /api/buyers/addresses
Authorization: Bearer <ID_TOKEN>
Content-Type: application/json
{
  "address": "123 Đường ABC, Quận 1, TP.HCM"
}
```

---

## 3. Data Model - Full Example

### BuyerStatus Enum
| Status   | Ý nghĩa                |
|----------|------------------------|
| ACTIVE   | Đang hoạt động         |
| INACTIVE | Đã khóa, không đăng nhập|
| DELETED  | Đã xóa tài khoản       |

### Buyer Entity
```json
{
  "id": "buyer_abc123",
  "email": "user@example.com",
  "name": "Nguyen Van A",
  "phone": "0901234567",
  "addresses": [
    {
      "id": "addr_abc123",
      "address": "123 Đường ABC, Quận 1, TP.HCM"
    }
  ],
  "createdAt": "2026-01-29T10:00:00Z"
}
```

### Order Entity (Buyer View)
```json
{
  "id": "order_abc123",
  "shopName": "Hiệp Thập Cẩm",
  "total": 120000,
  "status": "DELIVERED",
  "createdAt": "2026-01-29T10:00:00Z"
}
```

### Wallet Transaction
```json
{
  "id": "txn_abc123",
  "type": "DEBIT", // DEBIT | CREDIT
  "amount": 120000,
  "description": "Thanh toán đơn hàng order_abc123",
  "createdAt": "2026-01-29T10:00:00Z"
}
```

### Review Entity
```json
{
  "id": "review_abc123",
  "productId": "product_abc123",
  "buyerId": "buyer_abc123",
  "rating": 5,
  "comment": "Sản phẩm rất tốt!",
  "createdAt": "2026-01-29T10:00:00Z"
}
```

---

## 4. Flow thực tế & UI/UX

### 4.7. Upload avatar
1. Buyer vào trang "Thông tin cá nhân"
2. Chọn ảnh đại diện → Upload
3. Backend lưu file, trả về avatarUrl

### 4.8. Đăng ký FCM Token
1. Buyer đăng nhập app/web
2. App lấy FCM token, gửi lên backend
3. Backend lưu vào profile buyer

### 4.9. Xóa tài khoản
1. Buyer vào "Cài đặt tài khoản"
2. Chọn "Xóa tài khoản", xác thực lại mật khẩu
3. Backend xóa buyer, chuyển trạng thái DELETED

### 4.10. UI/UX Tips
- Hiển thị avatar mặc định nếu chưa upload
- Thông báo realtime khi có đơn hàng mới qua FCM
- Cảnh báo khi xóa tài khoản là không thể khôi phục
- Địa chỉ mặc định luôn hiển thị đầu tiên

### 4.1. Đăng ký/Đăng nhập
1. Buyer nhập email, password → Đăng ký/Đăng nhập
2. Nhận token, lưu vào localStorage
3. Chuyển sang dashboard cá nhân

### 4.2. Quản lý thông tin cá nhân
1. Buyer vào trang "Thông tin cá nhân"
2. Xem/sửa tên, số điện thoại, địa chỉ
3. Lưu thay đổi → Gọi API update

### 4.3. Lịch sử đơn hàng
1. Buyer vào "Lịch sử đơn hàng"
2. Gọi API /buyers/orders
3. Hiển thị danh sách đơn, trạng thái, chi tiết

### 4.4. Đánh giá sản phẩm
1. Buyer vào chi tiết đơn hàng đã giao
2. Chọn sản phẩm, nhập đánh giá, rating
3. Gửi đánh giá → Gọi API /buyers/reviews

### 4.5. Quản lý ví
1. Buyer vào "Ví của tôi"
2. Xem số dư, lịch sử giao dịch

### 4.6. Quản lý địa chỉ
1. Buyer vào "Địa chỉ giao hàng"
2. Thêm/xóa/sửa địa chỉ

---

## 5. Error Codes & Negative Test Cases

| BUY_007     | 400    | Invalid avatar file              | File ảnh không hợp lệ        |
| BUY_008     | 400    | FCM token invalid                | Token push notification sai  |
| BUY_009     | 403    | Cannot delete account            | Không đủ quyền xóa tài khoản |

**Negative Test Checklist (bổ sung):**
- [ ] Upload file avatar không hợp lệ (quá lớn, sai định dạng)
- [ ] Đăng ký FCM token sai
- [ ] Xóa tài khoản với token không hợp lệ

| Code        | Status | Message                        | Mô tả                        |
|-------------|--------|--------------------------------|------------------------------|
| BUY_001     | 400    | Email already exists           | Email đã tồn tại             |
| BUY_002     | 401    | Invalid credentials            | Sai email hoặc mật khẩu      |
| BUY_003     | 403    | Unauthorized                   | Không đủ quyền               |
| BUY_004     | 404    | Buyer not found                | Không tìm thấy người mua     |
| BUY_005     | 400    | Invalid address                | Địa chỉ không hợp lệ         |
| BUY_006     | 400    | Invalid review                 | Đánh giá không hợp lệ        |

**Negative Test Checklist:**
- [ ] Đăng ký với email đã tồn tại
- [ ] Đăng nhập sai mật khẩu
- [ ] Sửa thông tin với token không hợp lệ
- [ ] Thêm địa chỉ trống
- [ ] Đánh giá sản phẩm chưa mua

---

## 6. Testing Checklist & cURL
- [ ] Upload avatar
- [ ] Đăng ký FCM token
- [ ] Xóa tài khoản buyer

### cURL Example (bổ sung)
```bash
# Upload avatar
curl -X POST http://localhost:3000/api/buyers/avatar \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -F "avatar=@/path/to/avatar.jpg"

# Đăng ký FCM token
curl -X POST http://localhost:3000/api/buyers/fcm-token \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"token":"dF3K2mPxQ8y..."}'

# Xóa tài khoản
curl -X DELETE http://localhost:3000/api/buyers/profile \
  -H "Authorization: Bearer <ID_TOKEN>"
```

- [ ] Đăng ký buyer
- [ ] Đăng nhập buyer
- [ ] Lấy/sửa thông tin cá nhân
- [ ] Lấy lịch sử đơn hàng
- [ ] Đánh giá sản phẩm
- [ ] Thêm/xóa/sửa địa chỉ

### cURL Example
```bash
# Đăng ký
curl -X POST http://localhost:3000/api/buyers/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Test123!","name":"Nguyen Van A"}'

# Đăng nhập
curl -X POST http://localhost:3000/api/buyers/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Test123!"}'

# Lấy thông tin cá nhân
curl -X GET http://localhost:3000/api/buyers/profile \
  -H "Authorization: Bearer <ID_TOKEN>"

# Lấy lịch sử đơn hàng
curl -X GET http://localhost:3000/api/buyers/orders?page=1&limit=10 \
  -H "Authorization: Bearer <ID_TOKEN>"

# Đánh giá sản phẩm
curl -X POST http://localhost:3000/api/buyers/reviews \
  -H "Authorization: Bearer <ID_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"product_abc123","rating":5,"comment":"Sản phẩm rất tốt!"}'
```

---

## 7. Best Practices & Security
- Chỉ cho phép upload avatar định dạng JPEG/PNG, tối đa 5MB
- Khi xóa tài khoản, cần xác thực lại mật khẩu
- Không lưu FCM token trùng lặp cho 1 buyer
- Khi gửi thông báo push, kiểm tra token còn hiệu lực

- Mã hóa mật khẩu buyer khi lưu DB
- Kiểm tra xác thực token cho mọi API cá nhân
- Không trả về thông tin nhạy cảm (password, token) trong response
- Validate dữ liệu đầu vào phía backend và frontend
- Giới hạn số lượng địa chỉ, review mỗi buyer
- Lưu log mọi thay đổi thông tin, giao dịch ví

---

## 8. FAQ & Real-world Issues
**Q: Buyer upload avatar sai định dạng thì sao?**
A: Backend trả lỗi BUY_007.

**Q: Buyer bị mất thông báo push?**
A: Kiểm tra lại FCM token, đăng ký lại nếu cần.

**Q: Buyer xóa tài khoản rồi có đăng nhập lại được không?**
A: Không. Trạng thái chuyển DELETED, phải đăng ký mới.

**Q: Buyer có thể đăng ký nhiều tài khoản với cùng số điện thoại không?**
A: Không. Số điện thoại phải duy nhất.

**Q: Buyer có thể xóa tài khoản không?**
A: Có thể, cần xác thực lại mật khẩu trước khi xóa.

**Q: Buyer có thể xem lịch sử nạp/rút ví không?**
A: Có. Xem API /buyers/wallet.

**Q: Buyer có thể đánh giá sản phẩm chưa mua không?**
A: Không. Backend sẽ kiểm tra buyerId trong order.

---

## 9. Related Docs & Files
- [SHIPPERS_GUIDE.md](SHIPPERS_GUIDE.md) — Đối chiếu flow đăng ký/xóa tài khoản
- [CHAT_GUIDE.md](CHAT_GUIDE.md) — Tích hợp chat hỗ trợ buyer

- [AUTH_GUIDE.md](AUTH_GUIDE.md) — Đăng ký, xác thực
- [PRODUCTS_GUIDE.md](PRODUCTS_GUIDE.md) — Sản phẩm
- [ORDERS_GUIDE.md](ORDERS_GUIDE.md) — Đơn hàng
- [USER_GUIDE.md](USER_GUIDE.md) — Thông tin user

---

## 10. Support
6. Tham khảo thêm các file docs mẫu khác trong thư mục backend
Gặp vấn đề? Kiểm tra:
1. Backend logs: Terminal đang chạy `npm start`
2. Firebase Console: Authentication & Firestore tabs
3. Swagger docs: http://localhost:3000/api/docs
4. Issue tracker: GitHub repository
5. Tham khảo thêm các file docs mẫu khác trong thư mục backend
