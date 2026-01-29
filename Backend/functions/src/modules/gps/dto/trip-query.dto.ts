import { IsOptional, IsString, IsEnum, IsNumber, Min, Max } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';
import { TripStatus } from '../entities/shipper-trip.entity';

/**
 * Get My Trip Request DTO
 */
export class GetMyTripDto {
  @ApiProperty({
    description: 'Trip ID to retrieve',
    example: 'trip_abc123',
  })
  @IsString()
  tripId: string;
}

/**
 * List My Trips Request DTO (Query params)
 */
export class ListMyTripsDto {
  @ApiProperty({
    description: 'Filter by trip status',
    enum: TripStatus,
    required: false,
    example: 'PENDING',
  })
  @IsOptional()
  @IsEnum(TripStatus)
  status?: TripStatus;

  @ApiProperty({
    description: 'Page number (1-based)',
    example: 1,
    default: 1,
    required: false,
  })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  page?: number;

  @ApiProperty({
    description: 'Number of items per page',
    example: 20,
    default: 20,
    minimum: 1,
    maximum: 50,
    required: false,
  })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  @Max(50)
  limit?: number;
}

/**
 * Start Trip Request DTO
 */
export class StartTripDto {
  @ApiProperty({
    description: 'Trip ID to start',
    example: 'trip_abc123',
  })
  @IsString()
  tripId: string;
}

/**
 * Finish Trip Request DTO
 */
export class FinishTripDto {
  @ApiProperty({
    description: 'Trip ID to finish',
    example: 'trip_abc123',
  })
  @IsString()
  tripId: string;
}

/**
 * Cancel Trip Request DTO
 */
export class CancelTripDto {
  @ApiProperty({
    description: 'Trip ID to cancel',
    example: 'trip_abc123',
  })
  @IsString()
  tripId: string;

  @ApiProperty({
    description: 'Optional reason for cancellation',
    example: 'Customer requested cancellation',
    required: false,
  })
  @IsOptional()
  @IsString()
  reason?: string;
}
