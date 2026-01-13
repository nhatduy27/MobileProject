import {
  IsString,
  IsEnum,
  IsNotEmpty,
  MaxLength,
  Matches,
} from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export enum VehicleType {
  MOTORBIKE = 'MOTORBIKE',
  CAR = 'CAR',
  BICYCLE = 'BICYCLE',
}

export class ApplyShipperWithFilesDto {
  @ApiProperty({
    example: 'shop_abc123',
    description: 'ID của shop muốn apply làm shipper',
  })
  @IsNotEmpty({ message: 'Shop ID không được để trống' })
  @IsString()
  shopId: string;

  @ApiProperty({
    enum: VehicleType,
    example: VehicleType.MOTORBIKE,
    description: 'Loại phương tiện',
  })
  @IsNotEmpty({ message: 'Loại phương tiện không được để trống' })
  @IsEnum(VehicleType, { message: 'Loại phương tiện không hợp lệ' })
  vehicleType: VehicleType;

  @ApiProperty({
    example: '59X1-12345',
    description: 'Biển số xe',
  })
  @IsNotEmpty({ message: 'Biển số xe không được để trống' })
  @IsString()
  @MaxLength(20, { message: 'Biển số xe không được quá 20 ký tự' })
  vehicleNumber: string;

  @ApiProperty({
    example: '079202012345',
    description: 'Số CMND/CCCD (12 chữ số)',
  })
  @IsNotEmpty({ message: 'Số CMND/CCCD không được để trống' })
  @IsString()
  @Matches(/^[0-9]{12}$/, {
    message: 'Số CMND/CCCD phải là 12 chữ số',
  })
  idCardNumber: string;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Ảnh mặt trước CMND/CCCD',
  })
  idCardFront: any;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Ảnh mặt sau CMND/CCCD',
  })
  idCardBack: any;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    description: 'Ảnh bằng lái xe',
  })
  driverLicense: any;

  @ApiProperty({
    example: 'Tôi muốn làm shipper cho shop này',
    description: 'Lời nhắn khi apply (tối đa 500 ký tự)',
  })
  @IsNotEmpty({ message: 'Lời nhắn không được để trống' })
  @IsString()
  @MaxLength(500, { message: 'Lời nhắn không được quá 500 ký tự' })
  message: string;
}
