# CHI TIẾT CHỨC NĂNG - VAI TRÒ CHỦ CỬA HÀNG (OWNER)

> **Mục đích:** Mô tả chi tiết các chức năng của vai trò Chủ cửa hàng  
> **Ngày cập nhật:** 01/02/2026

---

## 1. DASHBOARD VÀ THỐNG KÊ

### Mô tả ngắn
Dashboard cung cấp cái nhìn tổng quan về hoạt động kinh doanh của cửa hàng, bao gồm doanh thu theo thời gian, số lượng đơn hàng theo trạng thái, top sản phẩm bán chạy, và danh sách đơn hàng đang chờ xử lý. Đây là màn hình trung tâm giúp chủ shop nắm bắt tình hình kinh doanh một cách nhanh chóng.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop đăng nhập và mở Dashboard**
- Sau khi đăng nhập, hệ thống tự động chuyển đến màn hình `DashBoardRootScreen`
- Màn hình hiển thị logo cửa hàng và tên shop ở phía trên

**Bước 2: Xem thống kê doanh thu**
- Phần đầu tiên hiển thị 3 card thống kê doanh thu:
  - **Hôm nay**: 2,450,000 VNĐ
  - **Tuần này**: 15,300,000 VNĐ
  - **Tháng này**: 58,900,000 VNĐ
- Mỗi card có icon tiền và màu sắc khác nhau để dễ phân biệt
- Hiển thị tỷ lệ thay đổi so với kỳ trước (ví dụ: "+12% so với tuần trước" bằng màu xanh)

**Bước 3: Xem thống kê đơn hàng**
- Bên dưới là phần "Đơn hàng theo trạng thái":
  - 🟡 **Chờ xác nhận**: 5 đơn
  - 🔵 **Đã xác nhận**: 8 đơn
  - 🟣 **Đang chuẩn bị**: 3 đơn
  - 🟢 **Sẵn sàng giao**: 2 đơn
  - 🚚 **Đang giao**: 6 đơn
  - ✅ **Hoàn thành**: 145 đơn
- Chủ shop nhấn vào bất kỳ trạng thái nào để xem danh sách đơn hàng

**Bước 4: Xem top sản phẩm bán chạy**
- Phần "Top sản phẩm" hiển thị 5 món bán chạy nhất:
  - 1️⃣ Phở bò - 45 đơn
  - 2️⃣ Bún chả - 38 đơn
  - 3️⃣ Cơm sườn - 32 đơn
  - 4️⃣ Trà sữa trân châu - 28 đơn
  - 5️⃣ Bánh mì pate - 25 đơn
- Mỗi món hiển thị ảnh nhỏ, tên, và số lượng đã bán

**Bước 5: Xem đơn hàng đang chờ xử lý**
- Phần "Đơn chờ xử lý" hiển thị danh sách các đơn pending:
  - Mỗi đơn hiển thị: mã đơn, tên khách hàng, tổng tiền, thời gian đặt
  - Nút "Xem chi tiết" bên cạnh mỗi đơn
- Chủ shop có thể nhấn vào để xử lý ngay

**Bước 6: Xem đơn hàng gần đây**
- Phần cuối cùng hiển thị "Đơn hàng gần đây" (10 đơn mới nhất)
- Mỗi đơn hiển thị:
  - Mã đơn: ORDER-12345
  - Trạng thái: Badge màu (ví dụ: "Đang giao" - màu xanh)
  - Tổng tiền: 150,000 VNĐ
  - Thời gian: "10 phút trước"
- Có nút "Xem tất cả" để chuyển đến màn hình Orders

**Bước 7: Pull-to-refresh để cập nhật**
- Chủ shop kéo xuống từ trên cùng
- Hệ thống hiển thị animation loading
- Tất cả dữ liệu được cập nhật (doanh thu, đơn hàng, top sản phẩm)

### Screenshot cần chụp
- [ ] `DashBoardRootScreen`: Toàn bộ dashboard với tất cả các phần
- [ ] `DashBoardRootScreen`: Card thống kê doanh thu hôm nay/tuần/tháng
- [ ] `DashBoardRootScreen`: Thống kê đơn hàng theo trạng thái
- [ ] `DashBoardRootScreen`: Top 5 sản phẩm bán chạy
- [ ] `DashBoardRootScreen`: Danh sách đơn chờ xử lý

---

## 2. QUẢN LÝ SẢN PHẨM

### Mô tả ngắn
Chức năng quản lý sản phẩm cho phép chủ shop thêm sản phẩm mới, chỉnh sửa thông tin sản phẩm hiện có, upload nhiều ảnh cho mỗi sản phẩm, bật/tắt trạng thái bán, và lọc sản phẩm theo danh mục. Đây là công cụ quan trọng để quản lý menu của cửa hàng.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình quản lý sản phẩm**
- Từ menu chính, chủ shop nhấn vào "Sản phẩm"
- Hệ thống hiển thị màn hình `FoodsScreen`
- Danh sách tất cả sản phẩm hiển thị dạng grid (2 cột) hoặc list
- Mỗi sản phẩm hiển thị: ảnh, tên, giá, trạng thái (Đang bán/Ngừng bán)

**Bước 2: Lọc sản phẩm theo danh mục**
- Phía trên danh sách có chips để lọc:
  - 🍜 Tất cả (45)
  - 🍜 Đồ ăn (28)
  - 🥤 Đồ uống (12)
  - 🍰 Tráng miệng (5)
- Chủ shop nhấn vào "Đồ uống"
- Danh sách chỉ hiển thị 12 sản phẩm thuộc danh mục đồ uống

**Bước 3: Thêm sản phẩm mới**
- Chủ shop nhấn vào nút FAB (Floating Action Button) "+" ở góc dưới phải
- Hệ thống chuyển đến màn hình `AddEditProductScreen`
- Form hiển thị các trường:
  - **Tên sản phẩm**: Ô nhập text
  - **Mô tả**: Ô nhập text nhiều dòng
  - **Giá**: Ô nhập số (VNĐ)
  - **Danh mục**: Dropdown (Đồ ăn, Đồ uống, Tráng miệng...)
  - **Ảnh sản phẩm**: Khu vực upload ảnh

