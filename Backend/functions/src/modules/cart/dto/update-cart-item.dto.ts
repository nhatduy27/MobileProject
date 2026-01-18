import { ApiProperty } from '@nestjs/swagger';
import { IsInt, Min, Max } from 'class-validator';

export class UpdateCartItemDto {
  @ApiProperty({ 
    description: 'New quantity for the cart item',
    example: 3, 
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
