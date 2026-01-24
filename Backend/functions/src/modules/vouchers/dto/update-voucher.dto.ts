import { IsString, IsNumber, IsOptional, Min, IsBoolean, IsISO8601, Length } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

export class UpdateVoucherDto {
  @ApiPropertyOptional({
    example: 200,
    description: 'Update total usage limit',
  })
  @IsNumber()
  @Min(1)
  @IsOptional()
  usageLimit?: number;

  @ApiPropertyOptional({
    example: 2,
    description: 'Update per-user usage limit',
  })
  @IsNumber()
  @Min(1)
  @IsOptional()
  usageLimitPerUser?: number;

  @ApiPropertyOptional({
    example: '2026-12-31T23:59:59Z',
    description: 'Update valid to date',
  })
  @IsISO8601()
  @IsOptional()
  validTo?: string;

  @ApiPropertyOptional({
    example: 'Updated description',
    description: 'Update description',
  })
  @IsString()
  @Length(1, 500)
  @IsOptional()
  description?: string;

  @ApiPropertyOptional({
    example: 'Updated name',
    description: 'Update name',
  })
  @IsString()
  @Length(1, 100)
  @IsOptional()
  name?: string;
}

export class UpdateVoucherStatusDto {
  @ApiPropertyOptional({
    example: false,
    description: 'Set active status',
  })
  @IsBoolean()
  isActive: boolean;
}
