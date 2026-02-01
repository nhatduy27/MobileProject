import { IsEmail, IsString, Length, IsEnum } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { OTPType } from '../entities/otp.entity';

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

  @ApiProperty({
    example: 'PASSWORD_RESET',
    description: 'Type of OTP verification',
    enum: OTPType,
    enumName: 'OTPType',
  })
  @IsEnum(OTPType, { message: 'Loại OTP không hợp lệ' })
  type: OTPType; 
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

  @ApiProperty({
    example: 'PASSWORD_RESET',
    description: 'Type of OTP verification',
    enum: OTPType,
    enumName: 'OTPType',
  })
  @IsEnum(OTPType, { message: 'Loại OTP không hợp lệ' })
  type: OTPType; // Đổi tên thành type
}
