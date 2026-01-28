import { IsOptional, IsString, IsInt, Min, Max } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * List Conversations Query DTO
 *
 * Query parameters for listing conversations with pagination.
 */
export class ListConversationsQueryDto {
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(50)
  @ApiPropertyOptional({
    description: 'Number of conversations to return (1-50)',
    default: 20,
    minimum: 1,
    maximum: 50,
  })
  limit?: number = 20;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({
    description: 'Cursor for pagination (conversation ID to start after)',
    example: 'user123__user456',
  })
  startAfter?: string;
}
