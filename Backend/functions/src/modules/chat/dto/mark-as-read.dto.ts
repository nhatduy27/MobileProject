import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Mark As Read DTO
 *
 * Used to mark a message as read.
 */
export class MarkAsReadDto {
  @IsString()
  @IsNotEmpty()
  @ApiProperty({
    description: 'Conversation ID containing the message',
    example: 'user123__user456',
  })
  conversationId: string;
}
