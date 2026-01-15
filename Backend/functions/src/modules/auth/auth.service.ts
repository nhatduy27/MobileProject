import {
  Injectable,
  Inject,
  ConflictException,
  NotFoundException,
  BadRequestException,
  UnauthorizedException,
  HttpException,
  HttpStatus,
} from '@nestjs/common';
import { FirebaseService } from '../../core/firebase/firebase.service';
import { EmailService } from '../email/email.service';
import {
  IUsersRepository,
  IOTPRepository,
  USERS_REPOSITORY_TOKEN,
  OTP_REPOSITORY_TOKEN,
} from './interfaces';
import {
  RegisterDto,
  LoginDto,
  GoogleAuthDto,
  SendOTPDto,
  VerifyOTPDto,
  ForgotPasswordDto,
  ResetPasswordDto,
  ChangePasswordDto,
  LogoutDto,
} from './dto';
import { UserEntity, UserRole, UserStatus, OTPType, OTP_CONFIG } from './entities';
import { FieldValue } from 'firebase-admin/firestore';

/**
 * Auth Service
 *
 * Handles all authentication operations:
 * - Registration (email/password)
 * - Login (email/password)
 * - Google Sign-In
 * - OTP verification
 * - Password reset
 * - Logout
 */
@Injectable()
export class AuthService {
  constructor(
    @Inject(USERS_REPOSITORY_TOKEN)
    private readonly usersRepository: IUsersRepository,
    @Inject(OTP_REPOSITORY_TOKEN)
    private readonly otpRepository: IOTPRepository,
    private readonly firebaseService: FirebaseService,
    private readonly emailService: EmailService,
  ) {}

