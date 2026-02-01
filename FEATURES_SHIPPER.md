# CHI TIẾT CHỨC NĂNG - VAI TRÒ SHIPPER (TÀI XẾ GIAO HÀNG)

> **Mục đích:** Mô tả chi tiết các chức năng của vai trò Shipper  
> **Ngày cập nhật:** 01/02/2026

---

## 1. ĐĂNG KÝ LÀM SHIPPER

### Mô tả ngắn
Chức năng đăng ký cho phép người dùng đăng ký trở thành tài xế giao hàng bằng cách điền thông tin cá nhân, upload giấy tờ xác minh (CCCD mặt trước/sau, bằng lái xe), và chọn cửa hàng muốn làm việc. Sau khi gửi đơn, shipper chờ chủ shop phê duyệt trước khi có thể bắt đầu nhận đơn hàng.

### Kịch bản sử dụng (Case Study)

**Bước 1: Người dùng mở màn hình đăng ký**
- Từ màn hình chính app, người dùng nhấn vào "Đăng ký làm Shipper"
- Hệ thống hiển thị màn hình `ShipperRegistrationScreen`
- Màn hình chào mừng với icon shipper và dòng chữ:
  - "Trở thành Shipper của KTX Delivery"
  - "Kiếm thu nhập linh hoạt với lịch làm việc tự do"
- Nút "Bắt đầu đăng ký"

**Bước 2: Điền thông tin cá nhân**
- Người dùng nhấn "Bắt đầu đăng ký"
- Form hiển thị các trường:
  - **Họ và tên**: Ô nhập text
  - **Số điện thoại**: Ô nhập số (tự động lấy từ tài khoản)
  - **Email**: Ô nhập email
  - **Ngày sinh**: Date picker
  - **Địa chỉ**: Ô nhập text
- Người dùng điền:
  - Họ tên: Trần Văn B
  - Số điện thoại: 097-xxx-xxxx (đã điền sẵn)
  - Email: tranvanb@gmail.com
  - Ngày sinh: 15/03/1995
  - Địa chỉ: 123 Nguyễn Huệ, Q.1, TP.HCM

**Bước 3: Upload CCCD mặt trước**
- Người dùng nhấn nút "Tiếp theo"
- Màn hình chuyển sang bước 2: "Upload CCCD"
- Hiển thị khung "Mặt trước CCCD" với icon camera
- Hướng dẫn: "Chụp rõ ràng, không bị mờ hay lóe sáng"
- Người dùng nhấn vào khung
- Bottom sheet hiển thị:
  - 📷 Chụp ảnh
  - 🖼️ Chọn từ thư viện
- Người dùng chọn "Chụp ảnh"

**Bước 4: Chụp ảnh CCCD với camera**
- Camera mở với overlay hình chữ nhật định vị
- Hướng dẫn: "Đặt CCCD vào khung hình"
- Người dùng chụp ảnh
- Preview hiển thị với 2 nút:
  - "Chụp lại"
  - "Sử dụng ảnh này"
- Người dùng nhấn "Sử dụng ảnh này"
- Ảnh hiển thị trong khung với icon tick xanh

**Bước 5: Upload CCCD mặt sau**
- Khung "Mặt sau CCCD" xuất hiện bên dưới
- Người dùng làm tương tự bước 3-4
- Cả 2 ảnh CCCD đều hiển thị với icon tick xanh
- Nút "Tiếp theo" được kích hoạt (không còn xám)

**Bước 6: Nhập thông tin CCCD**
- Người dùng nhấn "Tiếp theo"
- Form yêu cầu nhập:
  - **Số CCCD**: Ô nhập số (12 số)
  - **Ngày cấp**: Date picker
  - **Nơi cấp**: Dropdown (Chọn tỉnh/thành phố)
- Người dùng điền:
  - Số CCCD: 001095xxxxxx
  - Ngày cấp: 01/01/2020
  - Nơi cấp: TP. Hồ Chí Minh

**Bước 7: Upload bằng lái xe**
- Người dùng nhấn "Tiếp theo"
- Màn hình chuyển sang bước 3: "Upload Bằng lái xe"
- Hiển thị khung với icon camera
- Hướng dẫn: "Chụp rõ thông tin trên bằng lái"
- Người dùng chụp ảnh bằng lái
- Ảnh hiển thị với icon tick xanh

**Bước 8: Nhập thông tin bằng lái**
- Form yêu cầu:
  - **Loại bằng**: Dropdown (A1, A2, B1, B2...)
  - **Số bằng**: Ô nhập số
  - **Ngày cấp**: Date picker
  - **Ngày hết hạn**: Date picker
- Người dùng điền:
  - Loại: A1 (xe máy)
  - Số bằng: 12345678
  - Ngày cấp: 15/03/2018
  - Hết hạn: 15/03/2028

**Bước 9: Chọn cửa hàng**
- Người dùng nhấn "Tiếp theo"
- Màn hình chuyển sang bước 4: "Chọn cửa hàng"
- Danh sách các cửa hàng hiển thị:
  - Tên cửa hàng: Phở Hà Nội
  - Địa chỉ: 456 Lê Lợi, Q.1
  - Khoảng cách: 2.5 km
  - Số shipper hiện tại: 5 người
- Người dùng chọn "Phở Hà Nội"
- Cửa hàng được đánh dấu với icon tick

**Bước 10: Xem lại thông tin và gửi đơn**
- Người dùng nhấn "Tiếp theo"
- Màn hình "Xác nhận thông tin" hiển thị tóm tắt:
  - ✅ Thông tin cá nhân
  - ✅ CCCD (2 ảnh)
  - ✅ Bằng lái xe (1 ảnh)
  - ✅ Cửa hàng: Phở Hà Nội
- Checkbox: "Tôi đồng ý với Điều khoản sử dụng"
- Người dùng check vào checkbox
- Nhấn nút "Gửi đơn đăng ký"

**Bước 11: Hệ thống xử lý**
- Loading spinner hiển thị
- Hệ thống:
  - Upload 3 ảnh lên server
  - Lưu thông tin vào database
  - Gửi thông báo cho chủ shop
  - Tạo bản ghi đăng ký với trạng thái "Pending"

**Bước 12: Màn hình thành công**
- Sau 3-5 giây, hiển thị màn hình thành công:
  - Icon tick lớn màu xanh
  - "Đơn đăng ký đã được gửi!"
  - "Cửa hàng Phở Hà Nội sẽ xem xét đơn của bạn trong vòng 24-48 giờ."
  - "Chúng tôi sẽ thông báo kết quả qua ứng dụng."
- Nút "Về trang chủ"

**Bước 13: Chờ duyệt**
- Người dùng quay về màn hình chính
- Trạng thái hiển thị: "Đơn đăng ký đang chờ xét duyệt"
- Badge màu vàng: "Pending"
- Người dùng chưa thể nhận đơn hàng
- Khi chủ shop phê duyệt → Push notification: "Chúc mừng! Bạn đã được duyệt làm shipper"

### Screenshot cần chụp
- [ ] `ShipperRegistrationScreen`: Màn hình chào mừng
- [ ] `ShipperRegistrationScreen`: Form điền thông tin cá nhân
- [ ] `ShipperRegistrationScreen`: Upload CCCD mặt trước/sau
- [ ] Camera overlay với khung định vị
- [ ] Preview ảnh với nút "Chụp lại" / "Sử dụng ảnh này"
- [ ] `ShipperRegistrationScreen`: Upload bằng lái xe
- [ ] `ShipperRegistrationScreen`: Chọn cửa hàng (danh sách)
- [ ] `ShipperRegistrationScreen`: Màn hình xác nhận thông tin
- [ ] `ShipperRegistrationScreen`: Màn hình thành công
- [ ] Trạng thái "Pending" trên màn hình chính

---

## 2. BẬT/TẮT TRẠNG THÁI ONLINE

### Mô tả ngắn
Chức năng bật/tắt trạng thái online cho phép shipper điều khiển khả năng nhận đơn hàng. Khi bật online, shipper subscribe vào topic notification của shop và nhận thông báo khi có đơn sẵn sàng giao. Khi tắt, shipper unsubscribe và ngừng nhận thông báo mới.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper mở app sau khi được duyệt**
- Sau khi được phê duyệt, shipper mở app
- Màn hình `ShipperHomeScreen` hiển thị
- Phía trên có toggle switch lớn:
  - Trạng thái hiện tại: "Offline" (màu xám)
  - Switch đang ở vị trí tắt
  - Icon shipper màu xám
- Dưới switch hiển thị:
  - "Bạn đang offline"
  - "Bật online để bắt đầu nhận đơn hàng"

**Bước 2: Bật trạng thái online**
- Shipper kéo switch sang phải (hoặc nhấn vào switch)
- Hệ thống hiển thị loading spinner ngắn

**Bước 3: Hệ thống xử lý**
- Backend:
  - Cập nhật trạng thái shipper → "Online"
  - Subscribe shipper vào topic: `shop_${shopId}_shippers_active`
  - Lưu thời gian bật online
