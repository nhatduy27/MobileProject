import {
  IsOptional,
  IsString,
  MinLength,
  MaxLength,
  Matches,
  IsUrl,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * DTO for updating user profile
 *
 * All fields are optional - only update provided fields.
 */
export class UpdateProfileDto {
  @ApiPropertyOptional({
    description: 'Full name',
    example: 'Nguyễn Văn B',
  })
  @IsOptional()
  @IsString({ message: 'Họ tên phải là chuỗi' })
  @MinLength(2, { message: 'Họ tên phải có ít nhất 2 ký tự' })
  @MaxLength(100, { message: 'Họ tên không được quá 100 ký tự' })
  fullName?: string;

  @ApiPropertyOptional({
    description: 'Phone number (Vietnamese format)',
    example: '0901234567',
  })
  @IsOptional()
  @IsString({ message: 'Số điện thoại phải là chuỗi' })
  @Matches(/^(0[3|5|7|8|9])+([0-9]{8})$/, {
    message: 'Số điện thoại không hợp lệ (VD: 0901234567)',
  })
  phone?: string;

  @ApiPropertyOptional({
    description: 'Avatar image URL',
    example: 'https://example.com/avatar.jpg',
  })
  @IsOptional()
  @IsString({ message: 'URL ảnh đại diện phải là chuỗi' })
  @IsUrl({}, { message: 'URL ảnh đại diện không hợp lệ' })
  imageAvatar?: string;
}
