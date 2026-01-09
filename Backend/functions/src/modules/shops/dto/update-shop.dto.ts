import {
  IsString,
  IsOptional,
  MaxLength,
  MinLength,
  IsNumber,
  Min,
  Matches,
} from 'class-validator';

export class UpdateShopDto {
  @IsOptional()
  @IsString()
  @MinLength(3, { message: 'Tên shop phải có ít nhất 3 ký tự' })
  @MaxLength(100, { message: 'Tên shop không được quá 100 ký tự' })
  name?: string;

  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Mô tả không được quá 500 ký tự' })
  description?: string;

  @IsOptional()
  @IsString()
  @MaxLength(200, { message: 'Địa chỉ không được quá 200 ký tự' })
  address?: string;

  @IsOptional()
  @IsString()
  @Matches(/^[0-9]{10}$/, { message: 'Số điện thoại phải là 10 chữ số' })
  phone?: string;

  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ mở cửa phải có định dạng HH:mm (VD: 07:00)',
  })
  openTime?: string;

  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ đóng cửa phải có định dạng HH:mm (VD: 21:00)',
  })
  closeTime?: string;

  @IsOptional()
  @IsNumber()
  @Min(3000, { message: 'Phí ship tối thiểu 3,000đ' })
  shipFeePerOrder?: number;

  @IsOptional()
  @IsNumber()
  @Min(10000, { message: 'Đơn tối thiểu phải từ 10,000đ' })
  minOrderAmount?: number;

  @IsOptional()
  @IsString()
  coverImageUrl?: string;

  @IsOptional()
  @IsString()
  logoUrl?: string;
}
