import { ApiProperty } from '@nestjs/swagger';
import { ShipperTrip } from '../entities/shipper-trip.entity';

/**
 * Paginated Trips Response DTO
 */
export class PaginatedTripsDto {
  @ApiProperty({
    description: 'List of trips',
    type: 'array',
  })
  items: ShipperTrip[];

  @ApiProperty({
    description: 'Current page number (1-based)',
    example: 1,
  })
  page: number;

  @ApiProperty({
    description: 'Number of items per page',
    example: 20,
  })
  limit: number;

  @ApiProperty({
    description: 'Total number of trips matching filter',
    example: 42,
  })
  total: number;

  @ApiProperty({
    description: 'Total number of pages',
    example: 3,
  })
  totalPages: number;

  @ApiProperty({
    description: 'Whether there are more pages',
    example: true,
  })
  hasNext: boolean;
}
