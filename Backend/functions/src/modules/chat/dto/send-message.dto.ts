import { IsString, IsNotEmpty, MaxLength } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * Send Message DTO
 *
 * Used to send a text message in a conversation.
 * Max 1000 characters.
 */
export class SendMessageDto {
  @IsString()
  @IsNotEmpty()
  @ApiProperty({
    description: 'Conversation ID to send message to',
    example: 'user123__user456',
  })
  conversationId: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(1000)
  @ApiProperty({
    description: 'Text message content (max 1000 characters)',
    example: 'Hello! How are you?',
    maxLength: 1000,
  })
  text: string;
}
