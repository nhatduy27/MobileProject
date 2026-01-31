import { IsString, IsNotEmpty, IsNumber, Min, MaxLength, IsInt } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { Transform } from 'class-transformer';

export class CreateProductDto {
  @ApiProperty({ example: 'Cơm sườn nướng', description: 'Product name' })
  @IsString()
  @IsNotEmpty({ message: 'Tên sản phẩm không được để trống' })
  @MaxLength(100, { message: 'Tên sản phẩm tối đa 100 ký tự' })
  name: string;

  @ApiProperty({
    example: 'Cơm sườn nướng mật ong + trứng',
    description: 'Product description',
  })
  @IsString()
  @IsNotEmpty({ message: 'Mô tả không được để trống' })
  @MaxLength(500, { message: 'Mô tả tối đa 500 ký tự' })
  description: string;

  @ApiProperty({ example: 35000, description: 'Price in VND' })
  @Transform(({ value }) => {
    const parsed = parseFloat(value);
    return isNaN(parsed) ? value : parsed;
  })
  @IsNumber({}, { message: 'Giá phải là số' })
  @Min(1000, { message: 'Giá tối thiểu 1,000đ' })
  price: number;

  @ApiProperty({ example: 'cat_123', description: 'Category ID' })
  @IsString()
  @IsNotEmpty({ message: 'Category không được để trống' })
  categoryId: string;

  @ApiProperty({
    type: 'string',
    format: 'binary',
    isArray: true,
    description: 'Product images',
  })
  images: any;

  @ApiProperty({ example: 15, description: 'Preparation time in minutes' })
  @Transform(({ value }) => {
    const parsed = parseInt(value, 10);
    return isNaN(parsed) ? value : parsed;
  })
  @IsInt({ message: 'Thời gian chuẩn bị phải là số nguyên' })
  @Min(5, { message: 'Thời gian chuẩn bị tối thiểu 5 phút' })
  preparationTime: number;
}