- UI cập nhật:
  - Switch chuyển sang màu xanh
  - Trạng thái: "Online" (xanh)
  - Icon shipper đổi sang màu xanh sáng
  - Animation wave phát ra từ icon (biểu thị đang active)

**Bước 4: Màn hình sau khi online**
- Dưới toggle hiển thị:
  - ✅ "Bạn đang online"
  - "Sẵn sàng nhận đơn từ Phở Hà Nội"
  - Thời gian: "Online từ 08:30 AM"
- Phần "Đơn sẵn sàng giao" xuất hiện (nếu có đơn)
- Nếu chưa có đơn:
  - Icon hộp trống
  - "Chưa có đơn nào sẵn sàng"
  - "Chúng tôi sẽ thông báo khi có đơn mới"

**Bước 5: Nhận thông báo đơn mới**
- Khi chủ shop đánh dấu đơn "Sẵn sàng giao"
- Push notification xuất hiện:
  - **Tiêu đề**: "Đơn hàng mới sẵn sàng!"
  - **Nội dung**: "ORDER-12345 - 150,000đ - 2.5km"
  - **Icon**: Logo shop
  - **Âm thanh**: Notification sound
- Badge trên app icon tăng lên
- Nếu shipper đang trong app → Danh sách "Đơn sẵn sàng giao" tự động cập nhật

**Bước 6: Tắt trạng thái online**
- Sau khi làm việc xong, shipper muốn nghỉ
- Kéo switch về trái (tắt)
- Dialog xác nhận hiển thị:
  - "Bạn có chắc muốn offline?"
  - "Bạn sẽ không nhận được thông báo đơn hàng mới"
  - Nút "Hủy" và "Xác nhận"
- Shipper nhấn "Xác nhận"

**Bước 7: Hệ thống xử lý khi tắt**
- Backend:
  - Cập nhật trạng thái → "Offline"
  - Unsubscribe khỏi topic notification
  - Lưu thời gian tắt online
  - Tính tổng thời gian online trong ngày
- UI cập nhật:
  - Switch chuyển về màu xám
  - Trạng thái: "Offline"
  - Icon shipper màu xám
  - Animation wave dừng lại

**Bước 8: Thống kê sau khi offline**
- Màn hình hiển thị:
  - "Bạn đã offline"
  - "Thời gian online hôm nay: 6 giờ 30 phút"
  - "Đơn đã giao: 12 đơn"
  - "Thu nhập ước tính: 360,000đ"
- Nút "Xem chi tiết thu nhập"

**Bước 9: Tự động offline khi không hoạt động**
- Nếu shipper online nhưng không tương tác trong 4 giờ
- Hệ thống tự động chuyển sang offline
- Push notification:
  - "Bạn đã tự động offline do không hoạt động"
  - "Bật lại online để tiếp tục nhận đơn"

**Bước 10: Xem trạng thái từ Settings**
- Shipper vào màn hình `ShipperSettingsScreen`
- Phần "Trạng thái làm việc" hiển thị:
  - Toggle switch online/offline
  - Lịch sử online hôm nay:
    - 08:30 - 12:00 (3.5 giờ)
    - 13:30 - 18:00 (4.5 giờ)
    - Tổng: 8 giờ
  - Cài đặt thông báo:
    - ☑️ Âm thanh thông báo
    - ☑️ Rung khi có đơn mới
    - ☑️ Hiển thị popup

### Screenshot cần chụp
- [ ] `ShipperHomeScreen`: Toggle switch offline (xám)
- [ ] `ShipperHomeScreen`: Toggle switch online (xanh) với animation
- [ ] `ShipperHomeScreen`: Trạng thái "Đang online" với thời gian
- [ ] `ShipperHomeScreen`: Màn hình "Chưa có đơn sẵn sàng"
- [ ] Push notification đơn hàng mới
- [ ] Dialog xác nhận tắt online
- [ ] `ShipperHomeScreen`: Thống kê sau khi offline
- [ ] `ShipperSettingsScreen`: Lịch sử online trong ngày
- [ ] Push notification tự động offline

---

## 3. NHẬN ĐƠN HÀNG

### Mô tả ngắn
Chức năng nhận đơn hàng cho phép shipper xem danh sách các đơn hàng đã sẵn sàng giao, xem chi tiết từng đơn (sản phẩm, địa chỉ, giá tiền), và nhận đơn để bắt đầu giao hàng. Khi nhận đơn, trạng thái đơn tự động chuyển từ "Ready" sang "Shipping".

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper online và xem danh sách đơn**
- Shipper đã bật trạng thái online
- Màn hình `ShipperHomeScreen` hiển thị section "Đơn sẵn sàng giao"
- Danh sách đơn hàng với trạng thái "Ready" từ shop
- Mỗi đơn hiển thị:
  - Mã đơn: ORDER-12345
  - Tên shop: Phở Hà Nội
  - Tổng tiền: 150,000 VNĐ
  - Địa chỉ giao: 789 Nguyễn Thị Minh Khai, Q.3
  - Khoảng cách: 2.5 km
  - Thời gian sẵn sàng: "5 phút trước"
  - Nút "Xem chi tiết"

**Bước 2: Nhận thông báo đơn mới**
- Chủ shop đánh dấu đơn mới "Sẵn sàng giao"
- Push notification xuất hiện:
  - "Đơn hàng mới sẵn sàng!"
  - "ORDER-12346 - 120,000đ - 1.8km"
- Shipper nhấn vào notification
- App mở và scroll đến đơn ORDER-12346 trong danh sách
- Đơn mới có badge "MỚI" màu đỏ

**Bước 3: Xem chi tiết đơn hàng**
- Shipper nhấn vào đơn ORDER-12345
- Hệ thống mở màn hình `ShipperOrderDetailScreen`
- Hiển thị đầy đủ thông tin:
  - **Thông tin cửa hàng**:
    - Tên: Phở Hà Nội
    - Địa chỉ: 456 Lê Lợi, Q.1
    - Số điện thoại: 028-xxxx-xxxx
    - Nút "Gọi shop"
  - **Thông tin khách hàng**:
    - Tên: Nguyễn Văn A
    - Địa chỉ: 789 Nguyễn Thị Minh Khai, Q.3
    - Số điện thoại: 098-xxx-xxxx
    - Nút "Gọi khách"
  - **Danh sách sản phẩm**:
    - 2x Phở bò đặc biệt - 120,000đ
    - 1x Trà đá - 10,000đ
  - **Thanh toán**:
    - Tổng món: 130,000đ
    - Phí ship: 20,000đ
    - **Tổng**: 150,000đ
    - Phương thức: COD (Tiền mặt)
  - **Ghi chú**: "Không hành, ít dầu"

**Bước 4: Xem bản đồ và khoảng cách**
- Dưới thông tin có bản đồ nhỏ (mini map)
- Hiển thị:
  - 📍 Vị trí shop (màu xanh)
  - 📍 Vị trí giao hàng (màu đỏ)
  - Đường đi ước tính (đường màu xanh dương)
- Khoảng cách: 2.5 km
- Thời gian ước tính: 10-15 phút
- Nút "Xem bản đồ lớn"

**Bước 5: Quyết định nhận đơn**
- Shipper xem xét:
  - Khoảng cách hợp lý
  - Tiền COD: 150,000đ
  - Địa chỉ giao quen thuộc
- Quyết định nhận đơn
- Nhấn nút "Nhận đơn" (màu xanh lá, ở cuối màn hình)

**Bước 6: Xác nhận nhận đơn**
- Dialog xác nhận hiển thị:
  - "Xác nhận nhận đơn này?"
  - "Bạn cần đến shop để lấy hàng và giao cho khách"
  - Checkbox: "☑️ Tôi sẽ đến shop trong 10 phút"
  - Nút "Hủy" và "Xác nhận"
- Shipper check vào checkbox
- Nhấn "Xác nhận"

**Bước 7: Hệ thống xử lý**
- Loading spinner hiển thị
- Backend:
  - Cập nhật trạng thái đơn: READY → SHIPPING
  - Gán shipper cho đơn hàng
  - Gửi thông báo cho khách: "Đơn hàng đang được giao bởi shipper Trần Văn B"
  - Gửi thông báo cho shop: "Shipper Trần Văn B đã nhận đơn ORDER-12345"

**Bước 8: Màn hình sau khi nhận đơn**
- Toast hiển thị: "Đã nhận đơn thành công"
- Màn hình `ShipperOrderDetailScreen` cập nhật:
  - Badge "ĐÃ NHẬN" màu xanh ở trên cùng
  - Nút "Nhận đơn" đổi thành "Bắt đầu giao hàng"
  - Hiển thị timer: "Thời gian nhận đơn: 09:35 AM"
  - Section "Hành trình giao hàng" xuất hiện:
    - ✅ Nhận đơn - 09:35 AM
    - ⏳ Đến shop lấy hàng
    - ⏳ Giao hàng cho khách
    - ⏳ Hoàn thành

**Bước 9: Quay về danh sách**
- Shipper nhấn back về `ShipperHomeScreen`
- Đơn ORDER-12345 biến mất khỏi "Đơn sẵn sàng giao"
- Xuất hiện section mới: "Đơn đang giao" (1)
- Hiển thị đơn ORDER-12345 với badge "ĐANG GIAO"

