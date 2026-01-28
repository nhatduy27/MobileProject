import { IsEnum } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { RevenuePeriod } from '../entities/revenue-analytics.entity';

/**
 * Query DTO for GET /owner/revenue
 */
export class RevenueQueryDto {
  @ApiProperty({
    enum: RevenuePeriod,
    example: RevenuePeriod.TODAY,
    description: 'Period for revenue analytics',
    required: true,
  })
  @IsEnum(RevenuePeriod, {
    message: 'Invalid period parameter. Must be one of: today, week, month, year',
  })
  period: RevenuePeriod;

  constructor(partial: Partial<RevenueQueryDto>) {
    Object.assign(this, partial);
  }
}