**Bước 4: Upload ảnh sản phẩm**
- Chủ shop nhấn vào khu vực "Thêm ảnh"
- Hệ thống hiển thị bottom sheet:
  - 📷 Chụp ảnh
  - 🖼️ Chọn từ thư viện
- Chủ shop chọn "Chọn từ thư viện"
- Gallery mở ra, chủ shop chọn 3 ảnh
- Các ảnh hiển thị dạng thumbnail, có thể xóa hoặc sắp xếp lại

**Bước 5: Điền thông tin và lưu**
- Chủ shop nhập:
  - Tên: "Trà sữa trân châu đường đen"
  - Mô tả: "Trà sữa ngọt mát với trân châu dai ngon"
  - Giá: 35,000
  - Danh mục: "Đồ uống"
- Nhấn nút "Lưu"
- Hệ thống hiển thị loading spinner
- Sau 2-3 giây, hiển thị toast: "Thêm sản phẩm thành công"
- Quay về màn hình `FoodsScreen`, sản phẩm mới xuất hiện ở đầu danh sách

**Bước 6: Chỉnh sửa sản phẩm**
- Chủ shop nhấn vào một sản phẩm trong danh sách
- Hệ thống mở màn hình `AddEditProductScreen` với dữ liệu đã điền sẵn
- Chủ shop thay đổi giá từ 35,000 → 32,000
- Nhấn "Lưu"
- Toast hiển thị: "Cập nhật sản phẩm thành công"
- Giá mới được cập nhật trên danh sách

**Bước 7: Bật/tắt sản phẩm**
- Trên màn hình `FoodsScreen`, mỗi sản phẩm có switch bật/tắt
- Chủ shop tắt switch của sản phẩm "Cơm gà"
- Hệ thống hiển thị dialog xác nhận: "Ngừng bán sản phẩm này?"
- Chủ shop xác nhận
- Badge "Ngừng bán" xuất hiện trên sản phẩm
- Khách hàng sẽ không thấy sản phẩm này trong app

**Bước 8: Xóa sản phẩm**
- Chủ shop vuốt trái một sản phẩm
- Hiển thị nút "Xóa" màu đỏ
- Chủ shop nhấn "Xóa"
- Dialog xác nhận: "Xóa sản phẩm này vĩnh viễn? Không thể hoàn tác."
- Chủ shop xác nhận
- Sản phẩm biến mất khỏi danh sách

### Screenshot cần chụp
- [ ] `FoodsScreen`: Danh sách sản phẩm dạng grid
- [ ] `FoodsScreen`: Chips lọc theo danh mục
- [ ] `FoodsScreen`: Sản phẩm có switch bật/tắt
- [ ] `AddEditProductScreen`: Form thêm sản phẩm mới (rỗng)
- [ ] `AddEditProductScreen`: Upload nhiều ảnh sản phẩm
- [ ] `AddEditProductScreen`: Form đã điền đầy đủ thông tin
- [ ] Dialog xác nhận ngừng bán/xóa sản phẩm

---

## 3. XỬ LÝ ĐƠN HÀNG

### Mô tả ngắn
Chức năng xử lý đơn hàng cho phép chủ shop xem tất cả đơn hàng được phân loại theo trạng thái, xác nhận đơn mới, cập nhật tiến độ chuẩn bị, đánh dấu sẵn sàng giao, và hủy đơn với lý do cụ thể. Đây là luồng quan trọng nhất trong quá trình vận hành cửa hàng.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình đơn hàng**
- Từ menu chính, chủ shop nhấn vào "Đơn hàng"
- Hệ thống hiển thị màn hình `OrdersScreen`
- Màn hình có 6 tabs ngang:
  - 🟡 Chờ (5) - Pending
  - 🔵 Đã nhận (8) - Confirmed
  - 🟣 Chuẩn bị (3) - Preparing
  - 🟢 Sẵn sàng (2) - Ready
  - 🚚 Đang giao (6) - Shipping
  - ✅ Hoàn thành (145) - Completed
- Số trong ngoặc là số lượng đơn ở mỗi trạng thái

**Bước 2: Xem danh sách đơn chờ xác nhận**
- Tab "Chờ" được chọn mặc định (có 5 đơn mới)
- Mỗi đơn hiển thị:
  - Mã đơn: ORDER-12345
  - Tên khách hàng: Nguyễn Văn A
  - Số điện thoại: 098-xxx-xxxx
  - Tổng tiền: 150,000 VNĐ
  - Thời gian đặt: "5 phút trước"
  - 2 nút: "Xác nhận" (xanh) và "Hủy" (đỏ)
- Đơn mới nhất nằm ở trên cùng

**Bước 3: Xem chi tiết đơn hàng**
- Chủ shop nhấn vào đơn ORDER-12345
- Hệ thống mở màn hình `OrderDetailScreen`
- Hiển thị đầy đủ thông tin:
  - **Khách hàng**: Tên, số điện thoại, địa chỉ giao hàng
  - **Danh sách sản phẩm**:
    - 2x Phở bò - 120,000 VNĐ
    - 1x Trà đá - 10,000 VNĐ
  - **Thanh toán**:
    - Tạm tính: 130,000 VNĐ
    - Phí ship: 20,000 VNĐ
    - Voucher: -0 VNĐ
    - **Tổng**: 150,000 VNĐ
  - **Phương thức**: COD (Tiền mặt)
  - **Ghi chú**: "Không hành, thêm rau"

**Bước 4: Xác nhận đơn hàng**
- Chủ shop nhấn nút "Xác nhận đơn"
- Hệ thống hiển thị dialog xác nhận: "Xác nhận đơn hàng này?"
- Chủ shop nhấn "Xác nhận"
- Hệ thống:
  - Hiển thị loading spinner
  - Chuyển đơn từ tab "Chờ" sang tab "Đã nhận"
  - Gửi thông báo cho khách hàng: "Đơn hàng đã được xác nhận"
  - Toast hiển thị: "Xác nhận đơn thành công"

