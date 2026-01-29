import {
  IsArray,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  ArrayMinSize,
  ArrayMaxSize,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Location DTO
 */
export class LocationDto {
  @ApiProperty({ example: 10.773589 })
  @IsNumber()
  @IsNotEmpty()
  lat: number;

  @ApiProperty({ example: 106.659924 })
  @IsNumber()
  @IsNotEmpty()
  lng: number;

  @ApiProperty({ example: 'Cổng chính KTX', required: false })
  @IsOptional()
  @IsString()
  name?: string;
}

/**
 * Create Optimized Trip Request DTO
 */
export class CreateOptimizedTripDto {
  @ApiProperty({
    description: 'List of order IDs to include in trip',
    example: ['order_001', 'order_002', 'order_003'],
    minItems: 1,
    maxItems: 15,
  })
  @IsArray()
  @ArrayMinSize(1, { message: 'At least 1 order is required' })
  @ArrayMaxSize(15, { message: 'Maximum 15 orders per trip' })
  @IsString({ each: true })
  orderIds: string[];

  @ApiProperty({
    description: 'Starting location (shipper current position)',
    type: LocationDto,
  })
  @ValidateNested()
  @Type(() => LocationDto)
  @IsNotEmpty()
  origin: LocationDto;

  @ApiProperty({
    description: 'Return destination (hub/gate). If omitted, uses origin as return point.',
    type: LocationDto,
    required: false,
  })
  @ValidateNested()
  @Type(() => LocationDto)
  @IsOptional()
  returnTo?: LocationDto;
}
