import { IsEnum, IsOptional, IsInt, Min, Max, IsString } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * Sort options for buyer list
 */
export enum BuyerSortBy {
  CREATED_AT = 'createdAt',
  TOTAL_SPENT = 'totalSpent',
}

/**
 * Query DTO for GET /owner/buyers
 */
export class ListBuyersQueryDto {
  @ApiProperty({ example: 1, required: false, default: 1 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  page?: number = 1;

  @ApiProperty({ example: 20, required: false, default: 20, maximum: 50 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  limit?: number = 20;

  @ApiProperty({
    enum: ['ALL', ...Object.values(BuyerTier)],
    example: 'ALL',
    required: false,
    default: 'ALL',
  })
  @IsOptional()
  @IsString()
  tier?: 'ALL' | BuyerTier = 'ALL';

  @ApiProperty({ example: 'nguyễn', required: false })
  @IsOptional()
  @IsString()
  search?: string;

  @ApiProperty({
    enum: BuyerSortBy,
    example: BuyerSortBy.CREATED_AT,
    required: false,
    default: BuyerSortBy.CREATED_AT,
  })
  @IsOptional()
  @IsEnum(BuyerSortBy)
  sort?: BuyerSortBy = BuyerSortBy.CREATED_AT;

  constructor(partial: Partial<ListBuyersQueryDto>) {
    Object.assign(this, partial);
  }
}