**Bước 5: Cập nhật trạng thái "Đang chuẩn bị"**
- Chủ shop chuyển sang tab "Đã nhận"
- Nhấn vào đơn ORDER-12345
- Nhấn nút "Bắt đầu chuẩn bị"
- Đơn chuyển sang tab "Chuẩn bị"
- Gửi thông báo cho khách: "Đơn hàng đang được chuẩn bị"

**Bước 6: Đánh dấu sẵn sàng giao**
- Sau khi chuẩn bị xong, chủ shop chuyển sang tab "Chuẩn bị"
- Nhấn vào đơn ORDER-12345
- Nhấn nút "Sẵn sàng giao"
- Đơn chuyển sang tab "Sẵn sàng"
- Hệ thống:
  - Gửi thông báo cho **tất cả shipper đang online** của shop:
    - "Đơn hàng ORDER-12345 sẵn sàng giao"
    - Topic: `shop_${shopId}_shippers_active`
  - Gửi thông báo cho khách: "Đơn hàng sẵn sàng, chờ shipper nhận"

**Bước 7: Theo dõi đơn đang giao**
- Shipper nhận đơn → Đơn tự động chuyển sang tab "Đang giao"
- Chủ shop vào tab "Đang giao" để xem tiến độ
- Hiển thị thông tin shipper đang giao:
  - Tên: Trần Văn B
  - Số điện thoại: 097-xxx-xxxx
  - Trạng thái: "Đang giao hàng"

**Bước 8: Hủy đơn hàng**
- Nếu đơn đang ở trạng thái "Chờ" hoặc "Đã nhận"
- Chủ shop nhấn nút "Hủy đơn"
- Hệ thống hiển thị dialog:
  - "Lý do hủy đơn:"
  - Radio buttons:
    - ⭕ Hết hàng
    - ⭕ Khách hàng yêu cầu
    - ⭕ Không liên hệ được khách
    - ⭕ Lý do khác
  - Ô nhập chi tiết (nếu chọn "Lý do khác")
- Chủ shop chọn "Hết hàng" và nhấn "Xác nhận"
- Đơn chuyển sang trạng thái "Cancelled"
- Gửi thông báo cho khách: "Đơn hàng đã bị hủy - Lý do: Hết hàng"

### Screenshot cần chụp
- [ ] `OrdersScreen`: 6 tabs với số lượng đơn hàng
- [ ] `OrdersScreen`: Danh sách đơn ở tab "Chờ" với 2 nút
- [ ] `OrderDetailScreen`: Chi tiết đơn hàng đầy đủ
- [ ] `OrderDetailScreen`: Nút "Xác nhận đơn"
- [ ] `OrderDetailScreen`: Nút "Bắt đầu chuẩn bị"
- [ ] `OrderDetailScreen`: Nút "Sẵn sàng giao"
- [ ] Dialog chọn lý do hủy đơn
- [ ] Toast thông báo xác nhận thành công

---

## 4. DUYỆT SHIPPER

### Mô tả ngắn
Chức năng duyệt shipper cho phép chủ shop xem danh sách tài xế đăng ký làm việc cho cửa hàng, xem thông tin chi tiết bao gồm CCCD và bằng lái xe, phê duyệt hoặc từ chối đơn đăng ký, và xử lý các yêu cầu rời shop từ shipper hiện tại.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình quản lý shipper**
- Từ menu chính, chủ shop nhấn vào "Shipper"
- Hệ thống hiển thị màn hình `ShippersScreen`
- Màn hình có 3 tabs:
  - 🟡 **Chờ duyệt** (3) - Pending
  - ✅ **Đã duyệt** (12) - Approved
  - ⚠️ **Yêu cầu rời** (1) - Removal Requests

**Bước 2: Xem danh sách shipper chờ duyệt**
- Tab "Chờ duyệt" hiển thị 3 đơn đăng ký mới
- Mỗi đơn hiển thị:
  - Avatar (ảnh từ CCCD hoặc mặc định)
  - Tên: Trần Văn B
  - Số điện thoại: 097-xxx-xxxx
  - Ngày đăng ký: "2 ngày trước"
  - Badge: "Chờ xét duyệt" (màu vàng)
  - Nút "Xem chi tiết"

**Bước 3: Xem chi tiết đơn đăng ký**
- Chủ shop nhấn vào một shipper
- Hệ thống mở màn hình `ShipperDetailScreen`
- Hiển thị đầy đủ thông tin:
  - **Thông tin cá nhân**:
    - Họ tên: Trần Văn B
    - Số điện thoại: 097-xxx-xxxx
    - Email: tranvanb@gmail.com
    - Ngày sinh: 15/03/1995
  - **CCCD (Căn cước công dân)**:
    - Số CCCD: 001095xxxxxx
    - Ảnh mặt trước (có thể zoom)
    - Ảnh mặt sau (có thể zoom)
  - **Bằng lái xe**:
    - Loại: A1 (xe máy)
    - Số bằng: 12345678
    - Ảnh bằng lái (có thể zoom)
  - **Trạng thái**: Chờ duyệt

**Bước 4: Zoom ảnh CCCD/bằng lái**
- Chủ shop nhấn vào ảnh mặt trước CCCD
- Ảnh hiển thị toàn màn hình với khả năng pinch-to-zoom
- Chủ shop kiểm tra thông tin trên CCCD
- Nhấn nút back để quay lại
- Làm tương tự với ảnh mặt sau và bằng lái xe

**Bước 5: Phê duyệt shipper**
- Sau khi kiểm tra, chủ shop quyết định phê duyệt
- Nhấn nút "Phê duyệt" (màu xanh) ở cuối màn hình
- Hệ thống hiển thị dialog xác nhận: "Phê duyệt shipper này?"
- Chủ shop nhấn "Xác nhận"
- Hệ thống:
  - Hiển thị loading spinner
  - Cập nhật trạng thái → "Đã duyệt"
  - Gửi thông báo cho shipper: "Chúc mừng! Bạn đã được duyệt làm shipper cho [Tên Shop]"
  - Toast hiển thị: "Phê duyệt thành công"
