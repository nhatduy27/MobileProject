import {
  Injectable,
  Logger,
  BadRequestException,
  UnauthorizedException,
  ConflictException,
  NotFoundException,
} from '@nestjs/common';
import { FirebaseService } from '../../core/firebase/firebase.service';
import { RegisterDto, UpdateProfileDto } from './dto';
import {
  UserEntity,
  createDefaultUserEntity,
  createGoogleUserEntity,
} from './entities/user.entity';
import { ErrorCodes } from '../../shared/constants/error-codes';

/**
 * Auth Service
 *
 * Handles all authentication and user management operations:
 * - Register with email/password
 * - Login verification
 * - Google Sign-In
 * - Profile CRUD
 * - Role management
 */
@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);
  private readonly USERS_COLLECTION = 'users';

  constructor(private readonly firebaseService: FirebaseService) {}

  /**
   * Register new user with email/password
   *
   * 1. Create Firebase Auth user
   * 2. Create Firestore user document
   * 3. Return user data
   */
  async register(dto: RegisterDto): Promise<{ user: UserEntity; uid: string }> {
    const { email, password, fullName, phone } = dto;

    // Check if email already exists
    try {
      await this.firebaseService.auth.getUserByEmail(email);
      throw new ConflictException({
        code: ErrorCodes.AUTH_EMAIL_ALREADY_EXISTS,
        message: 'Email đã được sử dụng',
      });
    } catch (error: any) {
      // If user not found, continue with registration
      if (error.code !== 'auth/user-not-found') {
        if (error instanceof ConflictException) throw error;
        this.logger.error('Error checking email existence', error);
      }
    }

    try {
      // 1. Create Firebase Auth user
      const userRecord = await this.firebaseService.auth.createUser({
        email,
        password,
        displayName: fullName,
        emailVerified: false,
      });

      this.logger.log(`Created Firebase Auth user: ${userRecord.uid}`);

      // 2. Create Firestore user document
      const userEntity = createDefaultUserEntity(userRecord.uid, email, fullName);

      // Add phone if provided
      if (phone) {
        userEntity.phone = phone;
      }

      await this.firebaseService.firestore
        .collection(this.USERS_COLLECTION)
        .doc(userRecord.uid)
        .set(userEntity);

      this.logger.log(`Created Firestore user document: ${userRecord.uid}`);

      return {
        user: userEntity,
        uid: userRecord.uid,
      };
    } catch (error: any) {
      this.logger.error('Registration failed', error);

      // Handle Firebase Auth errors
      if (error.code === 'auth/email-already-exists') {
        throw new ConflictException({
          code: ErrorCodes.AUTH_EMAIL_ALREADY_EXISTS,
          message: 'Email đã được sử dụng',
        });
      }

      if (error.code === 'auth/invalid-email') {
        throw new BadRequestException({
          code: ErrorCodes.AUTH_INVALID_EMAIL,
          message: 'Email không hợp lệ',
        });
      }

      if (error.code === 'auth/weak-password') {
        throw new BadRequestException({
          code: ErrorCodes.AUTH_WEAK_PASSWORD,
          message: 'Mật khẩu quá yếu',
        });
      }

      throw new BadRequestException({
        code: ErrorCodes.AUTH_REGISTRATION_FAILED,
        message: 'Đăng ký thất bại. Vui lòng thử lại.',
      });
    }
  }

  /**
   * Verify Firebase ID Token
   *
   * Called after client login with Firebase Auth SDK.
   * Verifies the token and returns/creates user profile.
   */
  async verifyToken(idToken: string): Promise<{ user: UserEntity }> {
    try {
      // Verify the ID token
      const decodedToken = await this.firebaseService.auth.verifyIdToken(idToken);
      const uid = decodedToken.uid;

      // Get user profile from Firestore
      const userDoc = await this.firebaseService.firestore
        .collection(this.USERS_COLLECTION)
        .doc(uid)
        .get();

      if (!userDoc.exists) {
        throw new NotFoundException({
          code: ErrorCodes.AUTH_USER_NOT_FOUND,
          message: 'Không tìm thấy thông tin người dùng',
        });
      }

      const user = userDoc.data() as UserEntity;

      return { user };
    } catch (error: any) {
      this.logger.error('Token verification failed', error);

      if (error instanceof NotFoundException) throw error;

      if (error.code === 'auth/id-token-expired') {
        throw new UnauthorizedException({
          code: ErrorCodes.AUTH_TOKEN_EXPIRED,
          message: 'Phiên đăng nhập đã hết hạn',
        });
      }

      if (error.code === 'auth/id-token-revoked') {
        throw new UnauthorizedException({
          code: ErrorCodes.AUTH_TOKEN_REVOKED,
          message: 'Phiên đăng nhập đã bị thu hồi',
        });
      }

      throw new UnauthorizedException({
        code: ErrorCodes.AUTH_INVALID_TOKEN,
        message: 'Token không hợp lệ',
      });
    }
  }

  /**
   * Handle Google Sign-In
   *
   * 1. Verify Google ID Token
   * 2. Check if user exists in Firestore
   * 3. If not, create new user document
   * 4. Return user data
   */
  async googleSignIn(
    idToken: string,
  ): Promise<{ user: UserEntity; isNewUser: boolean }> {
    try {
      // Verify the ID token from Google Sign-In
      const decodedToken = await this.firebaseService.auth.verifyIdToken(idToken);
      const uid = decodedToken.uid;
      const email = decodedToken.email || '';
      const displayName = decodedToken.name || 'Google User';
      const photoUrl = decodedToken.picture;

      // Check if user exists in Firestore
      const userDoc = await this.firebaseService.firestore
        .collection(this.USERS_COLLECTION)
        .doc(uid)
        .get();

      if (userDoc.exists) {
        // Existing user - return profile
        const user = userDoc.data() as UserEntity;
        this.logger.log(`Google Sign-In: Existing user ${uid}`);
        return { user, isNewUser: false };
      }

      // New user - create Firestore document
      const userEntity = createGoogleUserEntity(uid, email, displayName, photoUrl);

      await this.firebaseService.firestore
        .collection(this.USERS_COLLECTION)
        .doc(uid)
        .set(userEntity);

      this.logger.log(`Google Sign-In: Created new user ${uid}`);

      return { user: userEntity, isNewUser: true };
    } catch (error: any) {
      this.logger.error('Google Sign-In failed', error);

      if (error.code === 'auth/id-token-expired') {
        throw new UnauthorizedException({
          code: ErrorCodes.AUTH_TOKEN_EXPIRED,
          message: 'Token đã hết hạn',
        });
      }

      throw new UnauthorizedException({
        code: ErrorCodes.AUTH_GOOGLE_SIGNIN_FAILED,
        message: 'Đăng nhập Google thất bại',
      });
    }
  }

  /**
   * Get user profile by UID
   */
  async getProfile(uid: string): Promise<UserEntity> {
    const userDoc = await this.firebaseService.firestore
      .collection(this.USERS_COLLECTION)
      .doc(uid)
      .get();

    if (!userDoc.exists) {
      throw new NotFoundException({
        code: ErrorCodes.AUTH_USER_NOT_FOUND,
        message: 'Không tìm thấy thông tin người dùng',
      });
    }

    return userDoc.data() as UserEntity;
  }

  /**
   * Update user profile
   */
  async updateProfile(uid: string, dto: UpdateProfileDto): Promise<UserEntity> {
    const userRef = this.firebaseService.firestore
      .collection(this.USERS_COLLECTION)
      .doc(uid);

    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      throw new NotFoundException({
        code: ErrorCodes.AUTH_USER_NOT_FOUND,
        message: 'Không tìm thấy thông tin người dùng',
      });
    }

    // Build update data - only include provided fields
    const updateData: Partial<UserEntity> = {
      updatedAt: Date.now(),
    };

    if (dto.fullName !== undefined) {
      updateData.fullName = dto.fullName;

      // Also update Firebase Auth display name
      await this.firebaseService.auth.updateUser(uid, {
        displayName: dto.fullName,
      });
    }

    if (dto.phone !== undefined) {
      updateData.phone = dto.phone;
    }

    if (dto.imageAvatar !== undefined) {
      updateData.imageAvatar = dto.imageAvatar;

      // Also update Firebase Auth photo URL
      await this.firebaseService.auth.updateUser(uid, {
        photoURL: dto.imageAvatar,
      });
    }

    await userRef.update(updateData);

    // Return updated user
    const updatedDoc = await userRef.get();
    return updatedDoc.data() as UserEntity;
  }

  /**
   * Update user role (called from Role Selection screen)
   */
  async updateRole(
    uid: string,
    role: 'user' | 'seller' | 'delivery',
  ): Promise<UserEntity> {
    const userRef = this.firebaseService.firestore
      .collection(this.USERS_COLLECTION)
      .doc(uid);

    const userDoc = await userRef.get();

    if (!userDoc.exists) {
      throw new NotFoundException({
        code: ErrorCodes.AUTH_USER_NOT_FOUND,
        message: 'Không tìm thấy thông tin người dùng',
      });
    }

    await userRef.update({
      role,
      updatedAt: Date.now(),
    });

    this.logger.log(`Updated role for user ${uid}: ${role}`);

    // Return updated user
    const updatedDoc = await userRef.get();
    return updatedDoc.data() as UserEntity;
  }

  /**
   * Verify user email/phone
   */
  async setVerified(uid: string, isVerify: boolean): Promise<void> {
    const userRef = this.firebaseService.firestore
      .collection(this.USERS_COLLECTION)
      .doc(uid);

    await userRef.update({
      isVerify,
      updatedAt: Date.now(),
    });

    // Also update Firebase Auth email verified status
    await this.firebaseService.auth.updateUser(uid, {
      emailVerified: isVerify,
    });

    this.logger.log(`Set verified for user ${uid}: ${isVerify}`);
  }

  /**
   * Delete user account
   *
   * Deletes both Firebase Auth user and Firestore document.
   */
  async deleteAccount(uid: string): Promise<void> {
    try {
      // Delete Firestore document
      await this.firebaseService.firestore
        .collection(this.USERS_COLLECTION)
        .doc(uid)
        .delete();

      // Delete Firebase Auth user
      await this.firebaseService.auth.deleteUser(uid);

      this.logger.log(`Deleted user account: ${uid}`);
    } catch (error: any) {
      this.logger.error('Failed to delete user account', error);
      throw new BadRequestException({
        code: ErrorCodes.AUTH_DELETE_FAILED,
        message: 'Xóa tài khoản thất bại',
      });
    }
  }
}
