# 📋 TỔNG QUAN DỰ ÁN - KTX Delivery App
> **Tài liệu Bảo vệ - Giai đoạn 1**  
> **Cập nhật lần cuối:** 30 tháng 1, 2026

---

## 1. BÀI TOÁN

### Bối cảnh: Môi trường Ký Túc Xá (Dormitory)

Sinh viên ở ký túc xá đang gặp phải nhiều khó khăn khi đặt đồ ăn:

1. **Hạn chế về dịch vụ giao hàng:** Hầu hết các ứng dụng giao đồ ăn phổ biến (GrabFood, ShopeeFood) phủ sóng kém trong khu vực ký túc xá, phí cao hoặc không giao được
2. **Không có nền tảng tập trung:** Sinh viên phải dựa vào các nhóm Facebook không chính thức hoặc truyền miệng để tìm quán ăn địa phương
3. **Vấn đề tin cậy:** Không có hệ thống đánh giá/xếp hạng chuẩn hóa cho các quán ăn gần ký túc xá
4. **Khoảng cách giao tiếp:** Khó phối hợp giữa khách hàng, quán ăn và shipper đồng nghiệp
5. **Không theo dõi được:** Sinh viên không thể theo dõi đơn hàng theo thời gian thực

### Giải pháp: KTX Delivery App

Một **nền tảng giao đồ ăn được thiết kế riêng cho hệ sinh thái ký túc xá đại học**, kết nối:
- **Sinh viên** (với vai trò khách hàng)
- **Quán ăn địa phương** (với vai trò chủ quán)
- **Sinh viên làm shipper** (giao hàng linh hoạt)

---

## 2. MỤC TIÊU DỰ ÁN

### Mục tiêu chính

| # | Mục tiêu | Bằng chứng trong Codebase |
|---|-----------|---------------------|
| 1 | Cho phép sinh viên duyệt và đặt hàng từ quán địa phương | `pages/client/home/`, `pages/client/cart/`, `pages/client/payment/` |
| 2 | Cho phép chủ quán quản lý nhà hàng kỹ thuật số | `pages/owner/dashboard/`, `pages/owner/foods/`, `pages/owner/orders/` |
| 3 | Cung cấp cơ hội giao hàng cho sinh viên shipper | `pages/shipper/home/`, `pages/shipper/gps/`, `pages/shipper/application/` |
| 4 | Theo dõi đơn hàng thời gian thực với GPS | `GpsViewModel.kt`, `DeliveryMapScreen.kt`, `modules/gps/` |
| 5 | Xác thực bảo mật và phân quyền theo vai trò | `AuthService`, `RolesGuard`, `firestore.rules` |

### Mục tiêu phụ

| # | Mục tiêu | Bằng chứng trong Codebase |
|---|-----------|---------------------|
| 6 | Thông báo đẩy (push notification) cho cập nhật đơn hàng | `modules/notifications/`, FCM integration |
| 7 | Chat trong ứng dụng giữa khách hàng và chủ quán | `modules/chat/`, `pages/*/chat/` |
| 8 | Quản lý voucher/giảm giá | `modules/vouchers/`, `pages/owner/vouchers/` |
| 9 | Phân tích doanh thu cho chủ quán | `modules/revenue/`, `pages/owner/revenue/` |
| 10 | Chatbot AI hỗ trợ khách hàng | `modules/chatbot/`, Gemini integration |

---

## 3. NGƯỜI DÙNG MỤC TIÊU & VAI TRÒ

### 3.1 Khách hàng (CUSTOMER role)
> **Persona:** Sinh viên đại học sống trong ký túc xá

**Chức năng:**
- Duyệt quán và sản phẩm
- Thêm món vào giỏ hàng, đặt hàng
- Theo dõi trạng thái đơn hàng theo thời gian thực
- Chat với chủ quán
- Đánh giá và review đơn hàng đã hoàn thành
- Quản lý yêu thích và địa chỉ

**Tham khảo:** `pages/client/` directory, `Screen.UserHome` in `NavGraph.kt`

---

### 3.2 Chủ quán (OWNER role)
> **Persona:** Người bán đồ ăn địa phương gần khu ký túc xá

**Chức năng:**
- Đăng ký và thiết lập hồ sơ quán
- Quản lý danh mục sản phẩm (CRUD)
- Xử lý đơn hàng đến (confirm → prepare → ready)
- Quản lý đơn xin làm việc của shipper
- Tạo và quản lý voucher
- Xem phân tích doanh thu
- Chat với khách hàng
- Trả lời review

