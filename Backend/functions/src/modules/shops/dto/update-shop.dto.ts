import {
  IsString,
  IsNumber,
  Min,
  MaxLength,
  MinLength,
  Matches,
  IsOptional,
} from 'class-validator';
import { Transform } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class UpdateShopDto {
  @ApiPropertyOptional({ example: 'Quán Phở Việt', description: 'Tên shop' })
  @IsOptional()
  @IsString()
  @MinLength(3, { message: 'Tên shop phải có ít nhất 3 ký tự' })
  @MaxLength(100, { message: 'Tên shop không được quá 100 ký tự' })
  name?: string;

  @ApiPropertyOptional({ example: 'Phở ngon nhất KTX', description: 'Mô tả shop' })
  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Mô tả không được quá 500 ký tự' })
  description?: string;

  @ApiPropertyOptional({ example: 'Tòa A, Tầng 1', description: 'Địa chỉ shop' })
  @IsOptional()
  @IsString()
  @MaxLength(200, { message: 'Địa chỉ không được quá 200 ký tự' })
  address?: string;

  @ApiPropertyOptional({ example: '0901234567', description: 'Số điện thoại shop' })
  @IsOptional()
  @IsString()
  @Matches(/^[0-9]{10}$/, { message: 'Số điện thoại phải là 10 chữ số' })
  phone?: string;

  @ApiPropertyOptional({ example: '07:00', description: 'Giờ mở cửa (HH:mm)' })
  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ mở cửa phải có định dạng HH:mm (VD: 07:00)',
  })
  openTime?: string;

  @ApiPropertyOptional({ example: '21:00', description: 'Giờ đóng cửa (HH:mm)' })
  @IsOptional()
  @IsString()
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ đóng cửa phải có định dạng HH:mm (VD: 21:00)',
  })
  closeTime?: string;

  @ApiPropertyOptional({ example: 5000, description: 'Phí ship (VNĐ), tối thiểu 3,000đ' })
  @IsOptional()
  @Transform(({ value }) => (value !== undefined && value !== '' ? Number(value) : undefined))
  @IsNumber({}, { message: 'shipFeePerOrder phải là số' })
  @Min(3000, { message: 'Phí ship tối thiểu 3,000đ' })
  shipFeePerOrder?: number;

  @ApiPropertyOptional({ example: 20000, description: 'Đơn hàng tối thiểu (VNĐ)' })
  @IsOptional()
  @Transform(({ value }) => (value !== undefined && value !== '' ? Number(value) : undefined))
  @IsNumber({}, { message: 'minOrderAmount phải là số' })
  @Min(10000, { message: 'Đơn tối thiểu phải từ 10,000đ' })
  minOrderAmount?: number;

  @ApiPropertyOptional({
    type: 'string',
    format: 'binary',
    description: 'Ảnh bìa shop (optional)',
  })
  @IsOptional()
  coverImage?: any;

  @ApiPropertyOptional({
    type: 'string',
    format: 'binary',
    description: 'Logo shop (optional)',
  })
  @IsOptional()
  logo?: any;
}
