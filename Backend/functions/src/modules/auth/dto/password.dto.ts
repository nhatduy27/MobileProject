import { IsEmail, IsString, MinLength, Length } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Forgot Password DTO
 * 
 * Request body for POST /auth/forgot-password
 */
export class ForgotPasswordDto {
  @ApiProperty({
    example: 'user@example.com',
    description: 'Email address',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  email: string;
}

/**
 * Reset Password DTO
 * 
 * Request body for POST /auth/reset-password
 */
export class ResetPasswordDto {
  @ApiProperty({
    example: 'user@example.com',
    description: 'Email address',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  email: string;

  @ApiProperty({
    example: '123456',
    description: '6-digit OTP code',
  })
  @IsString()
  @Length(6, 6, { message: 'Mã OTP phải có 6 ký tự' })
  code: string;

  @ApiProperty({
    example: 'NewPassword123!',
    description: 'New password (min 6 characters)',
  })
  @IsString()
  @MinLength(6, { message: 'Mật khẩu phải có ít nhất 6 ký tự' })
  newPassword: string;
}

/**
 * Change Password DTO
 * 
 * Request body for PUT /auth/change-password
 */
export class ChangePasswordDto {
  @ApiProperty({
    example: 'OldPassword123!',
    description: 'Current password',
  })
  @IsString({ message: 'Mật khẩu hiện tại là bắt buộc' })
  oldPassword: string;

  @ApiProperty({
    example: 'NewPassword123!',
    description: 'New password (min 6 characters)',
  })
  @IsString()
  @MinLength(6, { message: 'Mật khẩu mới phải có ít nhất 6 ký tự' })
  newPassword: string;
}
