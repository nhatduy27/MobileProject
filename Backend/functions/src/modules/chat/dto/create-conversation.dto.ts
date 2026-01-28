import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Create Conversation DTO
 *
 * Used to create or get a conversation with another user.
 * The conversation ID is deterministic based on sorted user IDs.
 */
export class CreateConversationDto {
  @IsString()
  @IsNotEmpty()
  @ApiProperty({
    description: 'ID of the user to chat with',
    example: 'user123',
  })
  participantId: string;
}
