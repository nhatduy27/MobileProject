import { IsOptional, IsString, IsInt, Min, Max } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * List Messages Query DTO
 *
 * Query parameters for listing messages with pagination.
 */
export class ListMessagesQueryDto {
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  @ApiPropertyOptional({
    description: 'Number of messages to return (1-50)',
    default: 20,
    minimum: 1,
    maximum: 50,
  })
  limit?: number = 20;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({
    description: 'Cursor for pagination (message ID to start after)',
    example: 'msg_abc123',
  })
  startAfter?: string;
}
