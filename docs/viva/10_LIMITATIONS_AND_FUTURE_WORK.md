# 10. Hạn Chế & Hướng Phát Triển Tương Lai

> **Mục đích**: Nêu rõ các giới hạn của MVP và hướng phát triển trong tương lai.  
> **Dành cho**: Bảo vệ đồ án – trả lời câu hỏi về scope, improvements, và roadmap.

---

## 1. ĐỊNH NGHĨA SCOPE CỦA MVP

### 1.1 Trong phạm vi (Đã triển khai)

| Danh mục | Tính năng |
|----------|----------|
| **Xác thực** | Email/Mật khẩu, Google Sign-In, Chọn vai trò |
| **Khách hàng** | Duyệt quán, Đặt hàng, Thanh toán (COD + Ngân hàng), Theo dõi, Đánh giá, Chat, Chatbot |
| **Chủ quán** | Quản lý quán, Sản phẩm, Đơn hàng, Voucher, Dashboard doanh thu |
| **Shipper** | Chấp nhận đơn, Theo dõi GPS, Quản lý chuyến, Tính kiếm |
| **Thông báo** | Push notification FCM, Trung tâm thông báo trong ứng dụng |
| **Thanh toán** | COD, Chuyển khoản ngân hàng (SePay QR) |

### 1.2 Ngoài phạm vi (Chưa triển khai)

| Tính năng | Lý do | Ưu tiên trong tương lai |
|----------|-------|----------------------|
| **Apple Sign-In** | Riêng cho iOS, giới hạn thời gian | Trung bình |
| **Xác thực OTP qua SMS** | Cần setup nhà cung cấp SMS | Thấp |
| **Chat thực tế** | WebSocket phức tạp | Trung bình |
| **Hình ảnh/Thoại trong chat** | Chi phí lưu trữ và băng thông | Thấp |
| **Nhiều địa chỉ cho người dùng** | Giới hạn thời gian | Trung bình |
| **Đặt hàng theo lịch** | Logic lập lịch phức tạp | Thấp |
| **Đa ngôn ngữ** | Thời gian setup i18n | Trung bình |
| **Chế độ tối** | Polish UI | Thấp |
| **Chế độ ngoại tuyến** | Logic đồng bộ phức tạp | Cao |
| **Cổng thanh toán (VNPay, Momo)** | Cần tài khoản merchant | Cao |

---

## 2. HẠN CHẾ KỸ THUẬT

### 2.1 Hạn Chế Kiến Trúc

| Lĩnh vực | Trạng thái hiện tại | Hạn chế | Tác động |
|---------|-------------------|--------|---------|
| **Cơ sở dữ liệu** | Firestore | Hạn chế NoSQL (join, transaction) | Các query phức tạp cần denormalize |
| **Backend** | Single Cloud Function | Cold start (~1-3s) | Request đầu tiên chậm |
| **Real-time** | Polling cho một số tính năng | Không phải real-time thực | Latency cao hơn, tiêu tốn pin |
| **Lưu trữ file** | Firebase Storage | Không có CDN tối ưu | Tải hình ảnh chậm |

### 2.2 Hạn Chế Di Động

| Lĩnh vực | Trạng thái hiện tại | Hạn chế |
|---------|-------------------|--------|
| **Ngoại tuyến** | Không hỗ trợ | Ứng dụng không sử dụng được mà không internet |
| **GPS nền** | Chỉ foreground | Theo dõi dừng khi app minimize |
| **Lưu trữ hình ảnh** | Lưu trữ Coil cơ bản | Hình ảnh lớn có thể tải lại |
| **Deep linking** | Triển khai cơ bản | Không có universal links |

### 2.3 Hạn Chế Backend

| Lĩnh vực | Trạng thái hiện tại | Hạn chế |
|---------|-------------------|--------|
| **Mở rộng** | Firebase auto-scale | Giới hạn bởi tier giá Firebase |
| **Tìm kiếm** | Query Firestore cơ bản | Không có tìm kiếm toàn văn bản |
| **Phân tích** | Dashboard cơ bản | Không có công cụ BI nâng cao |
| **Giám sát** | Firebase Crashlytics | APM hạn chế |

---

## 3. CÁC VẤN ĐỀ ĐÃ BIẾT

### 3.1 Nợ Kỹ Thuật

| Vấn đề | Mô tả | Ưu tiên |
|--------|-------|---------|
| **Manual DI** | Không dùng Hilt/Koin - verbose ViewModelFactory | Trung bình |
| **Mẫu state hỗn hợp** | Một số ViewModel dùng LiveData, một số dùng StateFlow | Thấp |
| **Không lưu trữ Repository** | Mỗi lệnh gọi gọi network | Trung bình |
| **Strings hardcoded** | Một số string UI không trong resources | Thấp |

### 3.2 Vấn Đề UX

