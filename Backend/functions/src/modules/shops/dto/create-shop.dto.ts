import {
  IsString,
  IsNotEmpty,
  IsNumber,
  Min,
  MaxLength,
  MinLength,
  Matches,
} from 'class-validator';
import { Transform } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';

export class CreateShopDto {
  @ApiProperty({ example: 'Quán Phở Việt', description: 'Tên shop' })
  @IsString()
  @IsNotEmpty({ message: 'Tên shop không được để trống' })
  @MinLength(3, { message: 'Tên shop phải có ít nhất 3 ký tự' })
  @MaxLength(100, { message: 'Tên shop không được quá 100 ký tự' })
  name: string;

  @ApiProperty({ example: 'Phở ngon nhất KTX', description: 'Mô tả shop' })
  @IsString()
  @IsNotEmpty({ message: 'Mô tả không được để trống' })
  @MaxLength(500, { message: 'Mô tả không được quá 500 ký tự' })
  description: string;

  @ApiProperty({ example: 'Tòa A, Tầng 1', description: 'Địa chỉ shop' })
  @IsString()
  @IsNotEmpty({ message: 'Địa chỉ không được để trống' })
  @MaxLength(200, { message: 'Địa chỉ không được quá 200 ký tự' })
  address: string;

  @ApiProperty({ example: '0901234567', description: 'Số điện thoại shop' })
  @IsString()
  @IsNotEmpty({ message: 'Số điện thoại không được để trống' })
  @Matches(/^[0-9]{10}$/, { message: 'Số điện thoại phải là 10 chữ số' })
  phone: string;

  @ApiProperty({ example: '07:00', description: 'Giờ mở cửa (HH:mm)' })
  @IsString()
  @IsNotEmpty({ message: 'Giờ mở cửa không được để trống' })
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ mở cửa phải có định dạng HH:mm (VD: 07:00)',
  })
  openTime: string;

  @ApiProperty({ example: '21:00', description: 'Giờ đóng cửa (HH:mm)' })
  @IsString()
  @IsNotEmpty({ message: 'Giờ đóng cửa không được để trống' })
  @Matches(/^([0-1][0-9]|2[0-3]):[0-5][0-9]$/, {
    message: 'Giờ đóng cửa phải có định dạng HH:mm (VD: 21:00)',
  })
  closeTime: string;

  @ApiProperty({ example: 5000, description: 'Phí ship (VNĐ), tối thiểu 3,000đ' })
  @Transform(({ value }) => Number(value))
  @IsNumber({}, { message: 'shipFeePerOrder phải là số' })
  @Min(3000, { message: 'Phí ship tối thiểu 3,000đ' })
  shipFeePerOrder: number;

  @ApiProperty({ example: 20000, description: 'Đơn hàng tối thiểu (VNĐ)' })
  @Transform(({ value }) => Number(value))
  @IsNumber({}, { message: 'minOrderAmount phải là số' })
  @Min(10000, { message: 'Đơn tối thiểu phải từ 10,000đ' })
  minOrderAmount: number;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Ảnh bìa shop',
  })
  coverImage: any;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Logo shop',
  })
  logo: any;
}
