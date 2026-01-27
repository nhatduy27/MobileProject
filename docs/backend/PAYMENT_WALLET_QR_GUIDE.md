# Hướng Dẫn Integration: Payment QR & Wallet Payout QR Flow

> **Version**: 1.0  
> **Last Updated**: 27/01/2026  
> **Status**: ✅ Tested & Working

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Flow 1: SePay Payment QR (Customer Payment)](#flow-1-sepay-payment-qr-customer-payment)
3. [Flow 2: Wallet Payout QR (Owner Withdrawal)](#flow-2-wallet-payout-qr-owner-withdrawal)
4. [API Reference](#api-reference)
5. [Error Handling](#error-handling)
6. [Testing Guide](#testing-guide)

---

## Tổng Quan

Hệ thống hỗ trợ 2 loại QR code payment flows:

### 1. **Payment QR Flow** (Customer → Shop)

- Customer đặt hàng và chọn thanh toán bằng SePay
- Hệ thống tạo QR code cho customer quét
- Customer chuyển tiền qua ngân hàng
- Backend tự động verify và cập nhật trạng thái đơn hàng

### 2. **Payout QR Flow** (Admin → Owner/Shipper)

- Owner/Shipper yêu cầu rút tiền từ ví
- Admin duyệt yêu cầu và nhận QR code
- Admin chuyển tiền cho Owner/Shipper
- Admin verify chuyển khoản thành công
- Backend tự động trừ tiền trong ví

---

## Flow 1: SePay Payment QR (Customer Payment)

### 📱 UI Flow cho Customer

```
Giỏ hàng → Chọn shop → Checkout → Chọn "SePay" →
Tạo order → Nhận QR code → Quét & chuyển tiền →
Chờ verify (tự động) → Đơn hàng PAID
```

### 🔧 Implementation Steps

#### **Step 1: Thêm sản phẩm vào giỏ hàng**

```http
POST /api/cart/items
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json

{
  "productId": "prod_123",
  "quantity": 2
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "cart_customer_xyz",
    "groups": [
      {
        "shopId": "shop_123",
        "shopName": "Quán Phở Việt",
        "items": [...],
        "subtotal": 70000
      }
    ]
  }
}
```

#### **Step 2: Tạo đơn hàng với phương thức SEPAY**

```http
POST /api/orders
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json

{
  "shopId": "shop_123",
  "paymentMethod": "SEPAY",
  "deliveryAddress": {
    "fullAddress": "KTX Khu B - Tòa B5",
    "building": "B5",
    "room": "101"
  }
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": "order_abc123",
    "orderNumber": "ORD1769519651697",
    "status": "PENDING",
    "paymentStatus": "UNPAID",
    "paymentMethod": "SEPAY",
    "total": 85000
  }
}
```

**⚠️ Lưu ý:**

- `orderId` sẽ dùng để tạo payment ở bước tiếp theo
- Order status: `PENDING`, Payment status: `UNPAID`

#### **Step 3: Tạo payment và nhận QR code**

```http
POST /api/orders/{orderId}/payment
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json

{
  "method": "SEPAY"
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "message": "Payment created successfully",
    "payment": {
      "id": "payment_xyz789",
      "orderId": "order_abc123",
      "amount": 85000,
      "method": "SEPAY",
      "status": "PROCESSING",
      "providerData": {
        "sepayContent": "KTXORD1769519651697M5NNG6",
        "qrCodeUrl": "https://qr.sepay.vn/img?acc=00012112005000&bank=MB&amount=85000&des=KTXORD1769519651697M5NNG6&template=compact",
        "accountNumber": "00012112005000",
        "accountName": "TONG DUONG THAI HOA",
        "bankCode": "MB",
        "amount": 85000
      }
    }
  }
}
```

**📱 UI Actions:**

1. Extract `qrCodeUrl` từ response
2. Hiển thị QR code cho customer (dùng Image component hoặc QR library)
3. Hiển thị thông tin chuyển khoản:
   - Số tiền: `85,000đ`
   - Ngân hàng: `MB (MBBank)`
   - Số tài khoản: `00012112005000`
   - Nội dung CK: `KTXORD1769519651697M5NNG6`

**💡 Gợi ý UI:**

```jsx
// React example
<div className="payment-qr">
  <img src={payment.providerData.qrCodeUrl} alt="QR Code" />
  <div className="payment-info">
    <p>Số tiền: {payment.amount.toLocaleString()}đ</p>
    <p>Ngân hàng: {payment.providerData.bankCode}</p>
    <p>Nội dung: {payment.providerData.sepayContent}</p>
  </div>
  <button onClick={startPolling}>Tôi đã chuyển khoản</button>
</div>
```

#### **Step 4: Polling để verify payment**

Sau khi customer nhấn "Tôi đã chuyển khoản", bắt đầu polling:

```http
POST /api/orders/{orderId}/payment/verify
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json
```

**Response khi chưa verify:**

```json
{
  "success": true,
  "data": {
    "matched": false,
    "payment": {
      "status": "PROCESSING",
      ...
    }
  }
}
```

**Response khi đã verify thành công:**

```json
{
  "success": true,
  "data": {
    "matched": true,
    "payment": {
      "status": "PAID",
      "paidAt": "2026-01-27T13:14:12.627Z",
      ...
    }
  }
}
```

**📱 Polling Logic:**

```javascript
// JavaScript/TypeScript example
const pollPaymentVerification = async (orderId) => {
  const maxAttempts = 40; // 40 lần
  const interval = 3000; // 3 giây

  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await fetch(`/api/orders/${orderId}/payment/verify`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      const result = await response.json();

      if (result.data.matched) {
        // ✅ Payment verified!
        showSuccess("Thanh toán thành công!");
        navigateToOrderDetail(orderId);
        return true;
      }

      // Chưa verify, đợi 3 giây rồi thử lại
      await new Promise((resolve) => setTimeout(resolve, interval));
    } catch (error) {
      console.error("Polling error:", error);
      // Tiếp tục polling nếu có lỗi network tạm thời
    }
  }

  // Timeout sau 2 phút (40 * 3s = 120s)
  showWarning("Chưa nhận được xác nhận. Vui lòng kiểm tra lại sau.");
  return false;
};
```

**⏱️ Thời gian verify:**

- Trung bình: 60-90 giây
- Tối đa: 2 phút (40 attempts × 3 seconds)
- Test case đã chạy: 66 giây (22 attempts)

#### **Step 5: Hiển thị kết quả**

Sau khi `matched: true`:

1. **Cập nhật UI:**
   - Ẩn QR code
   - Hiển thị icon success ✅
   - Message: "Thanh toán thành công!"

2. **Fetch order details:**

   ```http
   GET /api/orders/{orderId}
   Authorization: Bearer {CUSTOMER_TOKEN}
   ```

3. **Kiểm tra trạng thái:**
   - `paymentStatus`: `"PAID"` ✅
   - `status`: `"PENDING"` (chờ shop xác nhận)

4. **Navigate:** Chuyển đến trang chi tiết đơn hàng

---

## Flow 2: Wallet Payout QR (Owner Withdrawal)

### 📱 UI Flow cho Owner & Admin

#### **Owner Side:**

```
Ví → Yêu cầu rút tiền → Nhập thông tin TK ngân hàng →
Submit → Chờ admin duyệt → Nhận tiền
```

#### **Admin Side:**

```
Danh sách yêu cầu rút tiền → Duyệt → Nhận QR code →
Chuyển tiền → Verify → Hoàn tất
```

### 🔧 Implementation Steps

#### **OWNER: Step 1 - Kiểm tra số dư ví**

```http
GET /api/wallets/me
Authorization: Bearer {OWNER_TOKEN}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "wallet": {
      "id": "wallet_owner_xyz",
      "type": "OWNER",
      "balance": 1000000,
      "totalEarned": 1200000,
      "totalWithdrawn": 200000,
      "createdAt": "2026-01-20T10:00:00.000Z"
    }
  }
}
```

**💡 UI Display:**

```jsx
<div className="wallet-balance">
  <h3>Số dư khả dụng</h3>
  <p className="balance">{wallet.balance.toLocaleString()}đ</p>
  <p className="subtitle">Đã rút: {wallet.totalWithdrawn.toLocaleString()}đ</p>
  <button disabled={wallet.balance < 100000} onClick={handleRequestPayout}>
    Rút tiền (Tối thiểu 100,000đ)
  </button>
</div>
```

#### **OWNER: Step 2 - Tạo yêu cầu rút tiền**

```http
POST /api/wallets/payout
Authorization: Bearer {OWNER_TOKEN}
Content-Type: application/json

{
  "amount": 100000,
  "bankCode": "Vietinbank",
  "accountNumber": "108872766870",
  "accountName": "NGUYEN VAN A"
}
```

**Validation Rules:**

- `amount`: Số nguyên, >= 100,000đ, <= balance
- `bankCode`: String, tên ngân hàng (Vietinbank, VCB, ACB, ...)
- `accountNumber`: String, số tài khoản ngân hàng
- `accountName`: String, tên chủ tài khoản (viết hoa, không dấu)

**Response:**

```json
{
  "success": true,
  "data": {
    "message": "Payout request submitted successfully",
    "payoutRequest": {
      "id": "payout_abc123",
      "amount": 100000,
      "status": "PENDING",
      "bankCode": "Vietinbank",
      "accountNumber": "108872766870",
      "accountName": "NGUYEN VAN A",
      "createdAt": "2026-01-27T13:19:51.191Z"
    }
  }
}
```

**📱 UI Actions:**

1. Hiển thị modal xác nhận
2. Sau khi submit thành công:
   - Show notification: "Yêu cầu rút tiền đã được gửi"
   - Navigate về màn hình lịch sử rút tiền
   - Status: `PENDING` (Chờ duyệt)

#### **ADMIN: Step 3 - Xem danh sách yêu cầu rút tiền**

```http
GET /api/admin/payouts?status=PENDING&page=1&limit=20
Authorization: Bearer {ADMIN_TOKEN}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "payouts": [
      {
        "id": "payout_abc123",
        "userId": "owner_xyz",
        "walletType": "OWNER",
        "amount": 100000,
        "bankCode": "Vietinbank",
        "accountNumber": "108872766870",
        "accountName": "NGUYEN VAN A",
        "status": "PENDING",
        "createdAt": "2026-01-27T13:19:51.191Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "totalItems": 5,
      "totalPages": 1
    }
  }
}
```

**📱 UI Display:**

```jsx
<table className="payout-requests">
  <thead>
    <tr>
      <th>User</th>
      <th>Số tiền</th>
      <th>Ngân hàng</th>
      <th>Trạng thái</th>
      <th>Thao tác</th>
    </tr>
  </thead>
  <tbody>
    {payouts.map((payout) => (
      <tr key={payout.id}>
        <td>{payout.accountName}</td>
        <td>{payout.amount.toLocaleString()}đ</td>
        <td>
          {payout.bankCode} - {payout.accountNumber}
        </td>
        <td>
          <Badge status={payout.status} />
        </td>
        <td>
          <Button onClick={() => handleApprove(payout.id)}>Duyệt</Button>
          <Button onClick={() => handleReject(payout.id)}>Từ chối</Button>
        </td>
      </tr>
    ))}
  </tbody>
</table>
```

#### **ADMIN: Step 4 - Duyệt yêu cầu và nhận QR code**

```http
POST /api/admin/payouts/{payoutId}/approve
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json
```

**Response:**

```json
{
  "success": true,
  "data": {
    "message": "Payout đã được approve",
    "payout": {
      "id": "payout_abc123",
      "amount": 100000,
      "status": "APPROVED",
      "bankCode": "Vietinbank",
      "accountNumber": "108872766870",
      "accountName": "NGUYEN VAN A",
      "approvedBy": "admin_123",
      "approvedAt": "2026-01-27T13:20:00.000Z"
    },
    "qrUrl": "https://qr.sepay.vn/img?acc=108872766870&bank=Vietinbank&amount=100000&des=PAYOUTABC123AB&template=compact"
  }
}
```

**📱 UI Actions:**

1. Extract `qrUrl` từ response
2. Hiển thị QR code trong modal:

```jsx
<Modal title="Chuyển tiền cho Owner" onClose={handleClose}>
  <div className="payout-qr">
    <img src={qrUrl} alt="Payout QR Code" />

    <div className="transfer-info">
      <h4>Thông tin chuyển khoản</h4>
      <p>Số tiền: {payout.amount.toLocaleString()}đ</p>
      <p>Ngân hàng: {payout.bankCode}</p>
      <p>Số TK: {payout.accountNumber}</p>
      <p>Tên TK: {payout.accountName}</p>
      <p className="highlight">
        Nội dung: {payout.id.substring(0, 8).toUpperCase()}
      </p>
    </div>

    <Alert type="warning">
      ⚠️ Vui lòng quét QR hoặc nhập chính xác nội dung chuyển khoản
    </Alert>

    <div className="actions">
      <Button onClick={() => startVerifying(payout.id)}>
        Tôi đã chuyển khoản
      </Button>
      <Button variant="secondary" onClick={copyQRUrl}>
        Copy QR URL
      </Button>
    </div>
  </div>
</Modal>
```

**⚠️ Quan trọng:**

- Nội dung CK phải khớp với format: `PAYOUT{payoutId_first_8_chars}`
- Ví dụ: payoutId = `abc123def456` → Content = `PAYOUTABC123AB`
- Admin phải giữ chính xác nội dung này khi chuyển tiền

#### **ADMIN: Step 5 - Verify chuyển khoản**

Sau khi admin đã chuyển tiền xong, click "Tôi đã chuyển khoản":

```http
POST /api/admin/payouts/{payoutId}/verify
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json
```

**Response khi chưa phát hiện giao dịch:**

```json
{
  "success": true,
  "data": {
    "matched": false,
    "status": "APPROVED",
    "payout": {
      "id": "payout_abc123",
      "status": "APPROVED",
      ...
    }
  }
}
```

**Response khi đã phát hiện giao dịch:**

```json
{
  "success": true,
  "data": {
    "matched": true,
    "status": "TRANSFERRED",
    "payout": {
      "id": "payout_abc123",
      "status": "TRANSFERRED",
      "transferredBy": "SYSTEM_AUTO",
      "transferredAt": "2026-01-27T13:31:23.917Z",
      "transferNote": "Auto-verified by admin admin_123",
      ...
    }
  }
}
```

**📱 Polling Logic:**

```javascript
const verifyPayoutTransfer = async (payoutId) => {
  const maxAttempts = 20;
  const interval = 5000; // 5 giây

  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await fetch(`/api/admin/payouts/${payoutId}/verify`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${adminToken}`,
          "Content-Type": "application/json",
        },
      });

      const result = await response.json();

      if (result.data.matched) {
        // ✅ Transfer verified!
        showSuccess("Chuyển tiền thành công!");

        // Reload danh sách payouts
        refreshPayoutList();

        // Close modal
        closeModal();
        return true;
      }

      // Update UI: Đang kiểm tra... (Lần {i+1}/{maxAttempts})
      updatePollingStatus(i + 1, maxAttempts);

      await new Promise((resolve) => setTimeout(resolve, interval));
    } catch (error) {
      console.error("Verify error:", error);
    }
  }

  // Timeout
  showWarning("Chưa phát hiện giao dịch. Vui lòng kiểm tra lại sau vài phút.");
  return false;
};
```

**⏱️ Thời gian verify:**

- SePay API có thể delay 1-2 phút
- Polling: 20 lần × 5 giây = 100 giây (1 phút 40 giây)
- Nếu timeout: Admin có thể thử verify lại sau

#### **Alternative: Manual Mark as Transferred**

Nếu không muốn dùng auto-verify, admin có thể mark manually:

```http
POST /api/admin/payouts/{payoutId}/transferred
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "transferNote": "Đã chuyển khoản lúc 13:30, mã GD: ABC123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Đã đánh dấu payout là transferred"
}
```

#### **OWNER: Step 6 - Kiểm tra kết quả**

Owner có thể xem lịch sử rút tiền:

```http
GET /api/wallets/payout-history?page=1&limit=10
Authorization: Bearer {OWNER_TOKEN}
```

Hoặc kiểm tra số dư ví:

```http
GET /api/wallets/me
Authorization: Bearer {OWNER_TOKEN}
```

**Sau khi TRANSFERRED:**

- `balance` sẽ giảm đi số tiền đã rút
- `totalWithdrawn` tăng lên
- Payout status: `TRANSFERRED` ✅

---

## API Reference

### Payment APIs

#### Create Payment

```
POST /api/orders/{orderId}/payment
Auth: Customer
Body: { method: "SEPAY" | "COD" }
Response: { payment, qrCodeUrl (if SEPAY) }
```

#### Verify Payment

```
POST /api/orders/{orderId}/payment/verify
Auth: Customer
Response: { matched: boolean, payment }
```

### Wallet APIs (Owner/Shipper)

#### Get Wallet Balance

```
GET /api/wallets/me
Auth: Owner/Shipper
Response: { wallet: { balance, totalEarned, totalWithdrawn } }
```

#### Request Payout

```
POST /api/wallets/payout
Auth: Owner/Shipper
Body: { amount, bankCode, accountNumber, accountName }
Response: { payoutRequest }
```

#### Get Payout History

```
GET /api/wallets/payout-history?page=1&limit=10
Auth: Owner/Shipper
Response: { payouts, pagination }
```

### Admin Payout APIs

#### List Payout Requests

```
GET /api/admin/payouts?status=PENDING&page=1&limit=20
Auth: Admin
Response: { payouts, pagination }
```

#### Approve Payout

```
POST /api/admin/payouts/{payoutId}/approve
Auth: Admin
Response: { payout, qrUrl }
```

#### Verify Payout Transfer

```
POST /api/admin/payouts/{payoutId}/verify
Auth: Admin
Response: { matched: boolean, status, payout }
```

#### Reject Payout

```
POST /api/admin/payouts/{payoutId}/reject
Auth: Admin
Body: { reason: string }
Response: { message }
```

#### Manual Mark as Transferred

```
POST /api/admin/payouts/{payoutId}/transferred
Auth: Admin
Body: { transferNote: string }
Response: { message }
```

---

## Error Handling

### Common Errors

#### Payment Errors

**PAYMENT_001**: Order not found

```json
{
  "success": false,
  "message": "Order not found",
  "errorCode": "PAYMENT_001",
  "statusCode": 404
}
```

**PAYMENT_003**: Payment already exists

```json
{
  "success": false,
  "message": "Payment already exists for this order",
  "errorCode": "PAYMENT_003",
  "statusCode": 409
}
```

**Xử lý UI:**

```javascript
try {
  const response = await createPayment(orderId);
  // ...
} catch (error) {
  if (error.errorCode === "PAYMENT_003") {
    // Payment đã tồn tại, fetch payment hiện tại
    const payment = await getExistingPayment(orderId);
    showQRCode(payment);
  } else {
    showError(error.message);
  }
}
```

#### Wallet Errors

**WALLET_001**: Insufficient balance

```json
{
  "success": false,
  "message": "Insufficient balance",
  "errorCode": "WALLET_001"
}
```

**WALLET_002**: Below minimum payout amount

```json
{
  "success": false,
  "message": "Amount must be at least 100,000đ",
  "errorCode": "WALLET_002"
}
```

**Xử lý UI:**

```javascript
const handlePayout = async (amount) => {
  if (amount < 100000) {
    showError("Số tiền rút tối thiểu là 100,000đ");
    return;
  }

  if (amount > wallet.balance) {
    showError("Số dư không đủ");
    return;
  }

  try {
    await requestPayout({ amount, ... });
    showSuccess("Yêu cầu rút tiền đã được gửi");
  } catch (error) {
    handlePayoutError(error);
  }
};
```

#### Admin Payout Errors

**PAYOUT_001**: Payout not found
**PAYOUT_002**: Payout already processed
**PAYOUT_003**: Invalid status transition

```javascript
const handleVerifyError = (error) => {
  switch (error.errorCode) {
    case "PAYOUT_002":
      showInfo("Payout đã được xử lý rồi");
      refreshPayoutList();
      break;
    case "PAYOUT_003":
      showError("Trạng thái không hợp lệ. Vui lòng refresh trang.");
      break;
    default:
      showError(error.message);
  }
};
```

---

## Testing Guide

### Test Accounts

Xem file [TEST_ACCOUNTS.md](./TEST_ACCOUNTS.md) để lấy test accounts.

### Test Data Setup

#### 1. Customer có đơn hàng test:

```javascript
// Add product to cart
POST /api/cart/items
{ productId: "JCbTdpl1d7zbRA7nYHcm", quantity: 1 }