| Vấn đề | Mô tả | Ưu tiên |
|--------|-------|---------|
| **Không có skeleton loading** | Màn hình trắng trong khi tải | Trung bình |
| **Thông báo lỗi cơ bản** | "Có lỗi xảy ra" chung chung | Trung bình |
| **Không có cơ chế thử lại** | Người dùng phải thử lại yêu cầu thất bại thủ công | Trung bình |
| **Không có hướng dẫn** | Người dùng mới có thể bối rối | Thấp |

---

## 4. CÁC KHÍA CẠNH HIỆU NĂNG

### 4.1 Hiệu Năng Hiện Tại

| Chỉ số | Hiện tại | Mục tiêu |
|-------|---------|---------|
| **Thời gian khởi động ứng dụng** | ~2-3s | <1.5s |
| **Thời gian phản hồi API** | ~500ms-2s | <500ms |
| **Tải hình ảnh** | ~1-2s | <500ms |
| **Tần suất cập nhật GPS** | 5 giây | 3 giây |

### 4.2 Các Điểm Nghẽn

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CÁC ĐIỂM NGHẼN HIỆU NĂNG                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   1. Cold Start (Cloud Functions)                                       │
│      Vấn đề: Request đầu tiên sau khi idle mất 1-3 giây                 │
│      Giải pháp: Scheduled warming, di chuyển tới máy chủ luôn-on       │
│                                                                          │
│   2. Tải Hình Ảnh                                                       │
│      Vấn đề: Hình ảnh sản phẩm lớn tải chậm                            │
│      Giải pháp: Image CDN, responsive images, lazy loading             │
│                                                                          │
│   3. Firestore Reads                                                    │
│      Vấn đề: Nhiều lần đọc cho dữ liệu nested (shop + products)        │
│      Giải pháp: Denormalization, batch reads, caching                  │
│                                                                          │
│   4. GPS Polling                                                        │
│      Vấn đề: Khoảng 5 giây có thể bỏ lỡ những ngoặt                   │
│      Giải pháp: Adaptive polling, motion detection                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 5. CÁC CẢI THIỆN TRONG TƯƠNG LAI

### 5.1 Ngắn hạn (1-3 tháng)

| Tính năng | Mô tả | Nỗ lực |
|----------|-------|--------|
| **Chế độ ngoại tuyến** | Lưu cache đơn hàng, sản phẩm để xem ngoại tuyến | Cao |
| **Tối ưu hình ảnh** | Định dạng WebP, lazy loading, CDN | Trung bình |
| **Skeleton loading** | Hiển thị placeholder trong khi tải | Thấp |
| **Xử lý lỗi** | Thông báo lỗi tốt hơn, auto-retry | Trung bình |
| **Hilt DI** | Thay thế factories thủ công | Trung bình |

### 5.2 Trung hạn (3-6 tháng)

| Tính năng | Mô tả | Nỗ lực |
|----------|-------|--------|
| **Chat thực tế** | Messaging dựa trên WebSocket | Cao |
| **Tích hợp VNPay/Momo** | Phương thức thanh toán phổ biến | Cao |
| **Đa ngôn ngữ** | Tiếng Việt + Tiếng Anh | Trung bình |
| **Tìm kiếm nâng cao** | Tích hợp Algolia/Elasticsearch | Cao |
| **Đặt hàng theo lịch** | Pre-order cho tương lai | Trung bình |

### 5.3 Dài hạn (6-12 tháng)

| Tính năng | Mô tả | Nỗ lực |
|----------|-------|--------|
| **Phiên bản iOS** | Swift/SwiftUI hoặc KMM | Rất cao |
| **Ứng dụng Rider** | Ứng dụng shipper chuyên dụng | Cao |
| **Ứng dụng Admin di động** | Quản lý cửa hàng trên di động | Trung bình |
| **ML recommendations** | Gợi ý thức ăn được cá nhân hóa | Cao |
| **Chương trình loyalty** | Điểm, tier, phần thưởng | Trung bình |

---

## 6. TRẠNG THÁI KIỂM TRA

### 6.1 Kiểm Tra Backend

| Loại | Phạm vi | Trạng thái |
|------|--------|-----------|
| **Unit Tests** | 26 bộ test, 425+ test | ✅ Tất cả pass |
| **Integration Tests** | Các luồng chính được kiểm tra | ✅ Triển khai |
| **E2E Tests** | Luồng đơn hàng, luồng xác thực | ⚠️ Bán phần |
| **Load Tests** | Chưa triển khai | ❌ Thiếu |

**Tệp Test:**
- `vouchers.service.spec.ts` - Logic voucher
- `order-state-machine.service.spec.ts` - Chuyển đổi trạng thái đơn hàng
- `notifications.service.spec.ts` - Push notification
- `cart.service.spec.ts` - Thao tác giỏ hàng
- `products-soldcount.spec.ts` - Cập nhật hàng tồn kho

