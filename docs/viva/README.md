# 📚 KTX Delivery - Bảo Vệ Đồ Án - Chỉ Mục Tài Liệu (Viva Documentation Index)

> **Mục đích**: Danh mục toàn bộ tài liệu chuẩn bị cho bảo vệ đồ án (Viva/Oral Defense).  
> **Lần Cập Nhật Cuối Cùng**: 30 tháng 1, 2026

---

## 📋 BỘ TÀI LIỆU (Documentation Set)

### Giai Đoạn 0: Tham Chiếu Codebase (Phase 0: Codebase Reference)
| # | Document | Mô Tả (Description) | Số Dòng (Pages) |
|---|----------|---------------------|------------------|
| 00 | [00_CODEBASE_MAP.md](00_CODEBASE_MAP.md) | Sơ đồ cấu trúc codebase toàn bộ (Full codebase structure map) | ~1200 dòng |

### Giai Đoạn 1: Nền Tảng Dự Án (Phase 1: Project Foundation)
| # | Document | Mô Tả (Description) | Chủ Đề Chính (Key Topics) |
|---|----------|---------------------|---------------------------|
| 01 | [01_PROJECT_OVERVIEW.md](01_PROJECT_OVERVIEW.md) | Giới thiệu & mục tiêu dự án (Project introduction & goals) | Vấn đề, Giải pháp, Phạm vi (Problem, Solution, Scope) |
| 02 | [02_TECHSTACK.md](02_TECHSTACK.md) | Giải thích bộ công nghệ (Technology stack explanation) | Kotlin, NestJS, Firebase |
| 03 | [03_ARCHITECTURE.md](03_ARCHITECTURE.md) | Kiến trúc hệ thống (System architecture) | MVVM, Clean Architecture |

### Giai Đoạn 2: Tính Năng & Luồng Xử Lý (Phase 2: Features & Flows)
| # | Document | Mô Tả (Description) | Chủ Đề Chính (Key Topics) |
|---|----------|---------------------|---------------------------|
| 04 | [04_FEATURES_BY_ROLE.md](04_FEATURES_BY_ROLE.md) | Tính năng theo vai trò (Features per user role) | Người mua, Chủ quán, Shipper (Buyer, Seller, Shipper) |
| 05 | [05_MAIN_FLOWS.md](05_MAIN_FLOWS.md) | Luồng người dùng lõi (Core user journeys) | Đơn hàng, Thanh toán, Giao hàng (Order, Payment, Delivery) |

### Giai Đoạn 3: Phân Tích Kỹ Thuật Sâu (Phase 3: Technical Deep Dives)
| # | Document | Mô Tả (Description) | Chủ Đề Chính (Key Topics) |
|---|----------|---------------------|---------------------------|
| 06 | [06_BUSINESS_LOGIC_DEEP_DIVE.md](06_BUSINESS_LOGIC_DEEP_DIVE.md) | Logic tính năng phức tạp (Complex feature logic) | Thông báo, GPS, Chat, Chatbot (Notifications, GPS, Chat, Chatbot) |
| 07 | [07_JETPACK_COMPOSE_VS_XML.md](07_JETPACK_COMPOSE_VS_XML.md) | Lựa chọn UI framework (UI framework choice) | Khai báo vs Mệnh lệnh (Declarative vs Imperative) |
| 08 | [08_STATE_MANAGEMENT.md](08_STATE_MANAGEMENT.md) | Mẫu quản lý trạng thái (State handling patterns) | StateFlow, ViewModel, Compose |

### Giai Đoạn 4: Bảo Mật & Kết Luận (Phase 4: Security & Wrap-up)
| # | Document | Mô Tả (Description) | Chủ Đề Chính (Key Topics) |
|---|----------|---------------------|---------------------------|
| 09 | [09_SECURITY_AND_PERMISSIONS.md](09_SECURITY_AND_PERMISSIONS.md) | Mô hình bảo mật (Security model) | Xác thực, RBAC, Firestore Rules (Auth, RBAC, Firestore Rules) |
| 10 | [10_LIMITATIONS_AND_FUTURE_WORK.md](10_LIMITATIONS_AND_FUTURE_WORK.md) | Phạm vi & cải thiện (Scope & improvements) | Giới hạn MVP, Kiểm tra, Roadmap (MVP limits, Testing, Roadmap) |

---

## 🎯 KỊP THỜI 1 PHÚT - CÂU TRẢ LỜI (1-MINUTE SPEAKING SCRIPTS)

Mỗi chủ đề có thể trả lời trong 1 phút (Each topic can be answered in 1 minute):