- Quay về `ShippersScreen`, shipper chuyển sang tab "Đã duyệt"

**Bước 6: Từ chối đơn đăng ký**
- Nếu chủ shop không muốn duyệt (ví dụ: ảnh không rõ)
- Nhấn nút "Từ chối" (màu đỏ)
- Hệ thống hiển thị dialog:
  - "Lý do từ chối:"
  - Radio buttons:
    - ⭕ Ảnh CCCD không rõ
    - ⭕ Bằng lái không hợp lệ
    - ⭕ Thông tin không chính xác
    - ⭕ Lý do khác
  - Ô nhập chi tiết
- Chủ shop chọn "Ảnh CCCD không rõ" và nhấn "Xác nhận"
- Gửi thông báo cho shipper: "Đơn đăng ký bị từ chối - Lý do: Ảnh CCCD không rõ. Vui lòng nộp lại."
- Đơn đăng ký bị xóa khỏi danh sách

**Bước 7: Xem danh sách shipper đã duyệt**
- Chủ shop chuyển sang tab "Đã duyệt"
- Danh sách 12 shipper đang làm việc
- Mỗi shipper hiển thị:
  - Avatar, tên, số điện thoại
  - Trạng thái: "Đang online" (xanh) hoặc "Offline" (xám)
  - Số đơn đã giao: 145 đơn
  - Đánh giá trung bình: 4.8⭐
- Có nút "Xem chi tiết" để xem lịch sử giao hàng

**Bước 8: Xử lý yêu cầu rời shop**
- Chủ shop chuyển sang tab "Yêu cầu rời"
- Có 1 yêu cầu từ shipper Nguyễn Văn C
- Hiển thị:
  - Tên shipper: Nguyễn Văn C
  - Lý do: "Chuyển sang khu vực khác"
  - Ngày gửi yêu cầu: "1 ngày trước"
- Chủ shop nhấn "Chấp thuận"
- Hệ thống:
  - Xóa shipper khỏi danh sách shop
  - Gửi thông báo: "Yêu cầu rời shop đã được chấp thuận"
  - Toast: "Đã xử lý yêu cầu"

### Screenshot cần chụp
- [ ] `ShippersScreen`: 3 tabs (Chờ duyệt, Đã duyệt, Yêu cầu rời)
- [ ] `ShippersScreen`: Danh sách shipper chờ duyệt
- [ ] `ShipperDetailScreen`: Thông tin chi tiết shipper
- [ ] `ShipperDetailScreen`: Ảnh CCCD mặt trước/sau
- [ ] `ShipperDetailScreen`: Ảnh bằng lái xe
- [ ] `ShipperDetailScreen`: Nút "Phê duyệt" và "Từ chối"
- [ ] Dialog chọn lý do từ chối
- [ ] `ShippersScreen` tab "Đã duyệt": Shipper với trạng thái online/offline
- [ ] `RemovalRequestsScreen`: Yêu cầu rời shop

---

## 5. QUẢN LÝ VOUCHER

### Mô tả ngắn
Chức năng quản lý voucher cho phép chủ shop tạo các mã giảm giá cho khách hàng, bao gồm voucher giảm theo phần trăm, giảm trực tiếp, hoặc miễn phí ship. Chủ shop có thể chỉnh sửa thông tin, bật/tắt trạng thái, và xem thống kê sử dụng của từng voucher.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình quản lý voucher**
- Từ menu chính, chủ shop nhấn vào "Voucher"
- Hệ thống hiển thị màn hình `VouchersScreen`
- Danh sách tất cả voucher của shop hiển thị
- Mỗi voucher hiển thị:
  - Mã: GIAM50K
  - Mô tả: "Giảm 50K cho đơn từ 200K"
  - Loại: Badge "Giảm giá" hoặc "Free ship"
  - Trạng thái: Switch bật/tắt
  - Số lượt dùng: 15/100

**Bước 2: Tạo voucher giảm giá mới**
- Chủ shop nhấn nút FAB "+" ở góc dưới phải
- Hệ thống mở màn hình `AddEditVoucherScreen`
- Form hiển thị các trường:
  - **Mã voucher**: Ô nhập text (viết hoa tự động)
  - **Mô tả**: Ô nhập text
  - **Loại voucher**: Dropdown
    - Giảm theo phần trăm (%)
    - Giảm trực tiếp (VNĐ)
    - Miễn phí ship
  - **Giá trị giảm**: Ô nhập số
  - **Giảm tối đa**: Ô nhập số (chỉ hiện nếu chọn %)
  - **Đơn tối thiểu**: Ô nhập số
  - **Số lượng**: Ô nhập số
  - **Ngày bắt đầu**: Date picker
  - **Ngày kết thúc**: Date picker

**Bước 3: Điền thông tin voucher giảm theo %**
- Chủ shop nhập:
  - Mã: GIAM20
  - Mô tả: "Giảm 20% cho đơn đầu tiên"
  - Loại: "Giảm theo phần trăm"
  - Giá trị: 20 (%)
  - Giảm tối đa: 50,000 VNĐ
  - Đơn tối thiểu: 100,000 VNĐ
  - Số lượng: 50
  - Từ: 01/02/2026
  - Đến: 28/02/2026
- Nhấn nút "Tạo voucher"

**Bước 4: Lưu và xem preview**
- Hệ thống hiển thị loading spinner
- Sau 2 giây, hiển thị dialog preview:
  - 🎫 **GIAM20**
  - "Giảm 20% cho đơn đầu tiên"
  - "Giảm tối đa: 50,000đ"
  - "Đơn tối thiểu: 100,000đ"
  - "HSD: 28/02/2026"
  - "Còn: 50 lượt"
- Chủ shop nhấn "Xác nhận"
- Toast: "Tạo voucher thành công"
- Quay về `VouchersScreen`, voucher mới ở đầu danh sách

**Bước 5: Tạo voucher free ship**
- Chủ shop nhấn "+" để tạo voucher khác
- Điền thông tin:
  - Mã: FREESHIP99
  - Mô tả: "Miễn phí ship cho đơn từ 99K"
  - Loại: "Miễn phí ship"
  - Đơn tối thiểu: 99,000 VNĐ
  - Số lượng: 100
  - Từ: 01/02/2026 - Đến: 15/02/2026
