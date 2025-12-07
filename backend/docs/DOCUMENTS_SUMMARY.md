# DOCUMENTS_SUMMARY.md

Tóm tắt công dụng các tài liệu trong `backend/docs` để người mới biết cần đọc gì.

## Kiến trúc
- `OVERVIEW.md` [Bắt buộc] — Trả lời nhanh: Bức tranh tổng thể dự án trong vài phút. Mô tả ngắn: Tóm tắt role, kiến trúc client/backend, luồng chính và lộ trình đọc.
- `ARCHITECTURE.md` [Bắt buộc] — Trả lời nhanh: Hiểu kiến trúc hệ thống và các quyết định chính. Mô tả ngắn: Phân rã thành phần, flow chính, lý do thiết kế.
- `LAYERED_ARCHITECTURE.md` [Tham khảo] — Trả lời nhanh: Giải thích mô hình nhiều lớp backend. Mô tả ngắn: Mô tả trách nhiệm từng layer và nguyên tắc tách biệt.
- `ADR/ADR-001-Why-Firebase-Functions.md` [Tham khảo] — Trả lời nhanh: Vì sao chọn Firebase Functions. Mô tả ngắn: Ghi nhận quyết định và bối cảnh.
- `ADR/ADR-002-Layered-Architecture.md` [Tham khảo] — Trả lời nhanh: Quyết định dùng kiến trúc nhiều lớp. Mô tả ngắn: Lý do, tác động, lựa chọn thay thế.
- `ADR/ADR-003-No-Logic-In-Triggers.md` [Tham khảo] — Trả lời nhanh: Không nhét business logic vào trigger. Mô tả ngắn: Hướng dẫn đẩy logic vào service/repo.

## API (buyer, seller, shipper, auth)
- `API_REFERENCE_MVP.md` [Bắt buộc] — Trả lời nhanh: Hợp đồng API tối thiểu để chạy MVP. Mô tả ngắn: Endpoint chính cho Auth/Buyer/Seller, luồng trạng thái đơn, trigger cơ bản.
- `api-reference/API_REFERENCE.md` [Tham khảo] — Trả lời nhanh: Hợp đồng API đầy đủ hơn. Mô tả ngắn: Mở rộng nhóm role và case ngoài MVP.
- `api-reference/API_AUTH.md` [Bắt buộc] — Trả lời nhanh: Chi tiết API Auth. Mô tả ngắn: Đăng ký, đăng nhập, profile, claims.
- `api-reference/API_BUYER.md` [Bắt buộc] — Trả lời nhanh: API cho Buyer. Mô tả ngắn: Lấy quán, menu, đặt/hủy đơn, khuyến mãi.
- `api-reference/API_SELLER.md` [Bắt buộc] — Trả lời nhanh: API cho Seller. Mô tả ngắn: Duyệt/chuẩn bị đơn, cập nhật trạng thái.
- `api-reference/API_SHIPPER.md` [Tham khảo] — Trả lời nhanh: API cho Shipper. Mô tả ngắn: Nhận đơn, pickup, giao hàng, hoàn tất.
- `api-reference/API_TRIGGERS_AND_WORKFLOW.md` [Tham khảo] — Trả lời nhanh: Trình tự trigger và luồng trạng thái đơn. Mô tả ngắn: Miêu tả transitions và sự kiện.
- `api-reference/API_ERRORS_AND_TESTING.md` [Tham khảo] — Trả lời nhanh: Lỗi API thường gặp và cách test. Mô tả ngắn: Mẫu lỗi, mã lỗi, gợi ý kiểm thử.

## Firestore data & rules
- `FIRESTORE_SCHEMA.md` [Bắt buộc] — Trả lời nhanh: Schema dữ liệu Firestore. Mô tả ngắn: Collection, field, quan hệ, ràng buộc.
- `FIRESTORE_INDEXES.md` [Tham khảo] — Trả lời nhanh: Các index cần khai báo. Mô tả ngắn: Danh sách index phục vụ truy vấn.
- `RULES.md` [Bắt buộc] — Trả lời nhanh: Quy tắc bảo mật Firestore/Storage. Mô tả ngắn: Luật truy cập đọc/ghi, nguyên tắc triển khai.
- `ROLES_AND_PERMISSIONS.md` [Bắt buộc] — Trả lời nhanh: Quyền của từng role với tài nguyên. Mô tả ngắn: Ma trận quyền Buyer/Seller/Shipper/Admin.

## Hướng dẫn phát triển (development)
- `QUICKSTART.md` [Bắt buộc] — Trả lời nhanh: Thiết lập dự án trong vài phút. Mô tả ngắn: Cài đặt, clone, chạy backend/Android, emulator.
- `DEVELOPMENT_GUIDE.md` [Bắt buộc] — Trả lời nhanh: Quy trình làm việc hằng ngày. Mô tả ngắn: Cách chạy scripts, build, review checklist.
- `CODING_GUIDELINES.md` [Tham khảo] — Trả lời nhanh: Quy ước code. Mô tả ngắn: Style, đặt tên, pattern khuyến nghị.
- `EMULATOR_GUIDE.md` [Tham khảo] — Trả lời nhanh: Cách dùng Firebase Emulator. Mô tả ngắn: Thiết lập, cổng, thao tác thường dùng.
- `README.md` [Bắt buộc] — Trả lời nhanh: Tóm tắt dự án và hướng dẫn cơ bản. Mô tả ngắn: Mục tiêu, thành phần, cách chạy nhanh.

## Testing & debugging
- `COMMON_ERRORS.md` [Bắt buộc] — Trả lời nhanh: Lỗi phổ biến và cách xử lý. Mô tả ngắn: Checklist fix nhanh cho các lỗi thường gặp.
- `DEBUGGING.md` [Tham khảo] — Trả lời nhanh: Cách debug backend/frontend. Mô tả ngắn: Công cụ, mẹo, quy trình.
- `ERROR_HANDLING.md` [Tham khảo] — Trả lời nhanh: Chiến lược xử lý lỗi chuẩn. Mô tả ngắn: Mẫu trả lỗi, logging, mapping code.
- `EVENTS.md` [Tham khảo] — Trả lời nhanh: Sự kiện hệ thống và thông báo. Mô tả ngắn: Ai nhận notification, khi nào, payload gì.

## Ghi chú
- "Bắt buộc" = nên đọc trước khi bắt đầu code hoặc deploy.
- "Tham khảo" = đọc khi cần đào sâu hoặc xử lý tình huống đặc biệt.
