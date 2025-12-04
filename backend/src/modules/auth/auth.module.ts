import { Module } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { AuthRepository } from './domain/auth.repository';
import { FirebaseAuthRepository } from './infra/firebase-auth.repository';

/**
 * Auth Module
 * 
 * Feature module for authentication operations.
 * Uses Dependency Inversion by binding AuthRepository to FirebaseAuthRepository.
 */
@Module({
  controllers: [AuthController],
  providers: [
    AuthService,
    // Bind abstract AuthRepository to concrete FirebaseAuthRepository
    {
      provide: AuthRepository,
      useClass: FirebaseAuthRepository,
    },
  ],
  exports: [AuthService],
})
export class AuthModule {}
