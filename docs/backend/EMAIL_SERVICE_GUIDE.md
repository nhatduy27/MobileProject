# Hướng dẫn cấu hình Email Service (Brevo)

## Tổng quan

KTX Delivery sử dụng **Brevo** (trước đây là Sendinblue) làm email service chính để gửi:
- OTP xác thực email
- OTP đặt lại mật khẩu  
- Email chào mừng

## Tại sao chọn Brevo?

| Tiêu chí | Brevo | SendGrid |
|----------|-------|----------|
| Free tier | 300 emails/ngày | 100 emails/ngày |
| Deliverability | ✅ Tốt hơn | ⚠️ Dễ bị spam |
| Setup | ✅ Đơn giản | ⚠️ Phức tạp |
| Spam rate | ✅ Thấp | ⚠️ Cao (shared IP) |

## Hướng dẫn cấu hình

### Bước 1: Đăng ký tài khoản Brevo

1. Truy cập https://app.brevo.com/account/register
2. Đăng ký bằng email hoặc Google
3. Verify email xác nhận

### Bước 2: Hoàn tất thông tin

Sau khi đăng ký, điền:
- **Company name**: Tên dự án (vd: KTX Delivery)
- **Industry**: Food & Beverage hoặc Technology
- **Team size**: Chọn phù hợp

### Bước 3: Lấy API Key

1. Vào **Settings** (⚙️) → **SMTP & API**
2. Chọn tab **API Keys**
3. Click **"Generate a new API key"**
4. **Copy API key** ngay (chỉ hiển thị 1 lần!)

### Bước 4: Verify Sender Email

1. Vào **Settings** → **Senders & Domains**
2. Click **"Add a sender"**
3. Nhập email gửi (vd: `noreply@ktxdelivery.com`)
4. Check inbox và click link xác nhận

### Bước 5: Cấu hình .env

```env
EMAIL_PROVIDER=brevo

BREVO_API_KEY=xkeysib-xxxxxxxxxxxxxxxxx
BREVO_FROM_EMAIL=your-verified-email@domain.com
BREVO_FROM_NAME=KTX Delivery
```

## Chuyển đổi giữa Brevo và SendGrid

Hệ thống hỗ trợ cả 2 provider. Chỉ cần thay đổi biến `EMAIL_PROVIDER`:

```env
# Dùng Brevo (khuyến nghị)
EMAIL_PROVIDER=brevo

# Dùng SendGrid (fallback)
EMAIL_PROVIDER=sendgrid
```

## Tips tránh email bị spam

1. **Subject line**: Dùng tiếng Việt, tránh từ "OTP", "URGENT"
   - ✅ "Mã xác thực KTX Delivery"
   - ❌ "OTP Code", "Verify Now"

2. **Verify domain** (optional nhưng khuyến nghị):
   - Settings → Senders & Domains → Add domain
   - Thêm DNS records (SPF, DKIM)

3. **Rate limiting**: Không gửi quá nhiều email đột ngột

## Kiểm tra hoạt động

```bash
# Start backend
npm run start:dev

# Test gửi OTP
curl -X POST http://localhost:3000/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email": "your-email@gmail.com"}'
```

## Xử lý sự cố

### Email không nhận được?
1. Check folder Spam
2. Verify sender email đã xác thực
3. Kiểm tra Brevo Dashboard → Logs

### API key không hoạt động?
1. Tạo API key mới
2. Đảm bảo copy đúng key (không có space thừa)

### Lỗi "sender not verified"?
1. Vào Brevo → Settings → Senders
2. Verify email gửi
