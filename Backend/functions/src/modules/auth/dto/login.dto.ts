import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Login DTO
 * Đăng nhập bằng email/password
 */
export class LoginDto {
  @ApiProperty({
    description: 'Email của user',
    example: 'user@example.com',
  })
  @IsEmail({}, { message: 'Email không hợp lệ' })
  @IsNotEmpty({ message: 'Email không được để trống' })
  email: string;

  @ApiProperty({
    description: 'Mật khẩu (tối thiểu 6 ký tự)',
    example: 'password123',
    minLength: 6,
  })
  @IsString()
  @MinLength(6, { message: 'Mật khẩu phải có ít nhất 6 ký tự' })
  @IsNotEmpty({ message: 'Mật khẩu không được để trống' })
  password: string;
}

/**
 * Login Response DTO
 */
export class LoginResponseDto {
  @ApiProperty({
    description: 'Thông tin user sau khi đăng nhập',
  })
  user: {
    id: string;
    email: string;
    displayName: string;
    phone?: string;
    photoUrl?: string;
    role: string;
    status: string;
    emailVerified: boolean;
    createdAt: Date;
  };

  @ApiProperty({
    description: 'Custom token để client dùng signInWithCustomToken()',
    example: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  customToken: string;

  @ApiProperty({
    description: 'Message',
    example: 'Đăng nhập thành công',
  })
  message: string;
}