- Nhấn "Tạo voucher"
- Voucher được tạo và hiển thị badge "Free ship" màu xanh

**Bước 6: Chỉnh sửa voucher**
- Chủ shop nhấn vào voucher GIAM50K trong danh sách
- Màn hình `AddEditVoucherScreen` mở với dữ liệu đã điền sẵn
- Chủ shop thay đổi:
  - Số lượng: 100 → 200
  - Ngày kết thúc: 28/02 → 31/03
- Nhấn "Cập nhật"
- Toast: "Cập nhật voucher thành công"

**Bước 7: Bật/tắt voucher**
- Trên `VouchersScreen`, voucher GIAM20 có switch đang bật (xanh)
- Chủ shop tắt switch
- Dialog xác nhận: "Tắt voucher này? Khách hàng sẽ không thể sử dụng."
- Chủ shop xác nhận
- Badge "Ngừng hoạt động" hiển thị màu đỏ
- Khách hàng không còn thấy voucher này khi thanh toán

**Bước 8: Xem thống kê sử dụng**
- Chủ shop nhấn vào voucher FREESHIP99
- Cuộn xuống phần "Thống kê"
- Hiển thị:
  - 📊 **Đã sử dụng**: 35/100 lượt
  - 💰 **Tổng giảm giá**: 700,000 VNĐ
  - 📅 **Ngày sử dụng gần nhất**: 30/01/2026
  - 📈 **Số đơn hàng**: 35 đơn
  - ⭐ **Khách hàng sử dụng**: 28 người (7 người dùng lại)
- Có biểu đồ cột hiển thị lượt dùng theo ngày

**Bước 9: Xóa voucher**
- Chủ shop vuốt trái voucher hết hạn
- Nút "Xóa" màu đỏ xuất hiện
- Nhấn "Xóa"
- Dialog: "Xóa voucher này? Không thể hoàn tác."
- Xác nhận → Voucher biến mất

### Screenshot cần chụp
- [ ] `VouchersScreen`: Danh sách voucher với trạng thái
- [ ] `AddEditVoucherScreen`: Form tạo voucher mới (rỗng)
- [ ] `AddEditVoucherScreen`: Form đã điền đầy đủ (voucher giảm %)
- [ ] `AddEditVoucherScreen`: Form voucher free ship
- [ ] Dialog preview voucher trước khi lưu
- [ ] `VouchersScreen`: Voucher với badge "Giảm giá" và "Free ship"
- [ ] `VouchersScreen`: Switch bật/tắt voucher
- [ ] `AddEditVoucherScreen`: Phần thống kê sử dụng voucher
- [ ] Dialog xác nhận tắt/xóa voucher

---

## 6. PHÂN TÍCH DOANH THU

### Mô tả ngắn
Chức năng phân tích doanh thu cung cấp báo cáo chi tiết về doanh thu theo khoảng thời gian (ngày, tuần, tháng), so sánh xu hướng với kỳ trước, hiển thị biểu đồ trực quan, và liệt kê top sản phẩm có doanh thu cao nhất. Đây là công cụ quan trọng giúp chủ shop theo dõi hiệu quả kinh doanh.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình phân tích doanh thu**
- Từ menu chính, chủ shop nhấn vào "Doanh thu"
- Hệ thống hiển thị màn hình `RevenueScreen`
- Mặc định hiển thị dữ liệu tháng hiện tại

**Bước 2: Xem tổng quan doanh thu**
- Phần đầu hiển thị các card tổng quan:
  - 💰 **Tổng doanh thu**: 58,900,000 VNĐ
  - 📦 **Tổng đơn hàng**: 245 đơn
  - 💵 **Giá trị trung bình/đơn**: 240,408 VNĐ
  - 📈 **Xu hướng**: +15.3% so với tháng trước (màu xanh, mũi tên lên)

**Bước 3: Chọn khoảng thời gian**
- Phía trên có 3 tabs:
  - 📅 **Theo ngày** (Daily)
  - 📊 **Theo tuần** (Weekly)
  - 📈 **Theo tháng** (Monthly)
- Mặc định chọn "Theo tháng"
- Chủ shop nhấn vào "Theo tuần"

**Bước 4: Xem doanh thu theo tuần**
- Hệ thống hiển thị dữ liệu tuần hiện tại (01/02 - 07/02/2026)
- Card cập nhật:
  - 💰 **Doanh thu tuần này**: 15,300,000 VNĐ
  - 📦 **Đơn hàng**: 68 đơn
  - 💵 **TB/đơn**: 225,000 VNĐ
  - 📈 **Xu hướng**: +8.5% so với tuần trước

**Bước 5: Xem biểu đồ doanh thu**
- Dưới phần tổng quan là biểu đồ cột (Bar Chart)
- Trục X: 7 ngày trong tuần (T2, T3, T4, T5, T6, T7, CN)
- Trục Y: Doanh thu (triệu đồng)
- Mỗi cột có màu khác nhau, cao thấp theo doanh thu
- Ví dụ:
  - T2: 1.8M (thấp - đầu tuần)
  - T6: 3.2M (cao - cuối tuần)
  - T7: 4.5M (cao nhất - cuối tuần)
  - CN: 3.8M
- Chủ shop có thể nhấn vào cột để xem chi tiết ngày đó

**Bước 6: Xem chi tiết một ngày**
- Chủ shop nhấn vào cột "T7"
- Bottom sheet hiển thị:
  - 📅 **Ngày**: Thứ 7, 06/02/2026
  - 💰 **Doanh thu**: 4,500,000 VNĐ
  - 📦 **Số đơn**: 18 đơn
  - 💵 **TB/đơn**: 250,000 VNĐ
  - 🕐 **Giờ cao điểm**: 11:30 - 13:00 (6 đơn)
  - 🎫 **Voucher đã dùng**: 8 lượt (-180,000đ)
- Nút "Xem chi tiết đơn hàng" để xem danh sách 18 đơn

