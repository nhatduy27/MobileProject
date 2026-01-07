import { IsEmail, IsString, Length } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Send OTP DTO
 * 
 * Request body for POST /auth/send-otp
 */
export class SendOTPDto {
  @ApiProperty({
    example: 'user@example.com',
    description: 'Email to send OTP to',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  email: string;
}

/**
 * Verify OTP DTO
 * 
 * Request body for POST /auth/verify-otp
 */
export class VerifyOTPDto {
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
}
