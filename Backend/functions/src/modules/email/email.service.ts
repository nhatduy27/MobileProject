import { Injectable, Logger } from '@nestjs/common';
import sgMail from '@sendgrid/mail';

type EmailProvider = 'brevo' | 'sendgrid';

/**
 * Email Service
 *
 * Handles email sending via Brevo (primary) or SendGrid (fallback).
 * Used for OTP verification, password reset, notifications, etc.
 */
@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);
  private readonly provider: EmailProvider;
  private readonly fromEmail: string;
  private readonly fromName: string;

  // Brevo config
  private readonly brevoApiKey?: string;

  constructor() {
    // Determine which provider to use
    this.provider = (process.env.EMAIL_PROVIDER as EmailProvider) || 'brevo';

    if (this.provider === 'brevo') {
      this.brevoApiKey = process.env.BREVO_API_KEY;
      this.fromEmail = process.env.BREVO_FROM_EMAIL || 'noreply@ktxdelivery.com';
      this.fromName = process.env.BREVO_FROM_NAME || 'KTX Delivery';

      if (!this.brevoApiKey) {
        this.logger.warn('BREVO_API_KEY not configured. Email sending will fail.');
      } else {
        this.logger.log('Brevo email provider initialized successfully');
      }
    } else {
      // SendGrid fallback
      const sgApiKey = process.env.SENDGRID_API_KEY;
      this.fromEmail = process.env.SENDGRID_FROM_EMAIL || 'noreply@ktxdelivery.com';
      this.fromName = process.env.SENDGRID_FROM_NAME || 'KTX Delivery';

      if (!sgApiKey) {
        this.logger.warn('SENDGRID_API_KEY not configured. Email sending will fail.');
      } else {
        sgMail.setApiKey(sgApiKey);
        this.logger.log('SendGrid email provider initialized successfully');
      }
    }
  }

  /**
   * Send OTP email for email verification
   */
  async sendEmailVerificationOTP(email: string, code: string): Promise<void> {
    const subject = 'Xác thực email - KTX Delivery';
    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
          .container { max-width: 600px; margin: 0 auto; padding: 20px; }
          .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
          .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
          .otp-code { font-size: 32px; font-weight: bold; color: #667eea; text-align: center; letter-spacing: 5px; margin: 20px 0; padding: 15px; background: white; border-radius: 8px; border: 2px dashed #667eea; }
          .warning { color: #e74c3c; font-size: 14px; margin-top: 20px; }
          .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #999; }
        </style>
      </head>
      <body>
        <div class="container">
          <div class="header">
            <h1>🍜 KTX Delivery</h1>
            <p>Xác thực tài khoản của bạn</p>
          </div>
          <div class="content">
            <h2>Xin chào!</h2>
            <p>Bạn đã yêu cầu xác thực email cho tài khoản KTX Delivery.</p>
            <p>Vui lòng sử dụng mã OTP sau để hoàn tất xác thực:</p>
            
            <div class="otp-code">${code}</div>
            
            <p><strong>Mã OTP có hiệu lực trong 5 phút.</strong></p>
            
            <div class="warning">
              ⚠️ Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
              Không chia sẻ mã OTP với bất kỳ ai!
            </div>
          </div>
          <div class="footer">
            <p>© 2026 KTX Delivery. All rights reserved.</p>
            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
          </div>
        </div>
      </body>
      </html>
    `;

    await this.sendEmail(email, subject, html);
  }

  /**
   * Send password reset OTP
   */
  async sendPasswordResetOTP(email: string, code: string): Promise<void> {
    const subject = 'Đặt lại mật khẩu - KTX Delivery';
    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
          .container { max-width: 600px; margin: 0 auto; padding: 20px; }
          .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
          .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
          .otp-code { font-size: 32px; font-weight: bold; color: #f5576c; text-align: center; letter-spacing: 5px; margin: 20px 0; padding: 15px; background: white; border-radius: 8px; border: 2px dashed #f5576c; }
          .warning { color: #e74c3c; font-size: 14px; margin-top: 20px; padding: 15px; background: #fff3cd; border-left: 4px solid #e74c3c; border-radius: 4px; }
          .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #999; }
        </style>
      </head>
      <body>
        <div class="container">
          <div class="header">
            <h1>🔐 Đặt lại mật khẩu</h1>
            <p>KTX Delivery</p>
          </div>
          <div class="content">
            <h2>Xin chào!</h2>
            <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản KTX Delivery.</p>
            <p>Vui lòng sử dụng mã OTP sau để tiếp tục:</p>
            
            <div class="otp-code">${code}</div>
            
            <p><strong>Mã OTP có hiệu lực trong 5 phút.</strong></p>
            
            <div class="warning">
              <strong>⚠️ CẢNH BÁO BẢO MẬT</strong><br>
              Nếu bạn KHÔNG yêu cầu đặt lại mật khẩu, tài khoản của bạn có thể đang bị truy cập trái phép. 
              Vui lòng đổi mật khẩu ngay lập tức!<br><br>
              Không bao giờ chia sẻ mã OTP với bất kỳ ai, kể cả nhân viên KTX Delivery.
            </div>
          </div>
          <div class="footer">
            <p>© 2026 KTX Delivery. All rights reserved.</p>
            <p>Email này được gửi tự động, vui lòng không trả lời.</p>
          </div>
        </div>
      </body>
      </html>
    `;

    await this.sendEmail(email, subject, html);
  }

  /**
   * Send welcome email after successful registration
   */
  async sendWelcomeEmail(email: string, displayName: string): Promise<void> {
    const subject = 'Chào mừng đến với KTX Delivery! 🎉';
    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="UTF-8">
        <style>
          body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
          .container { max-width: 600px; margin: 0 auto; padding: 20px; }
          .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; border-radius: 10px 10px 0 0; }
          .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
          .button { display: inline-block; padding: 15px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
          .features { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
          .feature { margin: 15px 0; }
          .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #999; }
        </style>
      </head>
      <body>
        <div class="container">
          <div class="header">
            <h1>🍜 Chào mừng đến với KTX Delivery!</h1>
          </div>
          <div class="content">
            <h2>Xin chào ${displayName}!</h2>
            <p>Cảm ơn bạn đã đăng ký tài khoản KTX Delivery. Chúng tôi rất vui được phục vụ bạn!</p>
            
            <div class="features">
              <h3>🌟 Bắt đầu với KTX Delivery:</h3>
              <div class="feature">✅ Đặt món ăn từ hàng trăm quán trong KTX</div>
              <div class="feature">🚚 Giao hàng nhanh chóng trong khuôn viên</div>
              <div class="feature">💰 Thanh toán linh hoạt, nhiều ưu đãi</div>
              <div class="feature">⭐ Đánh giá và chia sẻ trải nghiệm</div>
            </div>
            
            <p>Hãy bắt đầu khám phá ngay!</p>
          </div>
          <div class="footer">
            <p>© 2026 KTX Delivery. All rights reserved.</p>
            <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ support@ktxdelivery.com</p>
          </div>
        </div>
      </body>
      </html>
    `;

    await this.sendEmail(email, subject, html);
  }

  /**
   * Core email sending method - supports Brevo and SendGrid
   */
  private async sendEmail(to: string, subject: string, html: string): Promise<void> {
    if (this.provider === 'brevo') {
      await this.sendViaBrevo(to, subject, html);
    } else {
      await this.sendViaSendGrid(to, subject, html);
    }
  }

  /**
   * Send email via Brevo API
   */
  private async sendViaBrevo(to: string, subject: string, html: string): Promise<void> {
    if (!this.brevoApiKey) {
      this.logger.error('Brevo API key not configured');
      return;
    }

    try {
      const response = await fetch('https://api.brevo.com/v3/smtp/email', {
        method: 'POST',
        headers: {
          accept: 'application/json',
          'api-key': this.brevoApiKey,
          'content-type': 'application/json',
        },
        body: JSON.stringify({
          sender: {
            name: this.fromName,
            email: this.fromEmail,
          },
          to: [{ email: to }],
          subject,
          htmlContent: html,
        }),
      });

      if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`Brevo API error: ${response.status} - ${errorBody}`);
      }

      const result = (await response.json()) as { messageId?: string };
      this.logger.log(
        `Email sent successfully via Brevo to ${to}: ${subject} (messageId: ${result.messageId || 'N/A'})`,
      );
    } catch (error: any) {
      this.logger.error(`Failed to send email via Brevo to ${to}:`, error.message);

      // Don't throw error - email failure shouldn't break the flow
      if (process.env.NODE_ENV === 'production') {
        // TODO: Send to error tracking (Sentry, etc.)
      }
    }
  }

  /**
   * Send email via SendGrid (fallback)
   */
  private async sendViaSendGrid(to: string, subject: string, html: string): Promise<void> {
    try {
      const msg = {
        to,
        from: {
          email: this.fromEmail,
          name: this.fromName,
        },
        subject,
        html,
      };

      await sgMail.send(msg);
      this.logger.log(`Email sent successfully via SendGrid to ${to}: ${subject}`);
    } catch (error: any) {
      this.logger.error(
        `Failed to send email via SendGrid to ${to}:`,
        error.response?.body || error.message,
      );

      // Don't throw error - email failure shouldn't break the flow
      if (process.env.NODE_ENV === 'production') {
        // TODO: Send to error tracking (Sentry, etc.)
      }
    }
  }
}