### Tổng Quan Dự Án (Project Overview) (01)
> "KTX Delivery là ứng dụng giao đồ ăn cho sinh viên KTX. Có 3 vai trò: Khách hàng đặt hàng, Chủ quán quản lý cửa hàng, Shipper giao hàng. Bộ công nghệ: Android Kotlin với Jetpack Compose, backend NestJS trên Firebase Cloud Functions, cơ sở dữ liệu Firestore." (KTX Delivery is a food delivery app for dormitory students. Has 3 roles: Customer orders food, Owner manages shop, Shipper delivers. Tech: Android Kotlin with Jetpack Compose, NestJS backend on Firebase Cloud Functions, Firestore database.)

### Kiến Trúc (Architecture) (03)
> "Ứng dụng dùng kiến trúc MVVM: View (Compose UI) - ViewModel (logic kinh doanh, trạng thái) - Model (Repository + API). StateFlow để quản lý trạng thái, ViewModel tồn tại qua thay đổi cấu hình. Backend dùng kiến trúc phân tầng: Controller - Service - Repository." (App uses MVVM architecture: View (Compose UI) - ViewModel (business logic, state) - Model (Repository + API). StateFlow for state management, ViewModel survives configuration change. Backend uses layered architecture: Controller - Service - Repository.)

### Quản Lý Trạng Thái (State Management) (08)
> "Dùng MutableStateFlow riêng tư trong ViewModel, phơi bày StateFlow công khai cho UI. UI collectAsState() để quan sát. Khi trạng thái thay đổi, Compose tự động recompose các thành phần bị ảnh hưởng. Sealed class cho type-safe UI states như Loading, Success, Error." (Use private MutableStateFlow in ViewModel, expose public StateFlow to UI. UI collectAsState() to observe. When state changes, Compose auto-recomposes affected components. Sealed class for type-safe UI states like Loading, Success, Error.)

### Bảo Mật (Security) (09)
> "Xác thực qua Firebase Auth (Email + Google). Token JWT gửi trong header mọi yêu cầu. Backend xác minh bằng Firebase Admin SDK. Kiểm soát truy cập dựa trên vai trò (RBAC) với RolesGuard. Quy tắc Bảo Mật Firestore từ chối theo mặc định, kiểm tra quyền sở hữu." (Authentication via Firebase Auth (Email + Google). JWT token sent in header of every request. Backend verifies with Firebase Admin SDK. Role-based access control with RolesGuard. Firestore Security Rules deny-by-default, check ownership.)

### GPS/Vị Trí (GPS/Location) (06)
> "Dùng FusedLocationProviderClient với LocationRequest. Cập nhật mỗi 5 giây, khoảng cách tối thiểu 5 mét. LocationHelper trả về Flow<Location> qua callbackFlow. GpsViewModel thu thập và gửi lên backend. Shipper cần dịch vụ foreground để theo dõi khi ứng dụng minimize." (Use FusedLocationProviderClient with LocationRequest. Update every 5 seconds, minimum 5 meters. LocationHelper returns Flow<Location> via callbackFlow. GpsViewModel collects and sends to backend. Shipper needs foreground service for tracking when app is minimized.)

---

## ✅ DANH SÁCH KIỂM TRA CÂU HỎI VIVA (VIVA QUESTION CHECKLIST)

Nhóm nên luyện tập trả lời các câu hỏi sau (Team should rehearse the following questions):

### Câu Hỏi Kỹ Thuật (Technical Questions)

- [ ] **Q1**: Tại sao chọn Jetpack Compose thay vì XML?
- [ ] **Q2**: StateFlow khác gì LiveData?
- [ ] **Q3**: MVVM pattern hoạt động như thế nào trong app?
- [ ] **Q4**: Firestore Security Rules bảo vệ data như thế nào?
- [ ] **Q5**: GPS tracking hoạt động ra sao? Battery optimization?
- [ ] **Q6**: FCM notification flow từ backend đến mobile?
- [ ] **Q7**: Order status machine - các transition hợp lệ?
- [ ] **Q8**: Làm sao prevent user sửa order của người khác?
- [ ] **Q9**: Chatbot AI sử dụng model gì? Rate limiting?
- [ ] **Q10**: Tại sao dùng manual DI thay vì Hilt?

### Câu Hỏi Thiết Kế (Design Questions)

- [ ] **Q11**: Tại sao chọn Firebase thay vì tự build?
- [ ] **Q12**: Scalability - app handle được bao nhiêu users?
- [ ] **Q13**: Nếu có thêm 1 tháng, sẽ làm feature gì?
- [ ] **Q14**: Testing strategy? Coverage bao nhiêu?
- [ ] **Q15**: Offline mode có được support không?

### Câu Hỏi Theo Vai Trò (Role-specific Questions)

- [ ] **Q16**: Customer journey từ browse đến nhận hàng?
- [ ] **Q17**: Owner quản lý shop như thế nào?
- [ ] **Q18**: Shipper trip lifecycle (create → deliver)?
- [ ] **Q19**: Voucher validation flow?
- [ ] **Q20**: Payment methods được support?

