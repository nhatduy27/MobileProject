# OVERVIEW.md

## 1. Tổng quan hệ thống
- Role chính: Buyer (đặt món), Seller (quản lý quán & xử lý đơn), Shipper (giao hàng).
- Kiến trúc: Android Client (Kotlin) giao tiếp với Backend Firebase (Cloud Functions, Firestore, Auth, Storage). 
- Mục tiêu: chu trình đặt món nhanh, an toàn, dễ mở rộng cho ba vai trò.

## 2. Thư mục chính trong repo
- `FoodApp/app/` → Mã nguồn Android (Kotlin).
- `backend/functions/` → Firebase Cloud Functions (TypeScript/Node.js).
- `backend/docs/` → Toàn bộ tài liệu kỹ thuật, quy trình, kiến trúc.

## 3. Vai trò các tài liệu quan trọng
- `ARCHITECTURE.md`: Bức tranh tổng thể kiến trúc (thành phần, flow, quyết định chính).
- `API_REFERENCE_MVP.md`: Hợp đồng API cho bản MVP (tối thiểu để chạy).
- `api-reference/API_REFERENCE.md` (đóng vai “full”): Hợp đồng API đầy đủ hơn, chi tiết các nhóm role.
- `api-reference/API_ERRORS_AND_TESTING.md`: Lỗi chung của API và cách kiểm thử.
- `FIRESTORE_SCHEMA.md`: Thiết kế collection, field, quan hệ; cần đọc trước khi chạm DB.
- `RULES.md`: Quy tắc bảo mật Firestore/Storage; bắt buộc xem trước khi deploy.
- `DEVELOPMENT_GUIDE.md`: Quy trình dev, scripts, môi trường, review checklist.
- `COMMON_ERRORS.md` & `DEBUGGING.md`: Danh sách lỗi thường gặp và cách xử lý.

## 4. Luồng tổng thể backend (rút gọn)
- **Auth**: Đăng ký/đăng nhập qua Firebase Auth; trigger `onUserCreated` tạo profile + claims.
- **Orders**: Buyer tạo đơn → Seller duyệt/chuẩn bị → (tuỳ chọn) Shipper nhận/giao → cập nhật trạng thái & thống kê.
- **Promotions**: Áp mã giảm giá khi đơn còn `PENDING`; kiểm tra hiệu lực, giới hạn, discount.
- **Notifications**: Cloud Functions đẩy thông báo (buyer/seller/shipper) khi đơn tạo, đổi trạng thái, khuyến mãi.
- **Triggers**: Firestore/Auth triggers để đồng bộ dữ liệu, thống kê, và log sự kiện.

## 5. Lộ trình đọc đề xuất (learning path)
- Bước 1: `QUICKSTART.md` (thiết lập nhanh môi trường & dự án).
- Bước 2: `OVERVIEW.md` (tài liệu đang đọc) để nắm bức tranh.
- Bước 3: `ARCHITECTURE.md` để hiểu kiến trúc và quyết định thiết kế.
- Bước 4: `FIRESTORE_SCHEMA.md` + `RULES.md` để nắm dữ liệu & bảo mật.
- Bước 5: `API_REFERENCE_MVP.md` (hoặc `API_REFERENCE.md` nếu cần đầy đủ) trước khi code client/backend.
- Bước 6: `DEVELOPMENT_GUIDE.md` + `COMMON_ERRORS.md`/`DEBUGGING.md` trước khi chạy và test.

## 6. Dành cho người mới
- Muốn hiểu nhanh: đọc `OVERVIEW.md` → lướt `ARCHITECTURE.md` (phần hình/flow) → xem `API_REFERENCE_MVP.md` để biết các API tối thiểu.
- Muốn setup ngay: làm theo `QUICKSTART.md`, sau đó kiểm tra API qua Emulator UI.
- Bỏ qua tạm thời: chi tiết từng endpoint sâu trong `API_REFERENCE.md` nếu bạn chỉ cần demo luồng chính.
- Khi chuẩn bị commit: kiểm tra `DEVELOPMENT_GUIDE.md`, `RULES.md`, và lướt `COMMON_ERRORS.md` để tránh lỗi.