**Tham khảo:** `pages/owner/` directory, `DashBoardRootScreen.kt`

---

### 3.3 Shipper (SHIPPER role)
> **Persona:** Sinh viên tìm kiếm thu nhập giao hàng linh hoạt

**Chức năng:**
- Nộp đơn làm việc cho quán
- Xem đơn hàng có sẵn của quán được giao
- Nhận đơn hàng và tạo chuyến giao hàng
- Giao hàng được theo dõi GPS với bản đồ điều hướng
- Đánh dấu đơn hàng đã giao
- Xem thu nhập và yêu cầu rút tiền

**Tham khảo:** `pages/shipper/` directory, `ShipperDashboardRootScreen.kt`

---

### 3.4 Quản trị viên (ADMIN role)
> **Persona:** Quản trị viên nền tảng

**Chức năng:**
- Quản lý tất cả người dùng (cấm/bỏ cấm)
- Quản lý tất cả quán (phê duyệt/từ chối)
- Phê duyệt yêu cầu rút tiền
- Gửi thông báo broadcast
- Xem phân tích nền tảng

**Tham khảo:** `Admin/` directory (React dashboard), `modules/admin/`

---

## 4. PHẠM VI MVP

### ✅ BAO GỒM trong MVP

**Tính năng cốt lõi (Bắt buộc):**
```
Authentication
├── Email/password registration & login
├── Google OAuth sign-in
├── OTP email verification
├── Role selection (Customer/Owner/Shipper)
└── Password reset

Shopping Flow
├── Browse shops and products
├── Product search & filtering
├── Shopping cart (add/update/remove)
├── Order placement with address selection
├── Payment methods (COD + Bank Transfer)
└── Order history

Owner Management
├── Shop setup wizard
├── Product CRUD with images
├── Order management (full lifecycle)
├── Shipper management (approve/reject)
└── Basic revenue view

Shipper Operations
├── Application to shops
├── Available orders view
├── Order acceptance
├── GPS delivery tracking
└── Delivery completion

Notifications
├── FCM push notifications
├── In-app notification list
└── Order status updates

Communication
├── 1-1 chat (Customer ↔ Owner)
└── AI chatbot assistance
```

**Tham khảo:** `docs/viva/00_CODEBASE_MAP.md` Section 7 "IMPLEMENTED FEATURES"

---

### ❌ NGOÀI PHẠM VI MVP (Cải tiến tương lai)

| Danh mục | Tính năng bị loại | Lý do |
|----------|-------------------|--------|
| **Localization** | Multi-language (i18n) | Hạn chế thời gian |
| **UI** | Dark mode | Không quan trọng cho MVP |
| **Offline** | Offline mode / caching | Phức tạp |
| **Business** | Refund system, disputes | Yêu cầu khung pháp lý |
| **Business** | Multi-store per owner | Giới hạn phạm vi |
| **Business** | Scheduled/recurring orders | Phức tạp |
| **Technical** | Hilt/Koin DI | Manual factory pattern đã đủ |
| **Technical** | WebSocket real-time | Firestore listeners đã đủ |
| **Infra** | CI/CD pipeline | Hạn chế thời gian |

**Tham khảo:** `docs/viva/00_CODEBASE_MAP.md` Section 7 "NOT IMPLEMENTED"

---

### ⚠️ Giới hạn đã biết của MVP

1. **Mô hình miễn phí ship:** Khách hàng trả 0đ phí ship (kế toán nội bộ qua `shipperPayout`)
   - **Lý do:** Đơn giản hóa UX, khuyến khích sử dụng
   - **Tham khảo:** `OrdersService.createOrder()` - `shipFee = 0`

2. **Một quán mỗi chủ:** Một tài khoản owner = một quán
   - **Lý do:** Đơn giản hóa MVP
   - **Tham khảo:** `GET /api/shops/my-shop` returns single shop

3. **Manual DI:** Không dùng Hilt/Koin, dùng ViewModelFactory pattern
   - **Lý do:** Learning curve, team familiarity
   - **Tham khảo:** `LoginViewModel.factory(context)`

---

## 5. KỊCH BẢN DEMO NHANH

> **Thời lượng:** Kịch bản nói 30-60 giây

### Phương án A: Hành trình khách hàng (30 giây)

