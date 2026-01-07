import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CategoryResponseDto {
  @ApiProperty({
    description: 'Category ID',
    example: 'cat_abc123',
  })
  id: string;

  @ApiProperty({
    description: 'Category name',
    example: 'Cơm',
  })
  name: string;

  @ApiProperty({
    description: 'Category description',
    example: 'Các món cơm đa dạng',
  })
  description: string;

  @ApiProperty({
    description: 'Icon/Image URL',
    example: 'https://storage.googleapis.com/ktx-delivery/categories/com.png',
  })
  iconUrl: string;

  @ApiProperty({
    description: 'Sort order for display',
    example: 1,
  })
  sortOrder: number;

  @ApiProperty({
    description: 'Whether category is active',
    example: true,
  })
  isActive: boolean;

  @ApiPropertyOptional({
    description: 'Created timestamp',
  })
  createdAt?: Date;

  @ApiPropertyOptional({
    description: 'Updated timestamp',
  })
  updatedAt?: Date;
}
