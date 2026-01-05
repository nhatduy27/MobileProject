import { Injectable, Logger, BadRequestException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { MailService } from '../mail/mail.service';
import { FirebaseConfig } from '../../firebase/firebase.config';
import { ISendOtpResponse, IVerifyOtpResponse, IVerificationStatus } from './interface/otp.interface';

@Injectable()
export class OtpService {
  private readonly logger = new Logger(OtpService.name);
  private readonly OTP_COLLECTION = 'otp_codes';
  private readonly EMAIL_VERIFICATIONS_COLLECTION = 'email_verifications';

  constructor(
    private readonly configService: ConfigService,
    private readonly mailService: MailService,
    private readonly firebase: FirebaseConfig,
  ) {}

  /**
   * Tạo mã OTP 6 số
   */
  private generateOtp(): string {
    return Math.floor(100000 + Math.random() * 900000).toString();
  }

  /**
   * Kiểm tra email hợp lệ
   */
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Tính thời gian hết hạn
   */
  private getExpiryTime(): Date {
    const expiryMinutes = this.configService.get<number>('OTP_EXPIRY_MINUTES') || 15;
    return new Date(Date.now() + expiryMinutes * 60 * 1000);
  }

  /**
   * Gửi OTP đến email
   */
  async sendOtp(email: string): Promise<ISendOtpResponse> {
    try {
      // 1. Validate email
      if (!this.isValidEmail(email)) {
        throw new BadRequestException('Email không hợp lệ');
      }

      // 2. Kiểm tra OTP cũ trong Firebase
      const otpRef = this.firebase.collection(this.OTP_COLLECTION);
      const snapshot = await otpRef
        .where('email', '==', email)
        .where('expiresAt', '>', new Date())
        .where('verified', '==', false)
        .orderBy('expiresAt', 'desc')
        .limit(1)
        .get();

      if (!snapshot.empty) {
        const existingOtp = snapshot.docs[0].data();
        const now = new Date();
        const timeDiff = existingOtp.expiresAt.toDate().getTime() - now.getTime();
        const minutesLeft = Math.ceil(timeDiff / (1000 * 60));

        if (minutesLeft > 0) {
          throw new BadRequestException(
            `Vui lòng đợi ${minutesLeft} phút trước khi gửi lại OTP`
          );
        }
      }

      // 3. Tạo OTP mới
      const otpCode = this.generateOtp();
      const expiresAt = this.getExpiryTime();

      // 4. Lưu vào Firebase
      const newOtpRef = otpRef.doc();
      await newOtpRef.set({
        id: newOtpRef.id,
        email,
        code: otpCode,
        expiresAt,
        verified: false,
        attempts: 0,
        createdAt: new Date(),
      });

      // 5. Gửi email
      const sent = await this.mailService.sendOtpEmail(email, otpCode);
      
      if (!sent) {
        // Xóa OTP nếu gửi email thất bại
        await newOtpRef.delete();
        throw new BadRequestException('Không thể gửi email OTP');
      }

      this.logger.log(`OTP ${otpCode} sent to ${email}`);

      return {
        success: true,
        message: 'OTP đã được gửi đến email của bạn',
        expiresAt,
      };
    } catch (error) {
      this.logger.error(`Send OTP failed for ${email}: ${error.message}`);
      throw error;
    }
  }

  /**
   * Xác thực OTP
   */
  async verifyOtp(email: string, otp: string): Promise<IVerifyOtpResponse> {
    try {
      // 1. Tìm OTP trong Firebase
      const otpRef = this.firebase.collection(this.OTP_COLLECTION);
      const snapshot = await otpRef
        .where('email', '==', email)
        .where('code', '==', otp)
        .where('verified', '==', false)
        .limit(1)
        .get();

      if (snapshot.empty) {
        throw new BadRequestException('OTP không tồn tại hoặc đã được sử dụng');
      }

      const otpDoc = snapshot.docs[0];
      const otpData = otpDoc.data();

      // 2. Kiểm tra hết hạn
      if (otpData.expiresAt.toDate() < new Date()) {
        await otpDoc.ref.update({ verified: true }); // Đánh dấu đã hết hạn
        throw new BadRequestException('OTP đã hết hạn');
      }

      // 3. Kiểm tra số lần thử
      const maxAttempts = this.configService.get<number>('OTP_MAX_ATTEMPTS') || 3;
      if (otpData.attempts >= maxAttempts) {
        await otpDoc.ref.update({ verified: true }); // Đánh dấu đã dùng
        throw new BadRequestException('Đã vượt quá số lần thử cho phép');
      }

      // 4. Tăng số lần thử
      await otpDoc.ref.update({
        attempts: otpData.attempts + 1,
      });

      // 5. Nếu OTP đúng, đánh dấu đã verify
      if (otpData.code === otp) {
        await otpDoc.ref.update({
          verified: true,
          verifiedAt: new Date(),
        });

        // 6. Ghi nhận email verification vào collection riêng
        await this.recordEmailVerification(email);

        this.logger.log(`OTP verified for ${email}`);

        return {
          success: true,
          message: 'Xác thực OTP thành công',
          verified: true,
        };
      } else {
        throw new BadRequestException('OTP không chính xác');
      }
    } catch (error) {
      this.logger.error(`Verify OTP failed for ${email}: ${error.message}`);
      throw error;
    }
  }

  /**
   * Ghi nhận xác thực email vào collection riêng
   */
  private async recordEmailVerification(email: string): Promise<void> {
    try {
      const verificationsRef = this.firebase.collection(this.EMAIL_VERIFICATIONS_COLLECTION);
      
      // 1. Kiểm tra đã verify chưa
      const existingSnapshot = await verificationsRef
        .where('email', '==', email)
        .where('verified', '==', true)
        .limit(1)
        .get();
      
      if (!existingSnapshot.empty) {
        this.logger.log(`Email ${email} already verified`);
        return;
      }
      
      // 2. Tạo verification record mới
      await verificationsRef.add({
        email,
        verified: true,
        verifiedAt: new Date(),
        createdAt: new Date(),
      });
      
      this.logger.log(`✅ Email verification recorded for: ${email}`);
    } catch (error) {
      this.logger.error(`❌ Failed to record verification: ${error.message}`);
    }
  }

  /**
   * Kiểm tra trạng thái xác thực email
   */
  async checkEmailVerification(email: string): Promise<IVerificationStatus> {
    try {
      const verificationsRef = this.firebase.collection(this.EMAIL_VERIFICATIONS_COLLECTION);
      const snapshot = await verificationsRef
        .where('email', '==', email)
        .where('verified', '==', true)
        .orderBy('verifiedAt', 'desc')
        .limit(1)
        .get();
      
      const isVerified = !snapshot.empty;
      
      return {
        email,
        isVerified,
        verifiedAt: isVerified ? snapshot.docs[0].data().verifiedAt?.toDate() : undefined,
      };
    } catch (error) {
      this.logger.error(`Check verification failed: ${error.message}`);
      return {
        email,
        isVerified: false,
      };
    }
  }

  /**
   * Dọn dẹp OTP hết hạn và verification records cũ
   */
  async cleanup(): Promise<{ otpsCleaned: number; verificationsCleaned: number }> {
    try {
      const now = new Date();
      let otpsCleaned = 0;
      let verificationsCleaned = 0;
      
      // 1. Clean expired OTPs
      const otpRef = this.firebase.collection(this.OTP_COLLECTION);
      const expiredOtpsSnapshot = await otpRef
        .where('expiresAt', '<', now)
        .where('verified', '==', false)
        .get();
      
      if (!expiredOtpsSnapshot.empty) {
        const batch = this.firebase.getFirestore().batch();
        expiredOtpsSnapshot.docs.forEach(doc => {
          batch.delete(doc.ref);
          otpsCleaned++;
        });
        await batch.commit();
      }
      
      // 2. Clean old verification records (giữ 30 ngày)
      const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      const verificationsRef = this.firebase.collection(this.EMAIL_VERIFICATIONS_COLLECTION);
      const oldVerificationsSnapshot = await verificationsRef
        .where('createdAt', '<', thirtyDaysAgo)
        .get();
      
      if (!oldVerificationsSnapshot.empty) {
        const batch = this.firebase.getFirestore().batch();
        oldVerificationsSnapshot.docs.forEach(doc => {
          batch.delete(doc.ref);
          verificationsCleaned++;
        });
        await batch.commit();
      }
      
      this.logger.log(`🧹 Cleanup: ${otpsCleaned} expired OTPs, ${verificationsCleaned} old verifications`);
      
      return { otpsCleaned, verificationsCleaned };
    } catch (error) {
      this.logger.error(`Cleanup failed: ${error.message}`);
      return { otpsCleaned: 0, verificationsCleaned: 0 };
    }
  }
}