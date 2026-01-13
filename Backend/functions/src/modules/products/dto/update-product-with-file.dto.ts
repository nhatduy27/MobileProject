import {
  IsString,
  IsNumber,
  Min,
  MaxLength,
  IsInt,
  IsOptional,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class UpdateProductWithFileDto {
  @ApiPropertyOptional({ example: 'Cơm sườn nướng', description: 'Product name' })
  @IsOptional()
  @IsString()
  @MaxLength(100, { message: 'Tên sản phẩm tối đa 100 ký tự' })
  name?: string;

  @ApiPropertyOptional({
    example: 'Cơm sườn nướng mật ong + trứng',
    description: 'Product description',
  })
  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Mô tả tối đa 500 ký tự' })
  description?: string;

  @ApiPropertyOptional({ example: 35000, description: 'Price in VND' })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1000, { message: 'Giá tối thiểu 1,000đ' })
  price?: number;

  @ApiPropertyOptional({ example: 'cat_123', description: 'Category ID' })
  @IsOptional()
  @IsString()
  categoryId?: string;

  @ApiPropertyOptional({
    type: 'string',
    format: 'binary',
    description: 'Product image (optional)',
  })
  image?: any;

  @ApiPropertyOptional({ example: 15, description: 'Preparation time in minutes' })
  @IsOptional()
  @Type(() => Number)
  @IsInt({ message: 'Thời gian chuẩn bị phải là số nguyên' })
  @Min(5, { message: 'Thời gian chuẩn bị tối thiểu 5 phút' })
  preparationTime?: number;
}
