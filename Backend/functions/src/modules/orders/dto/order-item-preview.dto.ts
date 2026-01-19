import { ApiProperty } from '@nestjs/swagger';

/**
 * Lightweight item preview for OWNER order list
 * Used to show a summary of ordered items without full detail
 */
export class OrderItemPreviewDto {
  @ApiProperty({
    description: 'Product ID',
    example: 'prod_123',
  })
  productId: string;

  @ApiProperty({
    description: 'Product name (snapshot at order time)',
    example: 'Cơm Tấm Sườn Bì Chả',
  })
  productName: string;

  @ApiProperty({
    description: 'Quantity ordered',
    example: 2,
  })
  quantity: number;

  @ApiProperty({
    description: 'Price per unit at order time',
    example: 35000,
  })
  price: number;

  @ApiProperty({
    description: 'Subtotal for this item (quantity * price)',
    example: 70000,
  })
  subtotal: number;
}