### 6.2 Kiểm Tra Di Động

| Loại | Phạm vi | Trạng thái |
|------|--------|-----------|
| **Unit Tests** | Logic ViewModel | ⚠️ Bán phần |
| **UI Tests** | Compose testing | ❌ Thiếu |
| **Integration Tests** | API + UI | ❌ Thiếu |
| **Manual Testing** | Tất cả tính năng | ✅ Hoàn thành |

### 6.3 Cải Thiện Test Cần Thiết

| Cải thiện | Ưu tiên | Nỗ lực |
|----------|--------|--------|
| Unit tests di động cho ViewModel | Cao | Trung bình |
| Compose UI tests | Trung bình | Cao |
| Backend load testing | Trung bình | Trung bình |
| E2E tests với Firebase thực | Thấp | Cao |

---

## 7. CÁC KHÍA CẠNH MỞ RỘNG

### 7.1 Công Suất Hiện Tại

| Chỉ số | Giới hạn | Ghi chú |
|-------|---------|--------|
| **Người dùng đồng thời** | ~1000 | Plan Spark Firebase |
| **Firestore reads** | 50K/ngày | Tier miễn phí |
| **Cloud Function invocations** | 2M/tháng | Tier miễn phí |
| **Lưu trữ** | 5GB | Tier miễn phí |

### 7.2 Chiến Lược Mở Rộng

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       ROADMAP MỞ RỘNG                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Giai đoạn 1: Firebase Blaze Plan                                      │
│   - Giá trả theo cách sử dụng                                           │
│   - Xóa giới hạn tier miễn phí                                          │
│   - Chi phí dự kiến: $50-200/tháng cho 10K users                       │
│                                                                          │
│   Giai đoạn 2: Tối ưu Backend                                           │
│   - Thêm lớp caching Redis                                              │
│   - Di chuyển đến Cloud Run (luôn-on)                                   │
│   - Triển khai connection pooling                                       │
│                                                                          │
│   Giai đoạn 3: Sharding Cơ sở dữ liệu                                  │
│   - Tách workload đọc/ghi                                               │
│   - Các instance Firestore khu vực                                      │
│   - Denormalize dữ liệu hot                                             │
│                                                                          │
│   Giai đoạn 4: CDN & Edge                                               │
│   - CloudFlare/Fastly cho static assets                                 │
│   - Edge functions cho xác thực                                         │
│   - API endpoints khu vực                                               │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 8. CÁC CẢI THIỆN BẢO MẬT

| Cải thiện | Hiện tại | Mục tiêu |
|----------|---------|---------|
| **Giới hạn tốc độ** | Chỉ chatbot | Tất cả endpoints |
| **Vệ sinh đầu vào** | Xác thực cơ bản | Ngăn chặn XSS nâng cao |
| **Audit logging** | Bán phần | Toàn diện |
| **Penetration testing** | Không có | Hàng năm |
| **GDPR compliance** | Cơ bản | Tuân thủ đầy đủ |

---

## 9. CÂU HỎI THƯỜNG GẶP

**Q: Tại sao không có chế độ ngoại tuyến?**  
A: Đồng bộ ngoại tuyến phức tạp (xung đột, quản lý hàng chờ). MVP tập trung vào online-first. Có thể thêm sau bằng Room database + WorkManager.

**Q: Tại sao dùng Firebase thay vì tự xây backend?**  
A: Firebase cung cấp auth, database, storage, hosting, push notification trong một package. Phù hợp MVP và học thuật. Nếu scale production có thể migrate dần dần.

**Q: Có bao nhiêu test?**  
A: Backend có 26 bộ test, 425+ test (unit + integration). Kiểm tra di động chủ yếu thủ công do giới hạn thời gian.

**Q: Giới hạn số user là bao nhiêu?**  
A: Tier miễn phí ~1000 người dùng đồng thời. Plan Blaze có thể scale tới hàng triệu với chi phí tương ứng.

**Q: Nếu có thêm thời gian, sẽ làm gì đầu tiên?**  
A: 
1. Chế độ ngoại tuyến (cải thiện UX lớn nhất)
2. Unit tests di động (khả năng bảo trì)
3. Chat thực tế với WebSocket
4. Tích hợp cổng thanh toán (VNPay/Momo)

**Q: Tại sao cần phải làm những hạn chế này?**  
A: Đây là dự án học thuật với thời gian hạn chế (1 học kỳ). Chúng tôi ưu tiên tính năng lõi: authentication, orders, GPS, notifications. Các tính năng phụ (offline, chat real-time) có thể thêm sau. Chiến lược MVP này cho phép chúng tôi cung cấp sản phẩm chất lượng cao với đầy đủ test thay vì công năng đầy đủ nhưng kém chất lượng.
