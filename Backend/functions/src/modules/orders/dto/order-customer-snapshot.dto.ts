import { ApiProperty } from '@nestjs/swagger';

/**
 * Lightweight customer snapshot for OWNER order list
 * Stored at order creation to avoid N+1 queries
 */
export class OrderCustomerSnapshotDto {
  @ApiProperty({
    description: 'Customer user ID',
    example: 'user_cust_001',
  })
  id: string;

  @ApiProperty({
    description: 'Customer display name',
    example: 'Nguyễn Văn A',
    required: false,
  })
  displayName?: string;

  @ApiProperty({
    description: 'Customer phone number',
    example: '0901234567',
    required: false,
  })
  phone?: string;
}
