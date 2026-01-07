import { IsString, IsEnum, IsOptional } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { UserRole } from '../entities';

/**
 * Google Auth DTO
 * 
 * Request body for POST /auth/google
 */
export class GoogleAuthDto {
  @ApiProperty({
    description: 'Google ID Token from Firebase Auth client SDK',
    example: 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjE...',
  })
  @IsString({ message: 'ID Token là bắt buộc' })
  idToken: string;

  @ApiPropertyOptional({
    enum: UserRole,
    example: UserRole.CUSTOMER,
    description: 'Role for new users (defaults to CUSTOMER)',
  })
  @IsOptional()
  @IsEnum(UserRole, { message: 'Role không hợp lệ' })
  role?: UserRole;
}

/**
 * Google Auth Response DTO
 */
export class GoogleAuthResponseDto {
  @ApiProperty({
    description: 'User data',
  })
  user: {
    id: string;
    email: string;
    displayName: string;
    photoUrl?: string;
    role: UserRole;
    status: string;
    emailVerified: boolean;
  };

  @ApiProperty({
    description: 'Whether this was a new user registration',
  })
  isNewUser: boolean;
}