**Bước 7: So sánh với kỳ trước**
- Dưới biểu đồ có section "So sánh"
- Hiển thị 2 biểu đồ cột nhỏ song song:
  - **Tuần này** (màu xanh): 15.3M
  - **Tuần trước** (màu xám): 14.1M
- Hiển thị mũi tên xanh ↗ +8.5%
- Nếu giảm, sẽ hiển thị mũi tên đỏ ↘

**Bước 8: Xem top sản phẩm theo doanh thu**
- Cuộn xuống phần "Top sản phẩm"
- Danh sách 10 món có doanh thu cao nhất trong kỳ:
  - 1️⃣ **Phở bò đặc biệt**
    - Đã bán: 85 phần
    - Giá: 60,000đ/phần
    - Doanh thu: 5,100,000đ
    - % tổng doanh thu: 8.7%
  - 2️⃣ **Cơm sườn**
    - Đã bán: 72 phần
    - Giá: 50,000đ/phần
    - Doanh thu: 3,600,000đ
    - % tổng doanh thu: 6.1%
  - ...
- Mỗi món có progress bar hiển thị % so với tổng

**Bước 9: Chọn khoảng thời gian tùy chỉnh**
- Chủ shop nhấn vào icon lịch ở góc trên phải
- Date range picker xuất hiện
- Chủ shop chọn: Từ 15/01/2026 đến 31/01/2026
- Nhấn "Áp dụng"
- Tất cả dữ liệu (tổng quan, biểu đồ, top sản phẩm) cập nhật theo khoảng thời gian này

**Bước 10: Xuất báo cáo (nếu có)**
- Nhấn nút "Xuất báo cáo" ở góc trên
- Hệ thống tạo file PDF hoặc Excel
- Hiển thị dialog: "Báo cáo đã được lưu vào Downloads"
- Chủ shop có thể gửi báo cáo qua email hoặc in

### Screenshot cần chụp
- [ ] `RevenueScreen`: Tổng quan doanh thu với 4 cards
- [ ] `RevenueScreen`: 3 tabs chọn khoảng thời gian (ngày/tuần/tháng)
- [ ] `RevenueScreen`: Biểu đồ cột doanh thu theo tuần
- [ ] `RevenueScreen`: Bottom sheet chi tiết một ngày
- [ ] `RevenueScreen`: Section so sánh với kỳ trước
- [ ] `RevenueScreen`: Top 10 sản phẩm theo doanh thu
- [ ] Date range picker để chọn khoảng thời gian tùy chỉnh
- [ ] Xu hướng tăng (+8.5%) với mũi tên xanh
- [ ] Xu hướng giảm (nếu có) với mũi tên đỏ

---

## 7. QUẢN LÝ KHÁCH HÀNG (BUYER TIERS)

### Mô tả ngắn
Chức năng quản lý khách hàng cho phép chủ shop xem danh sách tất cả khách hàng đã mua hàng, phân loại theo hạng thành viên (Bronze, Silver, Gold, Diamond) dựa trên tổng chi tiêu, và xem lịch sử mua hàng chi tiết của từng khách. Đây là công cụ giúp chủ shop hiểu rõ khách hàng và xây dựng chiến lược chăm sóc phù hợp.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình quản lý khách hàng**
- Từ menu chính, chủ shop nhấn vào "Khách hàng"
- Hệ thống hiển thị màn hình `CustomerScreen`
- Danh sách tất cả khách hàng đã từng mua hàng

**Bước 2: Xem thống kê tổng quan**
- Phía trên màn hình hiển thị 4 cards:
  - 💎 **Diamond**: 5 khách (≥5M)
  - 🥇 **Gold**: 15 khách (≥2M)
  - 🥈 **Silver**: 42 khách (≥500K)
  - 🥉 **Bronze**: 138 khách (<500K)
- Tổng cộng: **200 khách hàng**

**Bước 3: Lọc khách hàng theo hạng**
- Dưới cards có chips để lọc:
  - Tất cả (200)
  - 💎 Diamond (5)
  - 🥇 Gold (15)
  - 🥈 Silver (42)
  - 🥉 Bronze (138)
- Mặc định hiển thị "Tất cả"
- Chủ shop nhấn vào "Diamond"

**Bước 4: Xem danh sách khách Diamond**
- Danh sách 5 khách hàng VIP nhất hiển thị
- Mỗi khách hiển thị:
  - Avatar và tên: Nguyễn Văn A
  - Badge: 💎 Diamond
  - Số điện thoại: 098-xxx-xxxx
  - Tổng chi tiêu: 8,500,000 VNĐ
  - Số đơn hàng: 45 đơn
  - Đơn gần nhất: "3 ngày trước"
- Sắp xếp theo tổng chi tiêu giảm dần

**Bước 5: Xem chi tiết một khách hàng**
- Chủ shop nhấn vào khách hàng "Nguyễn Văn A"
- Hệ thống mở màn hình `CustomerDetailScreen`
- Phần thông tin cá nhân:
  - Avatar lớn
  - Tên: Nguyễn Văn A
  - Badge: 💎 Diamond
  - Số điện thoại: 098-xxx-xxxx
  - Email: nguyenvana@gmail.com
  - Ngày đăng ký: 15/08/2025

**Bước 6: Xem thống kê mua hàng**
- Dưới thông tin cá nhân là các thẻ thống kê:
  - 💰 **Tổng chi tiêu**: 8,500,000 VNĐ
  - 📦 **Tổng đơn hàng**: 45 đơn
  - 💵 **Giá trị TB/đơn**: 188,888 VNĐ
  - 🎫 **Voucher đã dùng**: 12 lần
  - ⭐ **Đánh giá trung bình**: 4.8/5 (15 đánh giá)
  - 📅 **Khách hàng từ**: 6 tháng

**Bước 7: Xem lịch sử mua hàng**
- Cuộn xuống phần "Lịch sử đơn hàng"
- Danh sách 45 đơn hàng được sắp xếp theo thời gian (mới nhất lên đầu)
- Mỗi đơn hiển thị:
  - Mã: ORDER-12345
  - Ngày: 28/01/2026
  - Sản phẩm: "Phở bò đặc biệt, Trà đá (và 1 món khác)"
  - Tổng tiền: 180,000 VNĐ
  - Trạng thái: ✅ Hoàn thành