---

## 📖 THAM CHIẾU NHANH - CÂUI TRẢ LỜI (QUICK REFERENCE ANSWERS)

### Q1: Tại sao dùng Compose? (Why Compose?)
- Google khuyến nghị, hiện đại, ít code hơn, type-safe (Google recommends, modern, less code, type-safe)
- State-driven UI phù hợp MVVM (State-driven UI fits MVVM)
- Công cụ tốt hơn (Better tooling) (Preview, hot reload)

### Q2: StateFlow khác gì LiveData? (StateFlow vs LiveData)
- StateFlow: Dựa trên Kotlin Flow, không cần Lifecycle owner (Kotlin Flow-based, no Lifecycle owner needed)
- LiveData: Android-specific, tự động tạm dừng (auto-pause)
- Dự án dùng StateFlow để thống nhất (Project uses StateFlow for consistency)

### Q5: GPS tracking như thế nào? (GPS tracking)
- FusedLocationProviderClient (Google Play Services)
- Khoảng 5 giây, khoảng cách tối thiểu 5 mét (5-second interval, 5-meter minimum distance)
- Mẫu callbackFlow, thu thập trong ViewModel (callbackFlow pattern, collect in ViewModel)
- Cần Dịch vụ Foreground cho theo dõi nền (Foreground Service for background tracking)

### Q7: Chuyển đổi trạng thái đơn hàng (Order transitions)
```
CHỜ_XỬ_LÝ → ĐÃ_XÁC_NHẬN → CHUẨN_BỊ → SẴN_SÀNG → ĐANG_GIAO → ĐÃ_GIAO
(PENDING → CONFIRMED → PREPARING → READY → SHIPPING → DELIVERED)
   ↓          ↓           ↓
ĐÃ_HỦY (chỉ trước READY) 
(CANCELLED - only before READY)
```

### Q11: Tại sao dùng Firebase? (Why Firebase?)
- Tất cả trong một: Xác thực, Cơ sở dữ liệu, Lưu trữ, Hosting, Push (All-in-one: Auth, DB, Storage, Hosting, Push)
- Tier miễn phí đủ cho MVP (Free tier enough for MVP)
- Tự động mở rộng (Auto-scale)
- SDK tốt cho Android (Good SDK for Android)

---

## 🚀 MẸO THUYẾT TRÌNH (PRESENTATION TIPS)

1. **Mở đầu** (Opening): Demo ứng dụng trước, giải thích kỹ thuật sau (Demo app first, explain tech after)
2. **Kiến trúc** (Architecture): Vẽ sơ đồ đơn giản trên bảng trắng (Draw simple diagram on whiteboard)
3. **Mã** (Code): Mở IDE, hiển thị mã thực tế khi được hỏi (Open IDE, show actual code when asked)
4. **Thành thật** (Honest): Nêu rõ giới hạn, đừng quảng cáo quá (State limitations clearly, don't oversell)
5. **Tương lai** (Future): Có lộ trình rõ ràng nếu được hỏi (Have clear roadmap if asked)

---

## 📁 VỊ TRÍ TỆP (FILE LOCATIONS)

```
MobileProject/
├── docs/
│   └── viva/
│       ├── README.md                          ← YOU ARE HERE
│       ├── 00_CODEBASE_MAP.md
│       ├── 01_PROJECT_OVERVIEW.md
│       ├── 02_TECHSTACK.md
│       ├── 03_ARCHITECTURE.md
│       ├── 04_FEATURES_BY_ROLE.md
│       ├── 05_MAIN_FLOWS.md
│       ├── 06_BUSINESS_LOGIC_DEEP_DIVE.md
│       ├── 07_JETPACK_COMPOSE_VS_XML.md
│       ├── 08_STATE_MANAGEMENT.md
│       ├── 09_SECURITY_AND_PERMISSIONS.md
│       └── 10_LIMITATIONS_AND_FUTURE_WORK.md
├── FoodApp/                                   ← Android source
├── Backend/                                   ← NestJS source
└── Admin/                                     ← React Admin panel
```

---

## 📊 THỐNG KÊ (STATS)

| Chỉ Số (Metric) | Giá Trị (Value) |
|--------|--------|
| Tổng tài liệu (Total docs) | 11 tệp (files) |
| Tổng dòng (Total lines) | ~5000+ |
| Backend tests | 26 bộ (suites), 425+ tests |
| Vai trò (Roles) | 4 (Khách hàng, Chủ quán, Shipper, Admin) (Customer, Owner, Shipper, Admin) |
| Tech stack | Kotlin + NestJS + Firebase |

---

**Chúc bạn thành công với viva! 🎓 (Good luck with the viva! 🎓)**