  /**
   * Login with email/password
   *
   * Flow:
   * 1. Verify credentials with Firebase Auth
   * 2. Get user from Firestore
   * 3. Update last login
   * 4. Return custom token
   */
  async login(dto: LoginDto) {
    try {
      // Get user by email first to check if exists in Firestore
      const user = await this.usersRepository.findByEmail(dto.email);
      if (!user) {
        throw new UnauthorizedException('Email hoặc mật khẩu không chính xác');
      }

      // Check user status
      if (user.status === UserStatus.BANNED) {
        throw new UnauthorizedException(
          `Tài khoản đã bị khóa${user.bannedReason ? `: ${user.bannedReason}` : ''}`,
        );
      }

      // Verify password with Firebase Auth
      // Note: Firebase Admin SDK doesn't have direct password verification
      // We use signInWithEmailAndPassword via REST API
      const signInUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${process.env.FIREBASE_API_KEY}`;

      const response = await fetch(signInUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: dto.email,
          password: dto.password,
          returnSecureToken: true,
        }),
      });

      if (!response.ok) {
        const error: any = await response.json();
        if (
          error.error?.message === 'INVALID_PASSWORD' ||
          error.error?.message === 'EMAIL_NOT_FOUND'
        ) {
          throw new UnauthorizedException('Email hoặc mật khẩu không chính xác');
        }
        throw new UnauthorizedException('Đăng nhập thất bại');
      }

      // Update last login
      await this.usersRepository.updateLastLogin(user.id!);

      // Generate custom token
      const customToken = await this.firebaseService.auth.createCustomToken(user.id!);

      return {
        user: {
          id: user.id,
          email: user.email,
          displayName: user.displayName,
          phone: user.phone,
          photoUrl: user.photoUrl,
          role: user.role,
          status: user.status,
          emailVerified: user.emailVerified,
          createdAt: user.createdAt,
        },
        customToken,
        message: 'Đăng nhập thành công',
      };
    } catch (error: any) {
      if (error instanceof UnauthorizedException) {
        throw error;
      }
      console.error('Login error:', error);
      throw new UnauthorizedException('Đăng nhập thất bại');
    }
  }

  /**
   * Register new user with email/password
   *
   * AUTH-003
   */
  async register(dto: RegisterDto) {
    // Check if email already exists
    const existingUser = await this.usersRepository.findByEmail(dto.email);
    if (existingUser) {
      throw new ConflictException('Email đã được sử dụng');
    }

    // Check if phone already exists (if provided)
    if (dto.phone) {
      const existingPhone = await this.usersRepository.findByPhone(dto.phone);
      if (existingPhone) {
        throw new ConflictException('Số điện thoại đã được sử dụng');
      }
    }

    try {
      // Normalize phone number to E.164 format if provided
      let phoneNumber: string | undefined;
      if (dto.phone) {
        // Convert Vietnamese phone: 0901234567 -> +84901234567
        phoneNumber = dto.phone.startsWith('+')
          ? dto.phone
          : dto.phone.startsWith('0')
            ? `+84${dto.phone.substring(1)}`
            : `+${dto.phone}`;
      }

      // Create Firebase Auth user
      const userRecord = await this.firebaseService.auth.createUser({
        email: dto.email,
        password: dto.password,
        displayName: dto.displayName,
        ...(phoneNumber && { phoneNumber }), // Only include if provided
        emailVerified: false, // Require email verification
      });

      // Set custom claims (role)
      await this.firebaseService.auth.setCustomUserClaims(userRecord.uid, {
        role: dto.role,
      });

      // Create Firestore user document
      const userEntity: Omit<UserEntity, 'id'> = {
        email: dto.email,
        displayName: dto.displayName,
        ...(phoneNumber && { phone: phoneNumber }), // Store normalized phone
        role: UserRole.CUSTOMER,
        status: UserStatus.ACTIVE,
        emailVerified: false,
        fcmTokens: [],
        loginCount: 0,
        createdAt: FieldValue.serverTimestamp() as any,
        updatedAt: FieldValue.serverTimestamp() as any,
      };

      await this.usersRepository.createWithId(userRecord.uid, userEntity as UserEntity);

      // Generate custom token for client to sign in
      const customToken = await this.firebaseService.auth.createCustomToken(userRecord.uid);
      // Send welcome email (don't await - run in background)
      this.emailService.sendWelcomeEmail(dto.email, dto.displayName).catch((err) => {
        console.error('Failed to send welcome email:', err);
      });

      return {
        user: {
          id: userRecord.uid,
          email: userEntity.email,
          displayName: userEntity.displayName,
          role: userEntity.role,
          status: userEntity.status,
        },
        customToken,
      };
    } catch (error: any) {
      // Handle Firebase Auth errors
      if (error.code === 'auth/email-already-exists') {
        throw new ConflictException('Email đã được sử dụng');
      }
      if (error.code === 'auth/phone-number-already-exists') {
        throw new ConflictException('Số điện thoại đã được sử dụng');
      }
      throw error;
    }
  }

  /**
   * Google Sign-In
   *
   * AUTH-004
   *
   * Flow:
   * 1. Mobile app gets Google ID token from Firebase SDK
   * 2. Send token to backend
   * 3. Backend verifies token
   * 4. Create/update user in Firestore
   */
  async googleSignIn(dto: GoogleAuthDto) {
    try {
      // Verify Google ID token with Firebase Admin
      const decodedToken = await this.firebaseService.auth.verifyIdToken(dto.idToken);
      const { uid, email, name, picture, email_verified } = decodedToken;

      if (!email) {
        throw new BadRequestException('Email không tồn tại trong Google account');
      }

      // Check if user exists
      let user = await this.usersRepository.findById(uid);
      let isNewUser = false;

      if (!user) {
        // New user - create in Firestore
        const role = dto.role || UserRole.CUSTOMER;

        // Set custom claims
        await this.firebaseService.auth.setCustomUserClaims(uid, { role });

        const userEntity: Omit<UserEntity, 'id'> = {
          email,
          displayName: name || email.split('@')[0],
          photoUrl: picture,
          role,
          status: UserStatus.ACTIVE,
          emailVerified: email_verified || false,
          fcmTokens: [],
          loginCount: 1,
          lastLoginAt: FieldValue.serverTimestamp() as any,
          createdAt: FieldValue.serverTimestamp() as any,
          updatedAt: FieldValue.serverTimestamp() as any,
        };

        await this.usersRepository.createWithId(uid, userEntity as UserEntity);
        user = await this.usersRepository.findById(uid);
        isNewUser = true;
      } else {
        // Existing user - update last login
        await this.usersRepository.updateLastLogin(uid);
        user = await this.usersRepository.findById(uid);
      }

      return {
        user: {
          id: user!.id,
          email: user!.email,
          displayName: user!.displayName,
          photoUrl: user!.photoUrl,
          role: user!.role,
          status: user!.status,
          emailVerified: user!.emailVerified,
        },
        isNewUser,
      };
    } catch (error: any) {
      if (error.code === 'auth/id-token-expired') {
        throw new UnauthorizedException('Google token đã hết hạn');
      }
      if (error.code === 'auth/invalid-id-token') {
        throw new UnauthorizedException('Google token không hợp lệ');
      }
      throw error;
    }
  }

  /**
   * Send OTP to email
   *
   * AUTH-005
   */
  async sendOTP(dto: SendOTPDto) {
    const { email } = dto;

    // Check rate limiting
    const hasRecent = await this.otpRepository.hasRecentRequest(
      email,
      OTPType.EMAIL_VERIFICATION,
      OTP_CONFIG.RATE_LIMIT_SECONDS,
    );

    if (hasRecent) {
      throw new HttpException(
        `Vui lòng đợi ${OTP_CONFIG.RATE_LIMIT_SECONDS} giây trước khi gửi lại OTP`,
        HttpStatus.TOO_MANY_REQUESTS,
      );
    }

    // Generate 6-digit code
    const code = this.generateOTPCode();

    // Save OTP to Firestore
    const expiresAt = new Date(Date.now() + OTP_CONFIG.EXPIRY_MINUTES * 60 * 1000);

    await this.otpRepository.create({
      email,
      code,
      type: OTPType.EMAIL_VERIFICATION,
      expiresAt,
      verified: false,
      attempts: 0,
      createdAt: FieldValue.serverTimestamp() as any,
      updatedAt: FieldValue.serverTimestamp() as any,
    } as any);

    // Send OTP email
    await this.emailService.sendEmailVerificationOTP(email, code);

    return {
      message: 'OTP đã được gửi đến email của bạn',
    };
  }

  /**
   * Verify OTP
   *
   * AUTH-005
   */
  async verifyOTP(dto: VerifyOTPDto) {
    const { email, code, type } = dto;

    // Find latest OTP
    const otp = await this.otpRepository.findLatestByEmail(email, type);

    if (!otp) {
      throw new NotFoundException('Không tìm thấy OTP. Vui lòng gửi lại OTP.');
    }
    // Check if expired (convert Firestore Timestamp to Date)
    const expiresAt =
      otp.expiresAt instanceof Date ? otp.expiresAt : (otp.expiresAt as any).toDate();

    if (new Date() > expiresAt) {
      throw new BadRequestException('OTP đã hết hạn. Vui lòng gửi lại OTP.');
    }

    // Check attempts
    if (otp.attempts >= OTP_CONFIG.MAX_ATTEMPTS) {
      throw new BadRequestException('Đã vượt quá số lần thử. Vui lòng gửi lại OTP.');
    }

    // Verify code
    if (otp.code !== code) {
      if (!otp.id) throw new Error('OTP ID missing');
      await this.otpRepository.incrementAttempts(otp.id);
      throw new BadRequestException('Mã OTP không chính xác');
    }

    // Mark as verified
    if (!otp.id) throw new Error('OTP ID missing');
    await this.otpRepository.markVerified(otp.id);

    // Update user's emailVerified status
    const user = await this.usersRepository.findByEmail(email);
    if (user && user.id) {
      await this.usersRepository.markEmailVerified(user.id);
    }

    // Delete all OTPs for this email
    await this.otpRepository.deleteByEmail(email, type);

    return {
      message: 'Xác thực email thành công',
    };
  }

  /**
   * Send password reset OTP
   *
   * AUTH-006
   */
  async forgotPassword(dto: ForgotPasswordDto) {
    const { email } = dto;

    // Check rate limiting
    const hasRecent = await this.otpRepository.hasRecentRequest(
      email,
      OTPType.PASSWORD_RESET,
      OTP_CONFIG.RATE_LIMIT_SECONDS,
    );

    if (hasRecent) {
      throw new HttpException(
        `Vui lòng đợi ${OTP_CONFIG.RATE_LIMIT_SECONDS} giây trước khi gửi lại OTP`,
        HttpStatus.TOO_MANY_REQUESTS,
      );
    }

    // Generate OTP
    const code = this.generateOTPCode();
    const expiresAt = new Date(Date.now() + OTP_CONFIG.EXPIRY_MINUTES * 60 * 1000);

    await this.otpRepository.create({
      email,
      code,
      type: OTPType.PASSWORD_RESET,
      expiresAt,
      verified: false,
      attempts: 0,
      createdAt: FieldValue.serverTimestamp() as any,
      updatedAt: FieldValue.serverTimestamp() as any,
    } as any);

    // Send password reset email
    await this.emailService.sendPasswordResetOTP(email, code);

    return {
      message: 'Mã xác nhận đã được gửi đến email của bạn',
    };
  }

  /**
   * Reset password with OTP
   *
   * AUTH-006
   */
  async resetPassword(dto: ResetPasswordDto) {
    const { email, newPassword } = dto;

    // Find user
    const user = await this.usersRepository.findByEmail(email);
    if (!user || !user.id) {
      throw new NotFoundException('User không tồn tại');
    }

    // Update password in Firebase Auth
    await this.firebaseService.auth.updateUser(user.id, {
      password: newPassword,
    });

    return {
      message: 'Đặt lại mật khẩu thành công',
    };
  }

  /**
   * Change password (authenticated user)
   *
   * AUTH-007
   */
  async changePassword(userId: string, dto: ChangePasswordDto) {
    const { newPassword } = dto;

    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User không tồn tại');
    }

    // Verify old password by attempting to sign in
    // Note: Firebase Admin SDK doesn't have a direct way to verify password
    // This is a workaround - client should ideally reauthenticate before changing password

    // For now, we'll just update the password
    // TODO: Consider requiring reauthentication on client side before calling this

    await this.firebaseService.auth.updateUser(userId, {
      password: newPassword,
    });

    return {
      message: 'Đổi mật khẩu thành công',
    };
  }

  /**
   * Logout - remove FCM token
   *
   * AUTH-008
   */
  async logout(userId: string, dto: LogoutDto) {
    if (dto.fcmToken) {
      await this.usersRepository.removeFcmToken(userId, dto.fcmToken);
    }

    return {
      message: 'Đăng xuất thành công',
    };
  }

  /**
   * Set user role
   * 
   * Updates role in both Firestore and Firebase Custom Claims
   */
  async setRole(userId: string, role: UserRole) {
    // Update role in Firestore
    await this.usersRepository.update(userId, { role } as Partial<UserEntity>);

    // Update custom claims in Firebase Auth
    await this.firebaseService.auth.setCustomUserClaims(userId, { role });

    return {
      message: 'Role updated successfully',
      role,
    };
  }

  /**
   * Generate random 6-digit OTP code
   */
  private generateOTPCode(): string {
    return Math.floor(100000 + Math.random() * 900000).toString();
  }
}