**Bước 10: Từ chối nhận đơn**
- Nếu shipper không muốn nhận (ví dụ: khoảng cách xa)
- Nhấn nút "Từ chối"
- Dialog:
  - "Tại sao bạn từ chối đơn này?"
  - Radio buttons:
    - ⭕ Khoảng cách quá xa
    - ⭕ Đang giao đơn khác
    - ⭕ Địa chỉ không rõ
    - ⭕ Lý do khác
- Chọn lý do và xác nhận
- Đơn vẫn ở trạng thái "Ready" cho shipper khác nhận

**Bước 11: Gọi điện cho shop/khách**
- Từ màn hình chi tiết đơn
- Shipper nhấn "Gọi shop"
- Hệ thống mở app điện thoại với số đã điền sẵn: 028-xxxx-xxxx
- Shipper hỏi về sản phẩm hoặc địa chỉ
- Làm tương tự với nút "Gọi khách"

**Bước 12: Xem nhiều đơn cùng lúc**
- Nếu có nhiều đơn sẵn sàng (5-10 đơn)
- Shipper có thể sort/filter:
  - 💰 Theo tiền (cao → thấp)
  - 📍 Theo khoảng cách (gần → xa)
  - ⏰ Theo thời gian (mới → cũ)
- Nút "Chọn nhiều đơn" để tạo chuyến giao hàng tối ưu (xem feature #4)

### Screenshot cần chụp
- [ ] `ShipperHomeScreen`: Danh sách đơn sẵn sàng giao
- [ ] Push notification đơn mới
- [ ] `ShipperOrderDetailScreen`: Chi tiết đơn hàng đầy đủ
- [ ] `ShipperOrderDetailScreen`: Mini map với 2 địa điểm
- [ ] `ShipperOrderDetailScreen`: Nút "Nhận đơn"
- [ ] Dialog xác nhận nhận đơn
- [ ] Toast "Đã nhận đơn thành công"
- [ ] `ShipperOrderDetailScreen`: Sau khi nhận (badge "ĐÃ NHẬN")
- [ ] `ShipperHomeScreen`: Section "Đơn đang giao"
- [ ] Dialog từ chối đơn với lý do
- [ ] Sort/filter đơn hàng

---

## 4. GPS VÀ LỘ TRÌNH GIAO HÀNG

### Mô tả ngắn
Chức năng GPS và lộ trình giao hàng cho phép shipper chọn nhiều đơn để tạo chuyến, tự động tối ưu lộ trình bằng Google Routes API, hiển thị bản đồ với các điểm dừng, navigation từng điểm, và tracking vị trí realtime. Đây là tính năng quan trọng giúp shipper giao nhiều đơn một cách hiệu quả.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper có nhiều đơn đang giao**
- Shipper đã nhận 3 đơn hàng:
  - ORDER-12345 - 789 Nguyễn Thị Minh Khai, Q.3
  - ORDER-12346 - 456 Lý Tự Trọng, Q.1
  - ORDER-12347 - 123 Pasteur, Q.1
- Trên `ShipperHomeScreen`, section "Đơn đang giao" hiển thị cả 3 đơn
- Nút "Tạo chuyến giao hàng" (màu xanh) xuất hiện

**Bước 2: Tạo chuyến giao hàng**
- Shipper nhấn "Tạo chuyến giao hàng"
- Hệ thống chuyển đến màn hình `GpsScreen`
- Danh sách 3 đơn hiển thị với checkbox:
  - ☑️ ORDER-12345 - Q.3 - 2.5km
  - ☑️ ORDER-12346 - Q.1 - 1.8km
  - ☑️ ORDER-12347 - Q.1 - 2.2km
- Tất cả đều được chọn mặc định
- Nút "Tối ưu lộ trình" ở cuối

**Bước 3: Tối ưu lộ trình với Google Routes API**
- Shipper nhấn "Tối ưu lộ trình"
- Loading overlay hiển thị:
  - Icon bản đồ đang xoay
  - "Đang tính toán lộ trình tối ưu..."
  - Progress bar
- Hệ thống:
  - Gọi Google Routes API
  - Truyền vào: vị trí hiện tại + 3 địa chỉ giao hàng
  - Nhận về: lộ trình tối ưu (shortest path) + ETA

**Bước 4: Hiển thị bản đồ với lộ trình**
- Sau 2-3 giây, màn hình chuyển đến `DeliveryMapScreen`
- Bản đồ toàn màn hình hiển thị:
  - 📍 **Vị trí hiện tại** (shipper) - chấm xanh lá, nhấp nháy
  - 🏠 **Điểm 1** (ORDER-12346) - Lý Tự Trọng, Q.1 - số thứ tự "1"
  - 🏠 **Điểm 2** (ORDER-12347) - Pasteur, Q.1 - số thứ tự "2"
  - 🏠 **Điểm 3** (ORDER-12345) - Nguyễn Thị Minh Khai, Q.3 - số thứ tự "3"
  - Đường đi màu xanh dương nối các điểm theo thứ tự tối ưu

**Bước 5: Thông tin tổng quan chuyến đi**
- Bottom sheet (có thể kéo lên/xuống) hiển thị:
  - 🚀 **Chuyến #001**
  - 📦 **3 đơn hàng**
  - 📍 **Tổng khoảng cách**: 6.5 km
  - ⏱️ **Thời gian ước tính**: 25-30 phút
  - 💰 **Tổng tiền COD**: 420,000đ
  - Nút "Bắt đầu giao hàng" (màu xanh lớn)

**Bước 6: Bắt đầu chuyến giao hàng**
- Shipper nhấn "Bắt đầu giao hàng"
- Dialog xác nhận:
  - "Bạn đã đến shop và lấy đủ hàng?"
  - Checkbox: "☑️ Tôi đã lấy đủ 3 đơn hàng"
  - Nút "Chưa" và "Đã sẵn sàng"
- Shipper check và nhấn "Đã sẵn sàng"

**Bước 7: Navigation đến điểm đầu tiên**
- Bản đồ zoom vào lộ trình hiện tại
- Card điểm đến xuất hiện ở trên:
  - 🏠 **Điểm 1 / 3**
  - ORDER-12346
  - Khách: Trần Thị B
  - Địa chỉ: 456 Lý Tự Trọng, Q.1
  - 📍 1.8 km - ⏱️ 8 phút
  - 💰 COD: 120,000đ
  - Nút "Gọi khách"
- Navigation bắt đầu:
  - Vị trí shipper cập nhật realtime (chấm xanh di chuyển)
  - Đường đi màu xanh dương từ vị trí hiện tại đến điểm 1
  - Mũi tên chỉ hướng đi

**Bước 8: Hướng dẫn rẽ (Turn-by-turn)**
- Khi shipper di chuyển, hệ thống voice guidance:
  - "Đi thẳng 200 mét"
  - "Rẽ trái vào đường Lý Tự Trọng"
  - "Điểm đến ở bên phải, sau 50 mét"
- Trên màn hình hiển thị:
  - Icon mũi tên lớn (thẳng, trái, phải)
  - Khoảng cách đến điểm rẽ: "50m"
  - Tên đường tiếp theo: "Lý Tự Trọng"

**Bước 9: Đến điểm đầu tiên**
- Khi shipper đến gần điểm 1 (trong vòng 50m)
- Hệ thống rung và hiển thị:
  - "Bạn đã đến điểm giao hàng!"
  - Nút "Giao hàng" (màu xanh lớn)
- Shipper giao hàng cho khách
- Nhấn nút "Giao hàng"

**Bước 10: Xác nhận giao hàng**
- Dialog hiển thị:
  - "Xác nhận đã giao ORDER-12346?"
  - "Khách: Trần Thị B"
  - "COD: 120,000đ"
  - Checkbox: "☑️ Đã thu tiền COD"
  - Ô nhập ghi chú (optional)
- Shipper check và nhấn "Xác nhận"
- Toast: "Đã giao đơn 1/3"

**Bước 11: Chuyển sang điểm tiếp theo**
- Điểm 1 đổi màu xanh (hoàn thành) trên bản đồ
- Card cập nhật:
  - 🏠 **Điểm 2 / 3**
  - ORDER-12347
  - Khách: Nguyễn Văn C
  - Địa chỉ: 123 Pasteur, Q.1
  - 📍 0.5 km - ⏱️ 3 phút
  - 💰 COD: 150,000đ
- Navigation tự động chuyển sang điểm 2
- Đường đi cập nhật từ vị trí hiện tại → điểm 2

**Bước 12: Hoàn thành tất cả điểm**
- Shipper lặp lại bước 9-10 cho điểm 2 và 3
- Sau khi giao xong điểm cuối cùng:
  - Màn hình hiển thị animation celebration
  - "Chúc mừng! Hoàn thành chuyến đi"
  - 📦 3/3 đơn đã giao
  - ⏱️ Thời gian: 28 phút
  - 📍 Tổng quãng đường: 6.3 km
  - 💰 Tổng thu COD: 420,000đ
  - Nút "Xem chi tiết" và "Hoàn thành"

**Bước 13: Tracking realtime (Khách hàng xem)**
- Trong khi shipper đang giao
- Khách hàng mở app và vào "Đơn hàng của tôi"
- Nếu đơn ở trạng thái "Shipping"
- Có nút "Xem vị trí shipper"
- Mở bản đồ realtime:
  - 📍 Vị trí shipper (cập nhật mỗi 5 giây)
  - 🏠 Địa chỉ nhận hàng
  - Đường đi ước tính
  - ETA: "Đến nơi sau 5 phút"

**Bước 14: Xử lý khi không thể giao**
- Nếu khách không nhận máy hoặc không có nhà
- Shipper nhấn "Không thể giao"
- Dialog:
  - "Lý do không thể giao:"
  - ⭕ Khách không nhận máy
  - ⭕ Khách không có nhà
  - ⭕ Địa chỉ sai
  - ⭕ Khách hủy đơn
  - Ô nhập ghi chú chi tiết
- Shipper chọn lý do và xác nhận
- Đơn đánh dấu "Giao thất bại"
- Thông báo cho shop và khách

### Screenshot cần chụp
- [ ] `GpsScreen`: Chọn đơn để tạo chuyến
- [ ] Loading "Đang tính toán lộ trình tối ưu..."
- [ ] `DeliveryMapScreen`: Bản đồ với 3 điểm dừng + lộ trình
- [ ] Bottom sheet thông tin chuyến đi
- [ ] `DeliveryMapScreen`: Navigation đến điểm 1
- [ ] Card điểm đến với thông tin khách hàng
- [ ] Turn-by-turn guidance (icon mũi tên + khoảng cách)
- [ ] Dialog "Bạn đã đến điểm giao hàng!"
- [ ] Dialog xác nhận giao hàng với checkbox COD
- [ ] Toast "Đã giao đơn 1/3"
- [ ] Animation hoàn thành chuyến đi
- [ ] Tracking realtime (góc nhìn khách hàng)
- [ ] Dialog "Không thể giao" với lý do

---

## 5. HOÀN THÀNH GIAO HÀNG

### Mô tả ngắn
Chức năng hoàn thành giao hàng cho phép shipper đánh dấu từng đơn hàng là đã giao thành công, xác nhận thu tiền COD, và hoàn tất toàn bộ chuyến đi. Sau khi hoàn thành, trạng thái đơn hàng tự động cập nhật thành "Delivered" và shipper có thể nhận đơn mới.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper đang ở điểm giao hàng**
- Shipper đã di chuyển đến địa chỉ khách hàng
- Màn hình `DeliveryMapScreen` hiển thị:
  - Vị trí shipper trùng với điểm giao hàng
  - Card điểm đến:
    - ORDER-12345
    - Khách: Nguyễn Văn A
    - Địa chỉ: 789 Nguyễn Thị Minh Khai, Q.3
    - 💰 COD: 150,000đ
  - Nút "Giao hàng" (màu xanh lớn)

**Bước 2: Gặp khách hàng**
- Shipper gọi chuông/gọi điện
- Khách hàng ra nhận hàng
- Shipper đưa đồ ăn và kiểm tra:
  - "Đây là đơn của anh Nguyễn Văn A phải không ạ?"
  - "2 phần phở bò và 1 trà đá"
  - Khách xác nhận đúng

**Bước 3: Thu tiền COD**
- Đơn có phương thức thanh toán: COD
- Shipper nói: "Tổng cộng 150,000đ ạ"
- Khách đưa tiền 200,000đ
- Shipper trả lại 50,000đ
- Khách nhận tiền thừa

**Bước 4: Xác nhận giao hàng trên app**
- Shipper nhấn nút "Giao hàng"
- Dialog hiển thị:
  - **"Xác nhận đã giao ORDER-12345?"**
  - **Thông tin khách**: Nguyễn Văn A
  - **COD**: 150,000đ
  - Checkbox: ☑️ "Đã thu tiền COD"
  - Checkbox: ☐ "Khách yêu cầu hoàn tiền" (optional)
  - **Ô nhập ghi chú** (optional):
    - "Khách hàng hài lòng"
    - "Đã giao đúng giờ"
  - Nút "Hủy" và "Xác nhận"

**Bước 5: Shipper xác nhận**
- Shipper check vào "Đã thu tiền COD"
- Nhập ghi chú: "Khách hàng hài lòng"
- Nhấn nút "Xác nhận"

**Bước 6: Hệ thống xử lý**
- Loading spinner ngắn
- Backend:
  - Cập nhật trạng thái đơn: SHIPPING → DELIVERED
  - Lưu thời gian giao hàng
  - Lưu ghi chú của shipper
  - Tính khoảng cách thực tế đã di chuyển
  - Cập nhật số dư COD của shipper (+150,000đ)
  - Gửi thông báo cho khách: "Đơn hàng đã được giao thành công"
  - Gửi thông báo cho shop: "Shipper đã giao xong ORDER-12345"

**Bước 7: Toast và cập nhật UI**
- Toast hiển thị: "✅ Đã giao đơn thành công"
- Nếu đây là điểm cuối cùng trong chuyến:
  - Điểm trên bản đồ đổi màu xanh (completed)
  - Counter cập nhật: "3/3 đơn đã giao"
  - Bottom sheet hiển thị "Hoàn thành chuyến"
- Nếu còn điểm khác:
  - Tự động chuyển sang điểm tiếp theo (xem feature #4)

**Bước 8: Màn hình hoàn thành chuyến**
- Sau khi giao xong tất cả đơn trong chuyến
- Màn hình hiển thị animation celebration:
  - Icon trophy hoặc confetti
  - "🎉 Chúc mừng!"
  - "Bạn đã hoàn thành chuyến đi"
- Thống kê chuyến:
  - 📦 **Đơn đã giao**: 3/3
  - ⏱️ **Thời gian**: 28 phút
  - 📍 **Quãng đường**: 6.3 km
  - 💰 **Tổng thu COD**: 420,000đ
  - 🚀 **Tốc độ trung bình**: 13.5 km/h
  - ⭐ **Hiệu suất**: Xuất sắc (giao đúng giờ)

**Bước 9: Xem chi tiết chuyến**
- Shipper nhấn "Xem chi tiết"
- Màn hình chuyển đến `TripDetailScreen`
- Hiển thị timeline:
  - ✅ Nhận chuyến - 09:30 AM
  - ✅ Điểm 1 (ORDER-12346) - 09:38 AM (8 phút)
  - ✅ Điểm 2 (ORDER-12347) - 09:45 AM (7 phút)
  - ✅ Điểm 3 (ORDER-12345) - 09:58 AM (13 phút)
  - ✅ Hoàn thành - 09:58 AM
- Bản đồ hiển thị lộ trình đã đi (đường màu xanh)

**Bước 10: Hoàn tất và quay về**
- Shipper nhấn "Hoàn thành"
- Quay về màn hình `ShipperHomeScreen`
- Section "Đơn đang giao" giảm từ 3 → 0
- Section "Hoàn thành hôm nay" tăng từ 0 → 3
- Badge thu nhập cập nhật: "Thu nhập hôm nay: +120,000đ" (phí ship)

**Bước 11: Xử lý đơn thanh toán online (không COD)**
- Nếu đơn đã thanh toán online (SePay)
- Dialog xác nhận giao hàng **không có** checkbox "Đã thu tiền COD"
- Chỉ cần xác nhận đã giao
- Shipper không cần thu tiền

**Bước 12: Giao hàng một phần**
- Nếu shipper chỉ có 1 đơn (không tạo chuyến)
- Từ `ShipperOrderDetailScreen`, nhấn "Giao hàng"
- Dialog tương tự bước 4
- Sau khi xác nhận:
  - Đơn đổi trạng thái → DELIVERED
  - Quay về `ShipperHomeScreen`
  - Đơn biến mất khỏi "Đơn đang giao"

**Bước 13: Yêu cầu đánh giá từ khách hàng**
- Sau khi đơn hoàn thành
- Khách hàng nhận thông báo: "Đánh giá trải nghiệm giao hàng"
- Khách có thể đánh giá:
  - Shipper: 1-5 sao
  - Tốc độ giao hàng
  - Thái độ phục vụ
  - Ghi chú
- Đánh giá này ảnh hưởng đến rating của shipper

**Bước 14: Xem rating cá nhân**
- Shipper vào "Hồ sơ" → "Đánh giá của tôi"
- Hiển thị:
  - ⭐ **Rating trung bình**: 4.8/5 (145 đánh giá)
  - 📊 Phân bố sao:
    - 5⭐: 120 người (82.7%)
    - 4⭐: 20 người (13.8%)
    - 3⭐: 3 người (2.1%)
    - 2⭐: 1 người (0.7%)
    - 1⭐: 1 người (0.7%)
  - Nhận xét gần đây từ khách hàng

### Screenshot cần chụp
- [ ] `DeliveryMapScreen`: Vị trí shipper tại điểm giao hàng
- [ ] Card điểm đến với nút "Giao hàng"
- [ ] Dialog xác nhận giao hàng với checkbox COD
- [ ] Dialog có ô nhập ghi chú
- [ ] Toast "Đã giao đơn thành công"
- [ ] Animation celebration hoàn thành chuyến
- [ ] Thống kê chuyến đi (thời gian, quãng đường, COD)
- [ ] `TripDetailScreen`: Timeline các điểm đã giao
- [ ] `ShipperHomeScreen`: Section "Hoàn thành hôm nay" cập nhật
- [ ] Dialog giao hàng đơn đã thanh toán online (không COD)
- [ ] Màn hình "Đánh giá của tôi" với rating

---

## 6. LỊCH SỬ CHUYẾN GIAO HÀNG

### Mô tả ngắn
Chức năng lịch sử chuyến giao hàng cho phép shipper xem lại tất cả các chuyến đi đã hoàn thành, xem chi tiết từng chuyến (lộ trình, thời gian, khoảng cách, thu nhập), và thống kê tổng số đơn đã giao. Đây là công cụ giúp shipper theo dõi hiệu suất làm việc của mình.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper mở màn hình lịch sử**
- Từ menu chính, shipper nhấn vào "Lịch sử giao hàng"
- Hệ thống hiển thị màn hình `TripHistoryScreen`
- Danh sách tất cả chuyến đi đã hoàn thành

**Bước 2: Xem tổng quan**
- Phía trên có 4 cards thống kê:
  - 🚀 **Tổng chuyến**: 45 chuyến
  - 📦 **Tổng đơn**: 128 đơn
  - 📍 **Tổng quãng đường**: 342 km
  - 💰 **Tổng thu nhập**: 3,840,000đ

**Bước 3: Lọc theo thời gian**
- Dưới cards có tabs:
  - 📅 **Hôm nay** (3 chuyến)
  - 📊 **Tuần này** (18 chuyến)
  - 📈 **Tháng này** (45 chuyến)
  - 🗓️ **Tất cả**
- Mặc định chọn "Hôm nay"

**Bước 4: Xem danh sách chuyến hôm nay**
- Hiển thị 3 chuyến đã giao trong ngày:
  - **Chuyến #003**
    - ⏰ 15:30 - 16:15 (45 phút)
    - 📦 3 đơn
    - 📍 8.2 km
    - 💰 240,000đ
    - Status: ✅ Hoàn thành
  - **Chuyến #002**
    - ⏰ 11:45 - 12:30 (45 phút)
    - 📦 4 đơn
    - 📍 6.5 km
    - 💰 320,000đ
    - Status: ✅ Hoàn thành
  - **Chuyến #001**
    - ⏰ 09:30 - 09:58 (28 phút)
    - 📦 3 đơn
    - 📍 6.3 km
    - 💰 240,000đ
    - Status: ✅ Hoàn thành

**Bước 5: Xem chi tiết một chuyến**
- Shipper nhấn vào "Chuyến #003"
- Hệ thống mở màn hình `TripDetailScreen`
- Thông tin tổng quan:
  - 🚀 **Chuyến #003**
  - 📅 **Ngày**: 01/02/2026
  - ⏰ **Thời gian**: 15:30 - 16:15 (45 phút)
  - 📍 **Quãng đường**: 8.2 km
  - 🚗 **Tốc độ TB**: 10.9 km/h
  - 💰 **Thu nhập**: 240,000đ

**Bước 6: Xem bản đồ lộ trình**
- Dưới thông tin có bản đồ hiển thị:
  - 📍 Điểm xuất phát (vị trí shipper lúc bắt đầu)
  - 🏠 Điểm 1, 2, 3 (các địa điểm đã giao)
  - Đường đi màu xanh nối các điểm (đã đi)
  - Có thể zoom in/out để xem chi tiết

**Bước 7: Xem timeline chi tiết**
- Cuộn xuống phần "Chi tiết giao hàng"
- Timeline hiển thị từng bước:
  - ✅ **Bắt đầu chuyến** - 15:30 PM
    - Vị trí: 456 Lê Lợi, Q.1 (shop)
  - ✅ **Điểm 1** - 15:38 PM (8 phút)
    - ORDER-12348
    - Khách: Lê Thị D
    - Địa chỉ: 123 Hai Bà Trưng, Q.3
    - COD: 80,000đ
    - Ghi chú: "Giao nhanh, khách hài lòng"
  - ✅ **Điểm 2** - 15:50 PM (12 phút)
    - ORDER-12349
    - Khách: Phạm Văn E
    - Địa chỉ: 789 Điện Biên Phủ, Q.3
    - COD: 100,000đ
  - ✅ **Điểm 3** - 16:08 PM (18 phút)
    - ORDER-12350
    - Khách: Hoàng Thị F
    - Địa chỉ: 456 Võ Văn Tần, Q.3
    - COD: 60,000đ
  - ✅ **Hoàn thành chuyến** - 16:15 PM

**Bước 8: Xem thống kê hiệu suất**
- Phần "Hiệu suất" hiển thị:
  - ⏱️ **Thời gian giao TB/đơn**: 15 phút
  - 📍 **Quãng đường TB/đơn**: 2.7 km
  - 💰 **Thu nhập TB/đơn**: 80,000đ
  - ⭐ **Đánh giá**: 4.9/5 (3 đánh giá)
  - 🎯 **Giao đúng giờ**: 100% (3/3 đơn)
  - Badge: "🏆 Xuất sắc"

**Bước 9: Xem đánh giá của khách**
- Cuộn xuống phần "Đánh giá từ khách hàng"
- Hiển thị 3 đánh giá:
  - **Lê Thị D** - 5⭐
    - "Giao hàng nhanh, thái độ tốt"
  - **Phạm Văn E** - 5⭐
    - "Đúng giờ, chu đáo"
  - **Hoàng Thị F** - 4⭐
    - "Tốt, nhưng giao hơi trễ"

**Bước 10: Xuất báo cáo**
- Shipper nhấn icon "..." ở góc trên phải
- Menu hiển thị:
  - 📊 Xuất báo cáo PDF
  - 📤 Chia sẻ
  - 🖨️ In báo cáo
- Shipper chọn "Xuất báo cáo PDF"
- Hệ thống tạo file PDF với:
  - Bản đồ lộ trình
  - Timeline chi tiết
  - Thống kê hiệu suất
- File lưu vào Downloads

**Bước 11: Tìm kiếm chuyến cũ**
- Quay về `TripHistoryScreen`
- Shipper nhấn vào icon tìm kiếm
- Có thể tìm theo:
  - Mã chuyến: #003
  - Ngày: 01/02/2026
  - Tên khách hàng
  - Mã đơn hàng
- Nhập "ORDER-12348"
- Hiển thị chuyến #003 chứa đơn này

**Bước 12: Sắp xếp danh sách**
- Nhấn icon sắp xếp
- Bottom sheet hiển thị:
  - ⏰ Thời gian (mới → cũ)
  - ⏰ Thời gian (cũ → mới)
  - 💰 Thu nhập (cao → thấp)
  - 📍 Quãng đường (xa → gần)
  - ⭐ Đánh giá (cao → thấp)
- Shipper chọn "Thu nhập (cao → thấp)"
- Danh sách sắp xếp lại

**Bước 13: Xem biểu đồ thống kê**
- Chuyển sang tab "Tuần này"
- Nhấn vào icon biểu đồ
- Hiển thị charts:
  - **Biểu đồ cột**: Số đơn giao mỗi ngày
    - T2: 12 đơn
    - T3: 15 đơn
    - T4: 18 đơn
    - ...
  - **Biểu đồ đường**: Thu nhập theo ngày
  - **Pie chart**: Phân bố thời gian giao hàng
    - Sáng: 35%
    - Trưa: 40%
    - Chiều: 25%

**Bước 14: So sánh với kỳ trước**
- Phần "So sánh" hiển thị:
  - 📈 **Tuần này vs tuần trước**:
    - Số đơn: 68 (+12 đơn, +21.4%)
    - Thu nhập: 1,920,000đ (+240,000đ, +14.3%)
    - Quãng đường: 156 km (+18 km, +13.0%)
  - Mũi tên xanh ↗ (tăng) hoặc đỏ ↘ (giảm)
  - Badge: "Cải thiện tốt!" nếu tăng

### Screenshot cần chụp
- [ ] `TripHistoryScreen`: 4 cards thống kê tổng quan
- [ ] `TripHistoryScreen`: 4 tabs lọc theo thời gian
- [ ] `TripHistoryScreen`: Danh sách chuyến hôm nay
- [ ] `TripDetailScreen`: Thông tin tổng quan chuyến
- [ ] `TripDetailScreen`: Bản đồ lộ trình đã đi
- [ ] `TripDetailScreen`: Timeline chi tiết từng điểm
- [ ] `TripDetailScreen`: Thống kê hiệu suất với badge
- [ ] `TripDetailScreen`: Đánh giá từ khách hàng
- [ ] Ô tìm kiếm chuyến
- [ ] Bottom sheet sắp xếp
- [ ] Biểu đồ thống kê (cột, đường, pie)
- [ ] So sánh với kỳ trước

---

## 7. QUẢN LÝ WALLET VÀ YÊU CẦU RÚT TIỀN (PAYOUT)

### Mô tả ngắn
Chức năng quản lý ví điện tử cho phép shipper xem số dư, lịch sử giao dịch, thống kê thu nhập theo thời gian, và yêu cầu rút tiền về tài khoản ngân hàng. Shipper có thể theo dõi trạng thái yêu cầu rút tiền và nhận thông báo khi được duyệt hoặc từ chối.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper mở màn hình Thu nhập**
- Từ bottom navigation, shipper nhấn vào tab "Thu nhập" (icon ví)
- Hệ thống hiển thị màn hình `EarningsScreen`
- Màn hình header: "Thu nhập của tôi"

**Bước 2: Xem tổng quan ví**
- Card đầu tiên hiển thị thông tin ví:
  - **💰 Số dư khả dụng**: 3,200,000đ
    - Font size lớn, màu xanh đậm
  - **📈 Tổng thu nhập**: 15,800,000đ
    - Badge: +1,240,000đ tuần này
  - **💸 Tổng đã rút**: 12,600,000đ
    - (Số lần rút: 8 lần)
  - Nút "Rút tiền" (màu xanh, nổi bật)

**Bước 3: Xem thống kê thu nhập**
- Card thứ hai: "Thu nhập theo thời gian"
- Có 3 tabs:
  - **Hôm nay**: 240,000đ (3 chuyến)
  - **Tuần này**: 1,680,000đ (18 chuyến)
  - **Tháng này**: 6,420,000đ (75 chuyến)
- Biểu đồ cột hiển thị thu nhập 7 ngày gần nhất
- Trục Y: Thu nhập (nghìn đồng)
- Trục X: Ngày trong tuần

**Bước 4: Xem lịch sử giao dịch**
- Cuộn xuống phần "Lịch sử giao dịch"
- Danh sách các transaction:
  - **EARNING - 240,000đ** (màu xanh ↗)
    - Chuyến #003 - 3 đơn
    - 01/02/2026 16:15 PM
  - **EARNING - 180,000đ**
    - Chuyến #002 - 2 đơn
    - 01/02/2026 14:30 PM
  - **PAYOUT - 2,000,000đ** (màu đỏ ↘)
    - Rút về MB Bank ...6789
    - 28/01/2026 10:00 AM
    - Status: ✅ TRANSFERRED
  - ... (pagination, hiển thị 20 items/page)

**Bước 5: Lọc lịch sử giao dịch**
- Nhấn icon filter
- Bottom sheet hiển thị:
  - **Loại giao dịch**:
    - ☑️ Thu nhập (EARNING)
    - ☑️ Rút tiền (PAYOUT)
  - **Khoảng thời gian**:
    - Hôm nay / Tuần này / Tháng này / Tùy chọn
  - Nút "Áp dụng"
- Shipper chọn chỉ xem "Thu nhập"
- Danh sách chỉ hiển thị EARNING transactions

**Bước 6: Mở dialog yêu cầu rút tiền**
- Shipper nhấn nút "Rút tiền" trên card tổng quan
- Dialog `PayoutDialog` hiển thị:
  - **Tiêu đề**: "Yêu cầu rút tiền"
  - **Số dư khả dụng**: 3,200,000đ
  - **Số tiền rút**: Ô nhập số
    - Placeholder: "Nhập số tiền (tối thiểu 100,000đ)"
    - Hiển thị đồng bộ: "Còn lại: X đ"
  - **Thông tin ngân hàng**:
    - Ngân hàng: Dropdown (MB Bank, VCB, TCB...)
    - Số tài khoản: Ô nhập số
    - Tên chủ tài khoản: Ô nhập text (viết hoa)
  - **Ghi chú**: Ô nhập (optional)
  - Nút "Hủy" và "Xác nhận" (disabled cho đến khi điền đủ)

**Bước 7: Điền thông tin rút tiền**
- Shipper nhập:
  - Số tiền: 2,500,000đ
    - Validation: Không được > số dư
    - Còn lại: 700,000đ (màu xanh)
  - Ngân hàng: MB Bank (970422)
  - Số tài khoản: 0123456789
  - Tên chủ TK: TRAN VAN B
  - Ghi chú: "Rút tiền cuối tuần"
- Nút "Xác nhận" được enable

**Bước 8: Xác nhận yêu cầu**
- Shipper nhấn "Xác nhận"
- Dialog xác nhận cuối cùng:
  - ⚠️ "Xác nhận rút tiền?"
  - Số tiền: **2,500,000đ**
  - Ngân hàng: MB Bank
  - Số TK: 0123456789
  - "Yêu cầu sẽ được xử lý trong 1-3 ngày làm việc"
  - Checkbox: ☑️ "Tôi xác nhận thông tin chính xác"
  - Nút "Hủy" và "Gửi yêu cầu"
- Shipper check và nhấn "Gửi yêu cầu"

**Bước 9: Hệ thống xử lý**
- Loading spinner hiển thị
- Backend:
  - Validate số dư đủ
  - Tạo payout request với status: PENDING
  - Trừ tạm số dư (reserved balance)
  - Gửi notification cho admin
  - Gửi confirmation cho shipper
- Sau 1-2 giây:
  - Toast: "Đã gửi yêu cầu rút tiền thành công"
  - Dialog đóng
  - Card tổng quan cập nhật:
    - Số dư: 3,200,000đ → 700,000đ (vàng, pending)
    - Badge: "🟡 1 yêu cầu đang chờ duyệt"

**Bước 10: Xem trạng thái payout**
- Cuộn xuống lịch sử giao dịch
- Transaction mới xuất hiện ở đầu:
  - **PAYOUT - 2,500,000đ** ↘
    - Rút về MB Bank ...6789
    - 01/02/2026 17:30 PM
    - Status: 🟡 **PENDING** (chờ duyệt)
- Shipper có thể nhấn vào để xem chi tiết

**Bước 11: Xem chi tiết payout request**
- Shipper nhấn vào payout PENDING
- Modal hiển thị:
  - **Payout Details**:
    - ID: PAYOUT-2026020100015
    - Amount: 2,500,000đ
    - Status: 🟡 PENDING
    - Requested At: 01/02/2026 17:30 PM
  - **Bank Info**:
    - Bank: MB Bank (970422)
    - Account: 0123456789
    - Name: TRAN VAN B
  - **Note**: "Rút tiền cuối tuần"
  - **Timeline**:
    - ✅ Yêu cầu đã gửi - 01/02 17:30
    - ⏳ Đang chờ admin duyệt
    - ⏹️ Chuyển khoản
    - ⏹️ Hoàn thành
  - Nút "Đóng" (không thể cancel khi PENDING)

**Bước 12: Nhận thông báo khi được duyệt**
- Admin duyệt yêu cầu
- Push notification: "Yêu cầu rút tiền 2,500,000đ đã được duyệt"
- Shipper mở app
- Status cập nhật: PENDING → 🟢 APPROVED

**Bước 13: Nhận thông báo đã chuyển khoản**
- Admin xác nhận đã chuyển tiền
- Push notification: "Tiền đã được chuyển vào tài khoản của bạn"
- Shipper mở app
- Status cập nhật: APPROVED → 💚 TRANSFERRED
- Transaction timeline:
  - ✅ Yêu cầu đã gửi - 01/02 17:30
  - ✅ Đã duyệt - 02/02 09:15
  - ✅ Đã chuyển khoản - 02/02 10:30
  - ✅ Hoàn thành
- Card tổng quan cập nhật:
  - Badge "đang chờ duyệt" biến mất
  - Tổng đã rút: 12,600,000đ → 15,100,000đ

**Bước 14: Trường hợp bị từ chối**
- Nếu admin từ chối yêu cầu
- Push notification: "Yêu cầu rút tiền bị từ chối: Số tài khoản không hợp lệ"
- Shipper mở app
- Status: ❌ REJECTED
- Lý do từ chối hiển thị rõ ràng
- Số dư được hoàn lại: 700,000đ → 3,200,000đ
- Shipper có thể gửi lại yêu cầu mới với thông tin đúng

**Bước 15: Xem tổng hợp thu nhập tháng**
- Nhấn icon "📊 Báo cáo" ở góc trên phải
- Modal hiển thị:
  - **Tháng 01/2026**:
    - Tổng thu nhập: 6,420,000đ
    - Số chuyến: 75
    - TB/chuyến: 85,600đ
    - Đã rút: 2,000,000đ
    - Còn lại: 4,420,000đ
  - Biểu đồ tròn:
    - Đã rút: 31%
    - Còn lại: 69%
  - Nút "Xuất PDF" để lưu báo cáo

### Screenshot cần chụp
- [ ] `EarningsScreen`: Card tổng quan ví với số dư, tổng thu nhập, tổng đã rút
- [ ] `EarningsScreen`: Thống kê thu nhập 3 tabs (Hôm nay, Tuần, Tháng) với biểu đồ
- [ ] `EarningsScreen`: Lịch sử giao dịch với EARNING và PAYOUT transactions
- [ ] Bottom sheet filter giao dịch
- [ ] `PayoutDialog`: Form yêu cầu rút tiền (rỗng)
- [ ] `PayoutDialog`: Form đã điền đầy đủ thông tin (validate pass)
- [ ] Dialog xác nhận cuối cùng trước khi gửi
- [ ] Toast "Đã gửi yêu cầu thành công"
- [ ] Card tổng quan với badge "1 yêu cầu đang chờ"
- [ ] Transaction PAYOUT với status PENDING
- [ ] Modal chi tiết payout với timeline
- [ ] Push notification "Yêu cầu đã được duyệt"
- [ ] Status APPROVED → TRANSFERRED
- [ ] Modal báo cáo thu nhập tháng với biểu đồ

---

## 8. AI CHATBOT HỖ TRỢ

### Mô tả ngắn
Chức năng AI Chatbot giúp shipper giải đáp thắc mắc về quy trình giao hàng, chính sách, vấn đề kỹ thuật, và các tình huống xử lý đặc biệt. Hệ thống sử dụng Gemini API để tạo câu trả lời thông minh và cung cấp quick replies để trả lời nhanh.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper mở Chatbot**
- Từ menu chính, shipper nhấn vào "Hỗ trợ AI" hoặc icon chatbot
- Hệ thống hiển thị màn hình `ShipperChatbotScreen`
- Tin nhắn chào mừng từ bot:
  - "Xin chào! Tôi là trợ lý AI của KTX Delivery dành cho Shipper."
  - "Tôi có thể giúp bạn về quy trình giao hàng, chính sách, và các vấn đề khác."
  - Avatar bot (icon robot)

**Bước 2: Hiển thị Quick Replies cho Shipper**
- Dưới tin nhắn chào mừng, hiển thị các nút quick reply:
  - 📦 "Cách nhận đơn hàng"
  - 🗺️ "Hướng dẫn dùng GPS"
  - 💰 "Chính sách thanh toán COD"
  - ⚠️ "Xử lý khi giao không thành công"
  - 📞 "Liên hệ hỗ trợ"

**Bước 3: Shipper hỏi bằng Quick Reply**
- Shipper nhấn nút "Xử lý khi giao không thành công"
- Text tự động gửi: "Xử lý khi giao không thành công"
- Tin nhắn hiển thị bên phải (màu xanh)

**Bước 4: Bot trả lời**
- Animation "Bot đang nhập..." (3 chấm nhảy)
- Sau 2-3 giây, bot trả lời:
  - "Khi không thể giao hàng thành công, bạn cần làm theo các bước sau:

    **1. Liên hệ khách hàng**
    - Gọi điện 2-3 lần
    - Đợi 5-10 phút nếu khách nói đang bận

    **2. Báo cho shop**
    - Nhấn nút 'Không thể giao' trong app
    - Chọn lý do cụ thể
    - Shop sẽ liên hệ lại khách

    **3. Xử lý đơn hàng**
    - Nếu khách không nhận: Mang hàng về shop
    - Nếu địa chỉ sai: Yêu cầu khách gửi địa chỉ đúng
    - Nếu khách hủy: Cập nhật trạng thái 'Đã hủy'

    **4. Bồi thường**
    - Nếu do lỗi shipper: Có thể bị trừ phí ship
    - Nếu do lỗi khách: Không bị trừ

    Bạn có cần hỗ trợ thêm không?"
  - Tin nhắn căn trái (màu xám nhạt)
  - Badge: "Độ tin cậy: 98%" (màu xanh)

**Bước 5: Bot gợi ý câu hỏi liên quan**
- Sau câu trả lời, bot hiển thị quick replies:
  - "Chính sách bồi thường chi tiết"
  - "Cách liên hệ khách hàng"
  - "Hỏi vấn đề khác"

**Bước 6: Shipper hỏi bằng text tự do**
- Shipper gõ: "Tôi có thể nhận bao nhiêu đơn cùng lúc?"
- Nhấn gửi
- Tin nhắn hiển thị bên phải

**Bước 7: Bot trả lời câu hỏi tự do**
- Animation "Bot đang nhập..."
- Bot trả lời:
  - "Số lượng đơn bạn có thể nhận cùng lúc phụ thuộc vào:

    📦 **Giới hạn hệ thống**: Tối đa 10 đơn
    📍 **Khoảng cách**: Nên chọn đơn trong cùng khu vực
    ⏰ **Thời gian**: Ước tính thời gian giao hợp lý

    🎯 **Khuyến nghị**:
    - Shipper mới: 2-3 đơn/chuyến
    - Shipper có kinh nghiệm: 4-6 đơn/chuyến
    - Khu vực gần nhau: 6-8 đơn/chuyến

    💡 **Mẹo**: Dùng tính năng 'Tối ưu lộ trình' để giao nhiều đơn hiệu quả hơn!

    Bạn cần biết thêm về tối ưu lộ trình không?"
  - Badge: "Độ tin cậy: 95%"

**Bước 8: Hỏi về vấn đề kỹ thuật**
- Shipper gõ: "GPS không hoạt động, làm sao?"
- Bot trả lời:
  - "Nếu GPS không hoạt động, bạn thử các cách sau:

    1️⃣ **Kiểm tra cài đặt**:
    - Bật GPS/Location trong Settings điện thoại
    - Cấp quyền Location cho app KTX Delivery
    - Settings → Apps → KTX Delivery → Permissions → Location → Allow

    2️⃣ **Khởi động lại**:
    - Tắt và bật lại GPS
    - Force stop app và mở lại
    - Khởi động lại điện thoại

    3️⃣ **Kiểm tra kết nối**:
    - Bật dữ liệu di động hoặc WiFi
    - GPS cần internet để hoạt động tốt

    4️⃣ **Cập nhật app**:
    - Vào Google Play Store
    - Kiểm tra bản cập nhật mới

    ⚠️ **Nếu vẫn không được**:
    - Liên hệ: 1900-xxxx
    - Hoặc chat với bộ phận kỹ thuật

    Vấn đề đã được giải quyết chưa?"
  - Badge: "Độ tin cậy: 92%"

**Bước 9: Bot không chắc chắn**
- Shipper hỏi: "Mức lương tháng này là bao nhiêu?"
- Bot trả lời:
  - "⚠️ Xin lỗi, tôi không có quyền truy cập thông tin lương của bạn.

    Để xem thông tin lương:
    - Vào menu → 'Thu nhập'
    - Chọn 'Lịch sử thanh toán'

    Hoặc liên hệ bộ phận tài chính:
    - Hotline: 1900-xxxx
    - Email: finance@ktxdelivery.com

    Tôi có thể giúp bạn về các vấn đề khác không?"
  - Badge: "Độ tin cậy: 45%" (màu vàng/đỏ)

**Bước 10: Chuyển sang hỗ trợ con người**
- Nếu bot không giải quyết được
- Hiển thị nút "💬 Chat với nhân viên hỗ trợ"
- Shipper nhấn vào
- Chuyển sang màn hình chat realtime với support team
- Lịch sử chat với bot được đính kèm

**Bước 11: Lịch sử chat với bot**
- Shipper có thể xem lại lịch sử chat
- Mỗi lần mở chatbot, lịch sử 10 tin nhắn gần nhất được hiển thị
- Có nút "Xóa lịch sử" để bắt đầu cuộc trò chuyện mới

**Bước 12: Câu hỏi thường gặp (FAQ)**
- Nhấn icon "❓" ở góc trên
- Hiển thị danh sách FAQ:
  - Cách nhận đơn hàng
  - Chính sách COD
  - Xử lý giao thất bại
  - Tính năng GPS
  - Quy định phạt
  - Liên hệ hỗ trợ
- Nhấn vào FAQ → Bot tự động trả lời

### Screenshot cần chụp
- [ ] `ShipperChatbotScreen`: Tin nhắn chào mừng với quick replies
- [ ] `ShipperChatbotScreen`: Bot trả lời về xử lý giao thất bại
- [ ] `ShipperChatbotScreen`: Bot trả lời về số đơn cùng lúc
- [ ] `ShipperChatbotScreen`: Bot trả lời về vấn đề GPS
- [ ] `ShipperChatbotScreen`: Bot không chắc chắn (độ tin cậy thấp)
- [ ] Nút "Chat với nhân viên hỗ trợ"
- [ ] Danh sách FAQ

---

## 9. YÊU CẦU RỜI SHOP

### Mô tả ngắn
Chức năng yêu cầu rời shop cho phép shipper gửi đơn xin nghỉ việc hoặc chuyển sang shop khác. Shipper cần ghi rõ lý do, sau đó chờ chủ shop xem xét và phê duyệt. Khi được chấp thuận, shipper sẽ bị xóa khỏi danh sách shipper của shop.

### Kịch bản sử dụng (Case Study)

**Bước 1: Shipper quyết định rời shop**
- Shipper đang làm việc cho shop "Phở Hà Nội"
- Vì lý do cá nhân (chuyển khu vực, bận việc riêng...), muốn nghỉ việc
- Vào menu chính, nhấn "Cài đặt" hoặc "Hồ sơ"

**Bước 2: Mở màn hình yêu cầu rời shop**
- Trong Settings, có section "Quản lý công việc"
- Nhấn vào "Yêu cầu rời shop"
- Hệ thống hiển thị màn hình `RemovalRequestScreen`
- Warning banner màu vàng:
  - ⚠️ "Lưu ý: Sau khi rời shop, bạn sẽ không thể nhận đơn hàng từ shop này nữa."

**Bước 3: Điền form yêu cầu**
- Form hiển thị:
  - **Shop hiện tại**: Phở Hà Nội (không thể thay đổi)
  - **Thời gian làm việc**: 3 tháng 15 ngày
  - **Tổng đơn đã giao**: 245 đơn
  - **Lý do rời shop**: Dropdown
    - ⭕ Chuyển sang khu vực khác
    - ⭕ Bận việc riêng
    - ⭕ Không hài lòng với shop
    - ⭕ Tìm được công việc khác
    - ⭕ Lý do cá nhân
    - ⭕ Lý do khác
  - **Chi tiết lý do**: Ô nhập text (bắt buộc)
  - **Ngày dự định nghỉ**: Date picker

**Bước 4: Shipper điền thông tin**
- Chọn lý do: "Chuyển sang khu vực khác"
- Nhập chi tiết:
  - "Em xin phép nghỉ việc vì gia đình chuyển về Quận 7, khoảng cách đi lại quá xa. Em rất biết ơn shop đã tạo cơ hội cho em trong 3 tháng qua. Hy vọng shop tìm được shipper mới phù hợp."
- Chọn ngày: 15/02/2026 (2 tuần sau)
- Checkbox: ☑️ "Tôi cam kết hoàn thành tất cả đơn hàng đã nhận trước khi nghỉ"

**Bước 5: Xác nhận gửi yêu cầu**
- Nhấn nút "Gửi yêu cầu"
- Dialog xác nhận hiển thị:
  - "Xác nhận gửi yêu cầu rời shop?"
  - "Shop sẽ xem xét yêu cầu của bạn trong vòng 24-48 giờ."
  - "Trong thời gian chờ, bạn vẫn có thể tiếp tục nhận và giao đơn hàng."
  - Nút "Hủy" và "Xác nhận"
- Shipper nhấn "Xác nhận"

**Bước 6: Hệ thống xử lý**
- Loading spinner
- Backend:
  - Tạo bản ghi removal request với trạng thái "Pending"
  - Lưu lý do và chi tiết
  - Gửi thông báo cho chủ shop:
    - "Shipper Trần Văn B đã gửi yêu cầu rời shop"
    - "Lý do: Chuyển sang khu vực khác"
  - Lưu timestamp

**Bước 7: Màn hình thành công**
- Hiển thị:
  - Icon tick xanh
  - "Yêu cầu đã được gửi!"
  - "Shop Phở Hà Nội sẽ xem xét yêu cầu của bạn."
  - "Chúng tôi sẽ thông báo kết quả qua app."
  - Nút "Về trang chính"

**Bước 8: Trạng thái chờ duyệt**
- Quay về màn hình chính
- Banner màu vàng hiển thị ở trên cùng:
  - "⏳ Yêu cầu rời shop đang chờ xét duyệt"
  - "Ngày gửi: 01/02/2026"
  - Nút "Hủy yêu cầu"
- Shipper vẫn có thể nhận và giao đơn bình thường

**Bước 9: Xem chi tiết yêu cầu**
- Nhấn vào banner
- Hiển thị bottom sheet:
  - **Trạng thái**: Chờ xét duyệt
  - **Ngày gửi**: 01/02/2026
  - **Ngày dự định nghỉ**: 15/02/2026
  - **Lý do**: Chuyển sang khu vực khác
  - **Chi tiết**: (hiển thị đầy đủ)
  - **Đơn đã giao**: 245 đơn
  - **Thời gian làm việc**: 3 tháng 15 ngày
  - Nút "Hủy yêu cầu"

**Bước 10: Hủy yêu cầu (nếu đổi ý)**
- Shipper đổi ý, không muốn rời nữa
- Nhấn "Hủy yêu cầu"
- Dialog xác nhận:
  - "Hủy yêu cầu rời shop?"
  - "Yêu cầu sẽ bị xóa và không thể khôi phục."
- Shipper xác nhận
- Yêu cầu bị xóa
- Banner biến mất
- Toast: "Đã hủy yêu cầu"

**Bước 11: Chủ shop chấp thuận**
- Chủ shop xem xét và chấp thuận yêu cầu
- Push notification đến shipper:
  - "Yêu cầu rời shop đã được chấp thuận"
  - "Bạn có thể tiếp tục làm việc đến 15/02/2026"
- Banner đổi màu xanh:
  - "✅ Yêu cầu rời shop đã được chấp thuận"
  - "Ngày nghỉ việc: 15/02/2026"

**Bước 12: Đến ngày nghỉ việc**
- Vào ngày 15/02/2026
- Hệ thống tự động:
  - Xóa shipper khỏi danh sách shop
  - Unsubscribe khỏi notification topics
  - Chuyển trạng thái → "Inactive"
  - Lưu lý do nghỉ việc
- Shipper không còn nhận được đơn từ shop này
- Có thể đăng ký lại làm shipper cho shop khác

**Bước 13: Chủ shop từ chối**
- Nếu chủ shop từ chối (ví dụ: đang thiếu shipper)
- Push notification:
  - "Yêu cầu rời shop bị từ chối"
  - "Lý do: Hiện shop đang thiếu shipper. Vui lòng làm việc thêm 1 tháng."
- Banner đổi màu đỏ:
  - "❌ Yêu cầu rời shop bị từ chối"
  - "Lý do từ chủ shop: ..."
- Shipper có thể:
  - Gửi yêu cầu mới với thời gian khác
  - Hoặc liên hệ trực tiếp với shop

**Bước 14: Xem lịch sử yêu cầu**
- Vào Settings → "Lịch sử yêu cầu rời shop"
- Hiển thị tất cả yêu cầu đã gửi:
  - **Yêu cầu #002** - 01/02/2026
    - Trạng thái: ✅ Đã chấp thuận
    - Lý do: Chuyển khu vực
  - **Yêu cầu #001** - 15/01/2026
    - Trạng thái: ❌ Bị từ chối
    - Lý do: Bận việc riêng

### Screenshot cần chụp
- [ ] `RemovalRequestScreen`: Warning banner
- [ ] `RemovalRequestScreen`: Form yêu cầu với dropdown lý do
- [ ] `RemovalRequestScreen`: Ô nhập chi tiết lý do
- [ ] Dialog xác nhận gửi yêu cầu
- [ ] Màn hình "Yêu cầu đã được gửi"
- [ ] Banner "Chờ xét duyệt" trên màn hình chính
- [ ] Bottom sheet chi tiết yêu cầu
- [ ] Dialog hủy yêu cầu
- [ ] Push notification "Đã được chấp thuận"
- [ ] Banner "Đã được chấp thuận" (màu xanh)
- [ ] Push notification "Bị từ chối"
- [ ] Lịch sử các yêu cầu rời shop

---

## GHI CHÚ CHO BÁO CÁO

### Các điểm kỹ thuật nổi bật cần nhấn mạnh:

1. **Quy trình đăng ký shipper**: Xác minh danh tính với CCCD và bằng lái xe, camera với overlay định vị, đảm bảo an toàn cho cả hai bên.

2. **Toggle online/offline**: Subscribe/unsubscribe topic notification realtime qua FCM, giúp shipper kiểm soát khả năng nhận đơn.

3. **Google Routes API**: Tối ưu lộ trình giao hàng cho nhiều đơn cùng lúc, tiết kiệm thời gian và chi phí nhiên liệu.

4. **GPS tracking realtime**: Cập nhật vị trí shipper mỗi 5 giây, cho phép khách hàng theo dõi đơn hàng trực tiếp.

5. **Turn-by-turn navigation**: Hướng dẫn rẽ từng bước với voice guidance, giúp shipper di chuyển dễ dàng.

6. **Xác nhận giao hàng**: Checkbox COD, ghi chú, timestamp đảm bảo minh bạch trong quá trình giao hàng.

7. **Lịch sử chuyến đi**: Lưu trữ đầy đủ timeline, bản đồ lộ trình, thống kê hiệu suất giúp shipper theo dõi công việc.

8. **AI Chatbot**: Hỗ trợ 24/7 với Gemini API, giải đáp thắc mắc về quy trình, chính sách, vấn đề kỹ thuật.

9. **Yêu cầu rời shop**: Quy trình rõ ràng với lý do, thời gian dự định, và xét duyệt từ chủ shop.

10. **Thống kê và báo cáo**: Biểu đồ trực quan, so sánh xu hướng, xuất PDF giúp shipper đánh giá hiệu suất làm việc.

---

**Ngày tạo:** 01/02/2026  
**Mục đích:** Tài liệu chi tiết chức năng shipper cho báo cáo đại học  
**Bước tiếp theo:** Chụp screenshots theo danh sách đã đánh dấu
