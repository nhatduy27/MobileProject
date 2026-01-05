import { IsEmail, IsNotEmpty, IsString, Length, Matches } from 'class-validator';

export class SendOtpDto {
  @IsEmail({}, { message: 'Email không hợp lệ' })
  @IsNotEmpty({ message: 'Email không được để trống' })
  email: string;
}

export class VerifyOtpDto {
  @IsEmail({}, { message: 'Email không hợp lệ' })
  @IsNotEmpty({ message: 'Email không được để trống' })
  email: string;

  @IsString({ message: 'OTP phải là chuỗi' })
  @IsNotEmpty({ message: 'OTP không được để trống' })
  @Length(6, 6, { message: 'OTP phải có 6 chữ số' })
  @Matches(/^\d+$/, { message: 'OTP chỉ được chứa số' })
  otp: string;
}