- Chủ shop có thể nhấn vào để xem chi tiết đơn

**Bước 8: Xem món ăn yêu thích**
- Phần "Món ăn yêu thích" hiển thị:
  - 1️⃣ Phở bò đặc biệt - Đã mua 18 lần
  - 2️⃣ Cơm sườn - Đã mua 12 lần
  - 3️⃣ Trà sữa trân châu - Đã mua 10 lần
- Giúp chủ shop hiểu sở thích khách hàng để gợi ý hoặc tạo voucher phù hợp

**Bước 9: Xem biểu đồ chi tiêu theo thời gian**
- Phần "Xu hướng chi tiêu" hiển thị biểu đồ đường (Line Chart)
- Trục X: 6 tháng gần đây
- Trục Y: Số tiền chi tiêu
- Cho thấy khách hàng này chi tiêu đều đặn, có xu hướng tăng

**Bước 10: Liên hệ khách hàng**
- Ở góc trên phải có 2 nút:
  - 💬 **Chat**: Mở trực tiếp cuộc hội thoại với khách
  - 📞 **Gọi**: Mở danh bạ với số điện thoại sẵn sàng
- Chủ shop nhấn "Chat"
- Chuyển đến màn hình `ChatScreen` với khách hàng này
- Chủ shop có thể gửi tin nhắn cảm ơn hoặc thông báo khuyến mãi riêng

**Bước 11: Tìm kiếm khách hàng**
- Quay lại `CustomerScreen`
- Chủ shop nhấn vào ô tìm kiếm
- Gõ tên hoặc số điện thoại: "Nguyễn"
- Hệ thống lọc và hiển thị tất cả khách có tên chứa "Nguyễn"

**Bước 12: Sắp xếp danh sách**
- Chủ shop nhấn vào icon sắp xếp
- Bottom sheet hiển thị options:
  - Tổng chi tiêu (cao → thấp)
  - Tổng chi tiêu (thấp → cao)
  - Số đơn hàng (nhiều → ít)
  - Đơn gần nhất
  - Tên A-Z
- Chủ shop chọn "Đơn gần nhất"
- Danh sách sắp xếp lại, khách mua gần đây nhất lên đầu

### Screenshot cần chụp
- [ ] `CustomerScreen`: 4 cards thống kê theo hạng
- [ ] `CustomerScreen`: Chips lọc theo hạng (Diamond, Gold, Silver, Bronze)
- [ ] `CustomerScreen`: Danh sách khách hàng với badge hạng
- [ ] `CustomerDetailScreen`: Thông tin cá nhân khách hàng
- [ ] `CustomerDetailScreen`: Các thẻ thống kê mua hàng
- [ ] `CustomerDetailScreen`: Lịch sử đơn hàng
- [ ] `CustomerDetailScreen`: Top món ăn yêu thích
- [ ] `CustomerDetailScreen`: Biểu đồ xu hướng chi tiêu
- [ ] `CustomerScreen`: Ô tìm kiếm khách hàng
- [ ] Bottom sheet sắp xếp danh sách

---

## 8. CHAT VỚI KHÁCH HÀNG

### Mô tả ngắn
Chức năng chat cho phép chủ shop nhắn tin trực tiếp với khách hàng để tư vấn sản phẩm, giải đáp thắc mắc, và xử lý các vấn đề về đơn hàng. Hệ thống sử dụng Firestore Realtime và Optimistic UI để đảm bảo trải nghiệm nhắn tin mượt mà và tức thời.

### Kịch bản sử dụng (Case Study)

**Bước 1: Chủ shop mở màn hình chat**
- Từ menu chính, chủ shop nhấn vào "Tin nhắn"
- Hệ thống hiển thị màn hình `OwnerConversationsScreen`
- Danh sách tất cả cuộc hội thoại với khách hàng

**Bước 2: Xem danh sách cuộc hội thoại**
- Mỗi cuộc hội thoại hiển thị:
  - Avatar khách hàng
  - Tên: Nguyễn Văn A
  - Badge hạng: 💎 Diamond (nếu có)
  - Tin nhắn gần nhất: "Món phở bò còn không ạ?"
  - Thời gian: "2 phút trước"
  - Badge số tin chưa đọc: (3) - màu đỏ
- Cuộc hội thoại có tin chưa đọc hiển thị đậm hơn
- Sắp xếp theo thời gian tin nhắn mới nhất

**Bước 3: Lọc tin nhắn chưa đọc**
- Phía trên có 2 tabs:
  - **Tất cả** (28)
  - **Chưa đọc** (5)
- Chủ shop nhấn vào "Chưa đọc"
- Chỉ hiển thị 5 cuộc hội thoại có tin chưa đọc
- Giúp chủ shop ưu tiên trả lời

**Bước 4: Mở một cuộc hội thoại**
- Chủ shop nhấn vào cuộc hội thoại với "Nguyễn Văn A"
- Hệ thống mở màn hình `OwnerChatDetailScreen`
- Lịch sử chat đầy đủ hiển thị:
  - Tin nhắn của khách (trái, màu xám)
  - Tin nhắn của shop (phải, màu xanh)
- Header hiển thị:
  - Avatar và tên khách: Nguyễn Văn A
  - Badge: 💎 Diamond
  - Trạng thái: "Đang online" (chấm xanh) hoặc "Hoạt động 5 phút trước"

**Bước 5: Xem thông tin khách hàng**
- Chủ shop nhấn vào avatar/tên khách trên header
- Bottom sheet hiển thị thông tin nhanh:
  - Tên: Nguyễn Văn A
  - Số điện thoại: 098-xxx-xxxx
  - Hạng: 💎 Diamond
  - Tổng chi tiêu: 8,500,000đ
  - Số đơn hàng: 45 đơn
  - Nút "Xem chi tiết" → Chuyển đến `CustomerDetailScreen`
  - Nút "Gọi điện"

