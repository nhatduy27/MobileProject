import { ApiProperty } from '@nestjs/swagger';

export class CartItemDto {
  @ApiProperty({ example: 'prod_abc' })
  productId: string;

  @ApiProperty({ example: 'shop_1', description: 'Shop ID (denormalized for quick access)' })
  shopId: string;

  @ApiProperty({ example: 'Cơm sườn nướng' })
  productName: string;

  @ApiProperty({ example: 'https://...', required: false })
  productImage?: string;

  @ApiProperty({ example: 2 })
  quantity: number;

  @ApiProperty({ example: 35000, description: 'Price at add time (locked when added)' })
  price: number;

  @ApiProperty({ example: 70000, description: 'price * quantity' })
  subtotal: number;

  @ApiProperty({ example: '2026-01-18T08:00:00.000Z', description: 'When item was added to cart' })
  addedAt: string;

  @ApiProperty({ example: '2026-01-18T10:30:00.000Z', description: 'When item was last updated' })
  updatedAt: string;
}