```
"Để tôi cho bạn thấy trải nghiệm khách hàng điển hình.

[Màn hình đăng nhập]
Người dùng đăng nhập bằng email hoặc tài khoản Google.

[Màn hình chính]
Họ duyệt các quán có sẵn gần ký túc xá - 
chúng tôi hiển thị đánh giá quán và thời gian giao hàng dự kiến.

[Chi tiết sản phẩm]
Nhấn vào sản phẩm hiển thị chi tiết. Thêm vào giỏ hàng bằng một lần chạm.

[Giỏ hàng → Thanh toán]
Trong giỏ hàng, họ chọn địa chỉ giao hàng, áp dụng mã voucher,
và chọn thanh toán - COD hoặc chuyển khoản ngân hàng qua QR.

[Đặt hàng thành công]
Đơn hàng được đặt! Chủ quán nhận thông báo đẩy ngay lập tức.

[Theo dõi đơn hàng]
Khách hàng có thể theo dõi trạng thái đơn hàng theo thời gian thực,
và xem vị trí GPS của shipper khi giao hàng bắt đầu."
```

---

### Phương án B: Demo luồng đầy đủ (60 giây)

```
"Đây là KTX Delivery - ứng dụng giao đồ ăn cho ký túc xá đại học.

[App khách hàng]
Một sinh viên duyệt quán, thêm món vào giỏ, và đặt hàng.
Họ có thể chat với chủ quán và theo dõi giao hàng theo thời gian thực.

[Dashboard chủ quán - Thiết bị khác]
Chủ quán nhận thông báo đơn hàng, xác nhận,
đánh dấu là 'đang chuẩn bị', sau đó 'sẵn sàng lấy hàng'.
Họ quản lý menu, voucher, và xem doanh thu ở đây.

[App Shipper - Màn hình thứ ba]
Một sinh viên shipper thấy đơn hàng có sẵn cho quán được giao.
Họ nhận đơn, bắt đầu chuyến giao hàng -
theo dõi GPS hiển thị vị trí của họ trên bản đồ.
Khi giao xong, họ hoàn tất chuyến đi.

[Quay lại khách hàng]
Khách hàng thấy trạng thái 'Đã giao' và có thể để lại đánh giá.

Backend chạy trên Firebase Cloud Functions với NestJS,
cung cấp RESTful APIs, cơ sở dữ liệu Firestore, và thông báo FCM.
Tất cả được bảo mật bởi Firebase Auth và Firestore Security Rules."
```

---

## 6. ĐIỂM KHÁC BIỆT CHÍNH

| Tính năng | Ứng dụng của chúng tôi | Ứng dụng phổ biến |
|---------|---------|-----------------|
| **Thị trường mục tiêu** | Ký túc xá đại học | Công chúng chung |
| **Shippers** | Sinh viên đồng nghiệp (linh hoạt) | Tài xế chuyên nghiệp |
| **Đưa quán lên hệ thống** | Tự thiết lập | Xác minh phức tạp |
| **Phí giao hàng** | Miễn phí cho khách | Giá động |
| **Chat thời gian thực** | Tích hợp sẵn (Khách ↔ Chủ quán) | Hỗ trợ tối thiểu |
| **Hỗ trợ AI** | Gemini chatbot | FAQ cơ bản |

---

## 7. CẤU TRÚC NHÓM DỰ ÁN

> *Điều chỉnh phần này dựa trên thành phần nhóm thực tế*

| Vai trò | Trách nhiệm | Sản phẩm chính |
|------|---------------|------------------|
| **Mobile Developer** | Android app (Kotlin + Compose) | `FoodApp/` |
| **Backend Developer** | Cloud Functions (NestJS) | `Backend/functions/` |
| **Full-stack** | Admin dashboard + Integration | `Admin/`, API integration |

---

## 8. THAM CHIẾU FILE

| Chủ đề | File chính |
|-------|-----------|
| **Android Entry** | `MainActivity.kt`, `NavGraph.kt` |
| **Authentication** | `authentication/login/`, `modules/auth/` |
| **Order Flow** | `pages/client/payment/`, `modules/orders/` |
| **GPS Tracking** | `pages/shipper/gps/`, `modules/gps/` |
| **Security** | `firestore.rules`, `AuthGuard`, `RolesGuard` |

---

**KẾT THÚC TỔNG QUAN DỰ ÁN**
