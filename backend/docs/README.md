# 📚 Tài liệu Backend — README.md

Chào mừng bạn đến với thư mục **docs/** của Backend dự án **FoodApp**!  
Đây là nơi tập hợp toàn bộ tài liệu dành cho team backend, giúp bạn:

- Dễ dàng hiểu kiến trúc dự án  
- Bắt đầu code nhanh  
- Không phải đọc quá nhiều tài liệu không cần thiết  
- Biết chính xác phải đọc file nào trong từng tình huống  

---

# 🧭 1. Mục đích của thư mục docs

Thư mục `docs/` được thiết kế để:

- **Onboard thành viên mới nhanh nhất có thể**
- **Giải thích kiến trúc backend rõ ràng**
- **Hướng dẫn cách mở rộng modules**
- **Chuẩn hoá cách viết code & tổ chức thư mục**
- **Cung cấp API contract cho frontend**
- **Hỗ trợ setup môi trường phát triển**

Hãy xem tài liệu này là **bản đồ** dẫn đường đến từng file cần thiết.

---

# 📁 2. Danh sách tài liệu & Khi nào nên đọc?

## ⭐ 1. START_HERE.md — *Bắt đầu tại đây*
👉 Dành cho thành viên mới  
👉 Đọc trong 5–10 phút  
👉 Hiểu nhanh luật chơi backend  

File này giúp bạn nắm:
- Backend kiến trúc gì?
- Đọc tài liệu nào trước?
- Luồng làm việc chung
- Quy tắc quan trọng (Controller → Service → Port → Adapter)

---

## ⭐ 2. BACKEND_CHEATSHEET.md — *Phao tóm tắt 3 phút*
Tài liệu siêu cô đọng:
- Cấu trúc thư mục  
- Quy tắc vàng  
- Các lệnh quan trọng  
- API chính  
- Luồng request  

Dành cho bạn nào cần tra cứu nhanh.

---

## ⭐⭐ 3. QUICKSTART.md — Chạy backend trong 1 phút
Bao gồm:
- Cài đặt phụ thuộc  
- Chạy server  
- Gọi thử endpoints  

Hữu ích nhất khi:
- Clone project lần đầu  
- Chuyển sang máy mới  
- Setup lại môi trường

---

## ⭐⭐ 4. DEVELOPMENT_GUIDE.md — Quy tắc viết code
Đây là quy chuẩn coding của backend:
- Controller, Service, Domain, Infra phải làm gì?
- Khi nào dùng Port? Khi nào dùng Adapter?
- DO / DON'T quan trọng  
- Ví dụ minh hoạ  

Chỉ đọc **phần 1–3** nếu bạn mới vào dự án.

---

## ⭐⭐ 5. HOW_TO_ADD_A_NEW_MODULE.md — Tạo module mới đúng chuẩn
File quan trọng với dev đảm nhiệm tính năng mới.

- Hướng dẫn step-by-step
- Ví dụ đầy đủ cho ProductsModule
- Cấu trúc thư mục
- Cách viết Port + Adapter
- Cách bind vào DI Container

Chỉ cần làm theo là tạo được module sạch 100%.

---

## ⭐⭐ 6. API_CONTRACT.md — Giao tiếp giữa Mobile ↔ Backend
Dành cho:
- Dev mobile
- Dev backend làm API  
- Tester viết test case  

Gồm:
- Định nghĩa các endpoint  
- Request / Response  
- Ví dụ JSON  
- Status codes  
- Sai số thường gặp  

Frontend dựa file này để gọi API đúng chuẩn.

---

## ⭐⭐⭐ 7. REPOSITORY_GUIDE.md — Kiến thức sâu hơn
Giải thích rõ về Ports & Adapters:
- Domain repository (Port)
- Infrastructure repository (Adapter)
- Cách DI hoạt động
- Cách mocking repository khi test

Dành cho bạn nào muốn hiểu sâu kiến trúc Clean-lite.

---

## ⭐⭐⭐ 8. ENVIRONMENT_SETUP.md — Setup môi trường backend
Gồm:
- Node / Nest CLI
- Biến môi trường `.env`
- Firebase Setup
- Redis / Message Queue (future)
- Troubleshooting  

Đọc khi:
- Setup backend trên máy mới  
- Làm việc với Firebase  
- Deploy backend

---

## ⭐⭐⭐ 9. ARCHITECTURE.md — Tài liệu kiến trúc tổng quan

Đây là tài liệu mô tả toàn bộ kiến trúc backend:

- Kiến trúc Monolithic + Layered  
- Clean Architecture (Ports & Adapters, Dependency Inversion)  
- Sơ đồ tầng: Presentation → Application → Domain → Infrastructure  
- Tổ chức thư mục backend  
- Nguyên tắc thiết kế: DIP, Separation of Concerns, testability  
- Các implementation stub (Firebase Auth, Firestore, Cache, EventBus…)  
- Hướng phát triển tiếp theo

👉 **Dành cho developer muốn hiểu sâu kiến trúc hoặc viết module phức tạp.  
Không bắt buộc phải đọc trước khi code.**

---

# 🎯 3. Tôi nên đọc tài liệu nào trước?

| Mục tiêu | File cần đọc |
|---------|---------------|
| Bắt đầu nhanh | START_HERE.md + QUICKSTART.md |
| Biết cách tổ chức code | DEVELOPMENT_GUIDE.md (mục 1–3) |
| Viết module mới | HOW_TO_ADD_A_NEW_MODULE.md |
| Gọi API đúng | API_CONTRACT.md |
| Tìm hiểu sâu | REPOSITORY_GUIDE.md + ARCHITECTURE.md |
| Setup môi trường | ENVIRONMENT_SETUP.md |

---

# 🧩 4. Sơ đồ tư duy — Khi nào mở file nào?

```
        Bạn muốn làm gì?
                |
        ┌───────┼────────┐
        |        |        |
  Chạy backend   Hiểu code  Tạo module mới
     |            |            |
 QUICKSTART     START_HERE    HOW_TO_ADD_A_NEW_MODULE
     |            |            |
ENVIRONMENT     DEVELOPMENT   REPOSITORY_GUIDE (nếu cần)
```

---

# 🤝 5. Quy tắc teamwork khi cập nhật docs

Khi bạn:
- Thêm API mới → cập nhật API_CONTRACT.md  
- Thêm module → cập nhật HOW_TO_ADD_A_NEW_MODULE.md  
- Sửa kiến trúc → cập nhật DEVELOPMENT_GUIDE.md  
- Setup Firebase/Redis → cập nhật ENVIRONMENT_SETUP.md  

Tài liệu là **sống**, không phải viết 1 lần rồi bỏ.

---

# 🎉 6. Kết luận

Bạn **không cần đọc hết tất cả tài liệu**!

Hãy:
- Đọc **đúng file, đúng thời điểm**
- Dùng `START_HERE.md` làm định hướng
- Dùng `CHEATSHEET.md` làm phao cứu sinh

Chào mừng bạn đến team Backend 🚀🔥

# 🗂️ 7. ISSUES_LIST.md — Danh sách đầu việc (Roadmap cho Backend)

Tài liệu này tổng hợp toàn bộ các đầu việc quan trọng mà team backend cần thực hiện, được chia theo mức độ ưu tiên và nhóm tính năng.

Bạn sẽ tìm thấy:

🔴 P0 — Việc bắt buộc phải làm sớm
(Hash password, JWT Auth, Repository error rules, Firestore integration cơ bản)

🟠 P1 — Việc quan trọng
(AuthGuard, Order state machine, update kiến trúc, update API contract)

🟡 P2 — Future Work
(Redis CachePort adapter, Message Queue adapter, Firebase Auth integration)

Trong mỗi issue đều có:

Mã issue: #ISSUE-XXX

Priority (P0/P1/P2)

Mô tả

Kết quả mong đợi

Người phụ trách (gợi ý)

Trạng thái: TODO / DOING / DONE

👉 File này cực kỳ quan trọng khi bắt đầu sprint hoặc chia việc cho team, giúp mọi người biết cần làm gì và mức độ ưu tiên của từng hạng mục.

📄 Xem chi tiết tại:
docs/ISSUES_LIST.md