**Bước 6: Trả lời tin nhắn**
- Chủ shop gõ tin nhắn: "Còn ạ! Hiện tại còn 5 tô."
- Nhấn nút gửi (icon máy bay giấy)

**Bước 7: Optimistic UI hoạt động**
- **NGAY LẬP TỨC** tin nhắn hiển thị phía bên phải với:
  - Nội dung: "Còn ạ! Hiện tại còn 5 tô."
  - Thời gian: "Vừa xong"
  - Icon đồng hồ (đang gửi)
- Chủ shop có thể tiếp tục gõ tin nhắn khác

**Bước 8: Xác nhận từ server**
- Sau 1-2 giây, Firestore xác nhận đã lưu tin nhắn
- Icon đồng hồ đổi thành icon tick đơn (đã gửi)
- Khi khách hàng xem tin nhắn → Icon đổi thành double tick màu xanh

**Bước 9: Nhận tin nhắn realtime**
- Khách hàng trả lời: "Vậy cho em đặt 2 tô nhé!"
- Tin nhắn **TỰ ĐỘNG HIỂN THỊ** ngay lập tức phía bên trái
- Không cần refresh
- Có âm thanh thông báo nhẹ (nếu bật)
- Màn hình tự động scroll xuống tin nhắn mới

**Bước 10: Gửi tin nhắn nhanh (Quick Replies)**
- Chủ shop có thể nhấn vào icon "⚡" bên cạnh ô nhập
- Hệ thống hiển thị danh sách tin nhắn mẫu:
  - "Cảm ơn bạn đã đặt hàng!"
  - "Món này hiện đã hết ạ."
  - "Thời gian giao hàng khoảng 30-45 phút."
  - "Bạn có thể liên hệ: 1900-xxxx"
- Chủ shop nhấn vào một tin mẫu → Tin nhắn tự động gửi đi

**Bước 11: Nhận tin nhắn từ màn hình khác**
- Chủ shop đang ở màn hình Dashboard
- Có tin nhắn mới từ khách hàng khác
- Push notification hiển thị:
  - "Tin nhắn mới từ Trần Thị B"
  - "Cho mình hỏi voucher GIAM50K còn dùng được không?"
- Badge trên icon "Tin nhắn" tăng lên (5 → 6)
- Chủ shop nhấn vào notification → Mở trực tiếp cuộc hội thoại với Trần Thị B

**Bước 12: Tìm kiếm cuộc hội thoại**
- Quay lại `OwnerConversationsScreen`
- Chủ shop nhấn vào icon tìm kiếm
- Gõ tên: "Nguyễn"
- Hệ thống lọc và hiển thị tất cả cuộc hội thoại với khách có tên chứa "Nguyễn"

**Bước 13: Đánh dấu tất cả đã đọc**
- Chủ shop nhấn vào icon "..." ở góc trên
- Menu hiển thị:
  - Đánh dấu tất cả đã đọc
  - Cài đặt thông báo
- Chủ shop chọn "Đánh dấu tất cả đã đọc"
- Tất cả badge số tin chưa đọc biến mất
- Toast: "Đã đánh dấu tất cả đã đọc"

### Screenshot cần chụp
- [ ] `OwnerConversationsScreen`: Danh sách cuộc hội thoại
- [ ] `OwnerConversationsScreen`: Badge số tin chưa đọc
- [ ] `OwnerConversationsScreen`: 2 tabs (Tất cả / Chưa đọc)
- [ ] `OwnerChatDetailScreen`: Giao diện chat với tin nhắn 2 bên
- [ ] `OwnerChatDetailScreen`: Header với avatar, tên, hạng khách
- [ ] Bottom sheet thông tin nhanh khách hàng
- [ ] `OwnerChatDetailScreen`: Optimistic UI - tin nhắn vừa gửi với icon đồng hồ
- [ ] `OwnerChatDetailScreen`: Tin nhắn đã gửi (icon tick đơn)
- [ ] `OwnerChatDetailScreen`: Tin nhắn đã đọc (icon double tick xanh)
- [ ] Quick Replies - danh sách tin nhắn mẫu
- [ ] Push notification tin nhắn mới
- [ ] Ô tìm kiếm cuộc hội thoại

---

## GHI CHÚ CHO BÁO CÁO

### Các điểm kỹ thuật nổi bật cần nhấn mạnh:

1. **Dashboard tổng quan**: Cung cấp cái nhìn toàn diện về hoạt động kinh doanh với dữ liệu thời gian thực.

2. **Quản lý sản phẩm**: Upload nhiều ảnh, bật/tắt nhanh, lọc theo danh mục giúp quản lý menu hiệu quả.

3. **Xử lý đơn hàng theo luồng**: 6 trạng thái rõ ràng (Pending → Confirmed → Preparing → Ready → Shipping → Completed) giúp theo dõi đơn hàng một cách có hệ thống.

4. **Duyệt Shipper**: Xem CCCD và bằng lái với khả năng zoom, đảm bảo tính minh bạch và an toàn.

5. **Voucher linh hoạt**: Hỗ trợ nhiều loại voucher (giảm %, giảm trực tiếp, free ship) với thống kê sử dụng chi tiết.

6. **Phân tích doanh thu**: Biểu đồ trực quan, so sánh xu hướng, top sản phẩm giúp ra quyết định kinh doanh.

7. **Buyer Tiers**: Phân loại khách hàng theo hạng (Bronze, Silver, Gold, Diamond) để xây dựng chiến lược chăm sóc phù hợp.

8. **Optimistic UI trong Chat**: Tin nhắn hiển thị ngay lập tức, tạo trải nghiệm tương tác mượt mà.

9. **Realtime Updates**: Sử dụng Firestore để cập nhật dữ liệu theo thời gian thực (đơn hàng mới, tin nhắn, thông báo).

10. **Thông báo FCM cho Shipper**: Gửi thông báo ORDER_READY đến tất cả shipper online qua topic subscription.

---

**Ngày tạo:** 01/02/2026  
**Mục đích:** Tài liệu chi tiết chức năng chủ cửa hàng cho báo cáo đại học  
**Bước tiếp theo:** Chụp screenshots theo danh sách đã đánh dấu
