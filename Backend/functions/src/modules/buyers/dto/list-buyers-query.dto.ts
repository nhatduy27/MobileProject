import { IsOptional, IsEnum, IsInt, Min, Max, IsString, IsIn } from 'class-validator';
import { Type, Transform } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * Buyer sort options
 */
export enum BuyerSortBy {
  CREATED_AT = 'createdAt',
  TOTAL_SPENT = 'totalSpent',
}

/**
 * Query params for listing buyers
 */
export class ListBuyersQueryDto {
  @ApiProperty({ required: false, default: 1, minimum: 1, description: 'Số trang' })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @ApiProperty({
    required: false,
    default: 20,
    minimum: 1,
    maximum: 50,
    description: 'Số lượng/trang',
  })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  limit?: number = 20;

  @ApiProperty({
    required: false,
    enum: ['ALL', 'NEW', 'NORMAL', 'VIP'], // Hiển thị rõ trên Swagger
    default: 'ALL',
    description: 'Lọc theo hạng thành viên',
  })
  @IsOptional()
  @IsIn(['ALL', ...Object.values(BuyerTier)]) // 👇 Validation chặt chẽ: Chỉ chấp nhận giá trị hợp lệ
  tier?: 'ALL' | BuyerTier = 'ALL';

  @ApiProperty({
    required: false,
    description: 'Tìm kiếm theo Tên hoặc SĐT (Prefix match)',
    example: 'Nguyễn',
  })
  @IsOptional()
  @IsString()
  @Transform(({ value }) => value?.trim()) // 👇 Tự động cắt khoảng trắng thừa
  search?: string;

  @ApiProperty({
    required: false,
    enum: BuyerSortBy,
    default: BuyerSortBy.CREATED_AT,
    description: 'Sắp xếp theo',
  })
  @IsOptional()
  @IsEnum(BuyerSortBy)
  sort?: BuyerSortBy = BuyerSortBy.CREATED_AT;
}