// Create order
POST /api/orders
{
  shopId: "nzIfau9GtqIPyWkmLyku",
  paymentMethod: "SEPAY",
  deliveryAddress: { fullAddress: "KTX Khu B - Tòa B5" }
}
```

#### 2. Owner có số dư ví để test:

```javascript
// Check balance first
GET / api / wallets / me;

// If balance > 100,000đ, can test payout
// If balance = 0, cần tạo đơn hàng để tạo doanh thu cho shop
```

### Testing Scenarios

#### Scenario 1: Happy Path - Payment QR

1. ✅ Customer tạo order với SEPAY
2. ✅ Tạo payment → Nhận QR URL
3. ✅ Quét QR bằng app ngân hàng thực
4. ✅ Chuyển tiền với đúng số tiền và nội dung
5. ✅ Poll verify → Nhận matched=true sau ~60-90s
6. ✅ Order payment status: PAID

#### Scenario 2: Happy Path - Payout QR

1. ✅ Owner có balance >= 100,000đ
2. ✅ Owner request payout
3. ✅ Admin approve → Nhận QR URL
4. ✅ Admin quét QR và chuyển tiền
5. ✅ Admin click verify → matched=true
6. ✅ Payout status: TRANSFERRED
7. ✅ Owner balance giảm đúng số tiền

#### Scenario 3: Edge Cases

**Test timeout scenario:**

- Tạo payment nhưng không chuyển tiền
- Poll 40 lần → Timeout
- UI hiển thị: "Chưa nhận được xác nhận"

**Test wrong content scenario:**

- Chuyển tiền nhưng sai nội dung
- Poll không bao giờ match
- Cần contact support

**Test duplicate payment:**

- Tạo payment cho order đã có payment
- Nhận error PAYMENT_003
- UI xử lý: Fetch payment cũ và hiển thị

### Mock Testing (Development)

Nếu chưa có SePay account để test, có thể:

1. **Mock QR URL:**

   ```javascript
   const mockQRUrl = "https://via.placeholder.com/300?text=Mock+QR+Code";
   ```

2. **Mock verify response:**

   ```javascript
   // Giả lập verify thành công sau 10 giây
   setTimeout(() => {
     setPaymentStatus("PAID");
   }, 10000);
   ```

3. **Test với COD thay vì SEPAY:**
   - COD instant PAID, không cần polling
   - Dùng để test các flow khác

---

## Important Notes

### ⚠️ Production Checklist

- [ ] Đã test QR flow trên staging với SePay account thật
- [ ] Đã test polling timeout scenarios
- [ ] Đã test error handling cho tất cả error codes
- [ ] UI hiển thị loading state khi đang poll
- [ ] UI hiển thị progress hoặc số lần thử (optional)
- [ ] Có fallback nếu user close app giữa chừng
- [ ] Log events cho analytics (payment_created, payment_verified, etc.)

### 🔒 Security Notes

- Không bao giờ hardcode tokens trong code
- Store tokens securely (secure storage trên mobile)
- Validate input trước khi call API
- Handle sensitive data (bank account) cẩn thận

### 🚀 Performance Tips

- Cache wallet balance trong 30s-1 phút
- Debounce request payout button (tránh double submit)
- Cancel polling nếu user navigate away
- Sử dụng AbortController cho fetch requests

### 📱 UX Recommendations

**Payment QR Screen:**

- Hiển thị countdown timer (2 phút)
- Show loading spinner khi đang poll
- Thêm button "Tôi gặp vấn đề" → Contact support
- Cho phép copy thông tin chuyển khoản

**Payout Admin Screen:**

- Highlight các payout PENDING (màu vàng/cam)
- Show timestamp để biết yêu cầu nào cũ nhất
- Filter/search theo status, amount, user
- Bulk approve (tùy chọn)

---

## Support

### FAQ

**Q: Tại sao payment verify mất 1-2 phút?**  
A: SePay API cần thời gian sync transaction từ ngân hàng. Thời gian trung bình 60-90 giây.

**Q: Nếu user close app giữa chừng polling thì sao?**  
A: User có thể quay lại màn hình order detail và retry verify. Payment vẫn ở trạng thái PROCESSING.

**Q: Admin có thể verify payout nhiều lần không?**  
A: Có. Endpoint verify là idempotent. Nếu đã TRANSFERRED thì return luôn status đó.

**Q: Số tiền rút tối thiểu/tối đa là bao nhiêu?**  
A: Tối thiểu 100,000đ. Tối đa = số dư ví hiện tại.

### Contact

- Backend Team: [Backend Issues](../README.md)
- API Documentation: [OpenAPI Spec](../../common/OPENAPI.md)
- Database Schema: [Database Docs](../database/)

---

**Version History:**

- v1.0 (27/01/2026): Initial release - Payment & Payout QR flows tested & documented
