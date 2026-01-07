import { Module } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';

/**
 * Auth Module
 *
 * Handles all authentication and user management:
 * - User registration (email/password)
 * - Login verification
 * - Google Sign-In
 * - Profile management
 * - Role management
 */
@Module({
  controllers: [AuthController],
  providers: [AuthService],
  exports: [AuthService],
})
export class AuthModule {}
