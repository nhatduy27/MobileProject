import { IsString, IsOptional, ValidateNested, ValidateIf } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

/**
 * KTX-style delivery address for checkout
 * Matches user AddressBook format
 */
export class DeliveryAddressDto {
  @ApiPropertyOptional({
    description: 'Address label (e.g., "Nhà", "Phòng ký túc xá")',
    example: 'KTX B5',
  })
  @IsString()
  @IsOptional()
  label?: string;

  @ApiProperty({
    description: 'Full address (KTX format)',
    example: 'KTX Khu B - Tòa B5',
  })
  @IsString()
  fullAddress: string;

  @ApiPropertyOptional({
    description: 'Building identifier',
    example: 'B5',
  })
  @IsString()
  @IsOptional()
  building?: string;

  @ApiPropertyOptional({
    description: 'Room number',
    example: '101',
  })
  @IsString()
  @IsOptional()
  room?: string;

  @ApiPropertyOptional({
    description: 'Delivery instructions/note',
    example: 'Gọi trước khi đến',
  })
  @IsString()
  @IsOptional()
  note?: string;

  // Legacy fields - DEPRECATED, kept for backward compatibility
  @ApiPropertyOptional({
    description: '(DEPRECATED) Street address - use fullAddress instead',
    example: '123 Nguyen Hue',
    deprecated: true,
  })
  @IsString()
  @IsOptional()
  street?: string;

  @ApiPropertyOptional({
    description: '(DEPRECATED) Ward name - use fullAddress instead',
    example: 'Ben Nghe',
    deprecated: true,
  })
  @IsString()
  @IsOptional()
  ward?: string;

  @ApiPropertyOptional({
    description: '(DEPRECATED) District name - use fullAddress instead',
    example: 'District 1',
    deprecated: true,
  })
  @IsString()
  @IsOptional()
  district?: string;

  @ApiPropertyOptional({
    description: '(DEPRECATED) City name - use fullAddress instead',
    example: 'Ho Chi Minh City',
    deprecated: true,
  })
  @IsString()
  @IsOptional()
  city?: string;

  @ApiPropertyOptional({
    description: '(DEPRECATED) GPS coordinates - optional',
    example: { lat: 10.7769, lng: 106.7009 },
    deprecated: true,
  })
  @IsOptional()
  coordinates?: {
    lat: number;
    lng: number;
  };
}

export class CreateOrderDto {
  @ApiProperty({
    description: 'Shop ID to create order from',
    example: 'shop_123',
  })
  @IsString()
  shopId: string;

  @ApiPropertyOptional({
    description:
      'ALTERNATIVE: Reference to saved address by ID (advanced use). Standard flow: frontend should fetch addresses, let user select/edit, then submit deliveryAddress snapshot.',
    example: 'addr_abc123',
  })
  @IsString()
  @IsOptional()
  deliveryAddressId?: string;

  @ApiPropertyOptional({
    description:
      'Delivery address snapshot (RECOMMENDED). Frontend: GET /me/addresses, auto-select isDefault, allow user to edit note, then POST with this deliveryAddress object.',
    type: DeliveryAddressDto,
  })
  @ValidateIf((o) => !o.deliveryAddressId)
  @ValidateNested()
  @Type(() => DeliveryAddressDto)
  deliveryAddress?: DeliveryAddressDto;

  @ApiPropertyOptional({
    description: 'Additional delivery notes (overrides address note)',
    example: 'Gọi trước 5 phút',
  })
  @IsString()
  @IsOptional()
  deliveryNote?: string;

  @ApiProperty({
    description: 'Payment method',
    enum: ['COD', 'SEPAY'],
    example: 'COD',
  })
  @IsString()
  paymentMethod: 'COD' | 'SEPAY';

  @ApiPropertyOptional({
    description: 'Voucher code to apply (optional for all payment methods)',
    example: 'FREESHIP10',
  })
  @IsString()
  @IsOptional()
  voucherCode?: string;
}
