import { Injectable, Logger } from '@nestjs/common';
import { MailerService } from '@nestjs-modules/mailer';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class MailService {
  private readonly logger = new Logger(MailService.name);

  constructor(
    private readonly mailerService: MailerService,
    private readonly configService: ConfigService,
  ) {}

  /**
   * Gửi email OTP xác thực
   */
  async sendOtpEmail(to: string, otpCode: string): Promise<boolean> {
    try {
      const appName = this.configService.get<string>('APP_NAME') || 'Ứng dụng của bạn';
      const expiryMinutes = this.configService.get<number>('OTP_EXPIRY_MINUTES') || 15;

      await this.mailerService.sendMail({
        to,
        subject: `Mã xác thực OTP - ${appName}`,
        text: `Mã OTP của bạn: ${otpCode}\nHiệu lực: ${expiryMinutes} phút\n\nKhông chia sẻ mã này với bất kỳ ai.`,
        html: `
          <div style="font-family: Arial, sans-serif; padding: 20px;">
            <h2>Mã xác thực OTP - ${appName}</h2>
            <p>Mã OTP của bạn là: <strong style="font-size: 24px;">${otpCode}</strong></p>
            <p>Hiệu lực: <strong>${expiryMinutes} phút</strong></p>
            <p style="color: red; font-weight: bold;">⚠️ Không chia sẻ mã này với bất kỳ ai.</p>
            <hr>
            <p>${appName}</p>
          </div>
        `,
      });

      this.logger.log(`✅ OTP email sent to ${to}`);
      return true;
    } catch (error) {
      this.logger.error(`❌ Failed to send OTP email to ${to}: ${error.message}`);
      return false;
    }
  }
}