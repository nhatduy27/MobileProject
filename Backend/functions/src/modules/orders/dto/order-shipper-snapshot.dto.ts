import { ApiProperty } from '@nestjs/swagger';

export class OrderShipperSnapshotDto {
  @ApiProperty({
    description: 'Shipper user ID',
    example: 'user_ship_001',
  })
  id: string;

  @ApiProperty({
    description: 'Shipper display name',
    example: 'Nguyễn Văn Ship',
    required: false,
  })
  displayName?: string;

  @ApiProperty({
    description: 'Shipper phone number',
    example: '0912345678',
    required: false,
  })
  phone?: string;
}
