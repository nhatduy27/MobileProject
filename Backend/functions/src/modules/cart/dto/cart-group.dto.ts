import { ApiProperty } from '@nestjs/swagger';
import { CartItemDto } from './cart-item.dto';

export class CartGroupDto {
  @ApiProperty({ example: 'shop_123' })
  shopId: string;

  @ApiProperty({ example: 'Quán Phở Việt', description: 'Shop name (denormalized)' })
  shopName: string;

  @ApiProperty({ example: true, description: 'Shop is open and accepting orders' })
  isOpen: boolean;

  @ApiProperty({ example: 0, description: 'Ship fee (always 0 in cart, calculated at order stage)' })
  shipFee: number;

  @ApiProperty({ type: () => CartItemDto, isArray: true })
  items: CartItemDto[];

  @ApiProperty({ example: 70000, description: 'Sum of all items in this group (price * quantity)' })
  subtotal: number;
}
