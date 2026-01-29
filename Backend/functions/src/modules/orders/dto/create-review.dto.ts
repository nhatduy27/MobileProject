import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsString,
  IsInt,
  IsOptional,
  Min,
  Max,
  IsArray,
  ValidateNested,
  MaxLength,
  IsNotEmpty,
} from 'class-validator';
import { Type } from 'class-transformer';

/**
 * DTO for individual product review within an order review
 */
export class ProductReviewDto {
  @ApiProperty({
    description: 'Product ID',
    example: 'prod_abc123',
  })
  @IsString()
  @IsNotEmpty()
  productId: string;

  @ApiProperty({
    description: 'Rating for this product (1-5)',
    example: 5,
    minimum: 1,
    maximum: 5,
  })
  @IsInt()
  @Min(1, { message: 'Rating phải từ 1 đến 5' })
  @Max(5, { message: 'Rating phải từ 1 đến 5' })
  rating: number;

  @ApiPropertyOptional({
    description: 'Comment for this product',
    example: 'Phở bò rất ngon!',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Comment không được quá 500 ký tự' })
  comment?: string;
}

/**
 * DTO for creating an order review
 * Supports both shop-level rating and individual product ratings
 */
export class CreateReviewDto {
  @ApiProperty({
    description: 'Order ID to review',
    example: 'order_xyz789',
  })
  @IsString()
  @IsNotEmpty()
  orderId: string;

  @ApiProperty({
    description: 'Overall rating for the shop (1-5)',
    example: 5,
    minimum: 1,
    maximum: 5,
  })
  @IsInt()
  @Min(1, { message: 'Rating phải từ 1 đến 5' })
  @Max(5, { message: 'Rating phải từ 1 đến 5' })
  rating: number;

  @ApiPropertyOptional({
    description: 'Overall comment for the shop',
    example: 'Quán tuyệt vời, ship nhanh!',
    maxLength: 1000,
  })
  @IsOptional()
  @IsString()
  @MaxLength(1000, { message: 'Comment không được quá 1000 ký tự' })
  comment?: string;

  @ApiPropertyOptional({
    description: 'Individual product reviews',
    type: [ProductReviewDto],
    example: [
      { productId: 'pho_bo_id', rating: 5, comment: 'Phở bò rất ngon!' },
      { productId: 'tra_sua_id', rating: 4, comment: 'Trà sữa hơi ngọt' },
    ],
  })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ProductReviewDto)
  productReviews?: ProductReviewDto[];
}
