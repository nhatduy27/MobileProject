import { IsString, IsNumber, Min, IsOptional } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';

export class ValidateVoucherDto {
  @ApiProperty({
    example: 'FREESHIP50',
    description: 'Voucher code to validate',
  })
  @IsString()
  @Transform(({ value }) => value?.toUpperCase())
  code: string;

  @ApiProperty({
    example: 'shop_123',
    description: 'Shop ID',
  })
  @IsString()
  shopId: string;

  @ApiProperty({
    example: 85000,
    description: 'Order subtotal (before shipping, before discount)',
  })
  @IsNumber()
  @Min(0)
  subtotal: number;

  @ApiPropertyOptional({
    example: 15000,
    description: 'Shipping fee (required for FREE_SHIP validation)',
  })
  @IsNumber()
  @Min(0)
  @IsOptional()
  shipFee?: number;
}
