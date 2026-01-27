import { Module } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { USERS_REPOSITORY_TOKEN, OTP_REPOSITORY_TOKEN } from './interfaces';
import { FirestoreUsersRepository, FirestoreOTPRepository } from './repositories';
import { WalletsModule } from '../wallets/wallets.module';

/**
 * Auth Module
 *
 * Handles authentication operations:
 * - Email/password registration
 * - Google Sign-In
 * - OTP verification
 * - Password reset
 * - Logout
 *
 * Dependency Injection:
 * - USERS_REPOSITORY_TOKEN -> FirestoreUsersRepository
 * - OTP_REPOSITORY_TOKEN -> FirestoreOTPRepository
 */
@Module({
  imports: [WalletsModule],
  controllers: [AuthController],
  providers: [
    // Register repositories
    {
      provide: USERS_REPOSITORY_TOKEN,
      useClass: FirestoreUsersRepository,
    },
    {
      provide: OTP_REPOSITORY_TOKEN,
      useClass: FirestoreOTPRepository,
    },
    AuthService,
  ],
  exports: [
    AuthService,
    USERS_REPOSITORY_TOKEN, // Export for use in other modules
  ],
})
export class AuthModule {}
