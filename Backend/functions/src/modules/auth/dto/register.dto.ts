import { IsEmail, IsString, MinLength, MaxLength, IsEnum, IsOptional, Matches } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { UserRole } from '../entities';

/**
 * Register DTO
 * 
 * Request body for POST /auth/register
 */
export class RegisterDto {
  @ApiProperty({
    example: 'user@example.com',
    description: 'Email address',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  email: string;

  @ApiProperty({
    example: 'Password123!',
    description: 'Password (min 6 characters)',
    minLength: 6,
  })
  @IsString()
  @MinLength(6, { message: 'Mật khẩu phải có ít nhất 6 ký tự' })
  password: string;

  @ApiProperty({
    example: 'Nguyễn Văn A',
    description: 'Display name',
  })
  @IsString()
  @MinLength(2, { message: 'Tên phải có ít nhất 2 ký tự' })
  @MaxLength(100, { message: 'Tên không được quá 100 ký tự' })
  displayName: string;

  @ApiPropertyOptional({
    example: '0901234567',
    description: 'Phone number (Vietnamese format)',
  })
  @IsOptional()
  @IsString()
  @Matches(/^(0|\+84)[0-9]{9}$/, { message: 'Số điện thoại không hợp lệ' })
  phone?: string;

  @ApiProperty({
    enum: UserRole,
    example: UserRole.CUSTOMER,
    description: 'User role',
  })
  @IsOptional()
  @IsEnum(UserRole, { message: 'Role không hợp lệ' })
  role: UserRole;
}

/**
 * Register Response DTO
 */
export class RegisterResponseDto {
  @ApiProperty({
    description: 'User data',
  })
  user: {
    id: string;
    email: string;
    displayName: string;
    role: UserRole;
    status: string;
  };

  @ApiProperty({
    description: 'Firebase custom token for client-side sign-in',
  })
  customToken: string;
}
