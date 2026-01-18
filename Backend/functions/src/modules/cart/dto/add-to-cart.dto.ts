import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsNotEmpty, IsInt, Min, Max } from 'class-validator';

export class AddToCartDto {
  @ApiProperty({ 
    description: 'Product ID to add to cart',
    example: 'prod_abc123',
    required: true
  })
  @IsString()
  @IsNotEmpty()
  productId: string;

  @ApiProperty({ 
    description: 'Quantity to add (increments existing quantity if product already in cart)',
    example: 2, 
    minimum: 1, 
    maximum: 999,
    required: true,
    type: 'integer'
  })
  @IsInt()
  @Min(1, { message: 'Số lượng tối thiểu là 1' })
  @Max(999, { message: 'Số lượng tối đa là 999' })
  quantity: number;
}
