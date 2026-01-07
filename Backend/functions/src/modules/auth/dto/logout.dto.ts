import { IsString, IsOptional } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';

/**
 * Logout DTO
 * 
 * Request body for POST /auth/logout
 */
export class LogoutDto {
  @ApiPropertyOptional({
    description: 'FCM token to remove from user devices',
    example: 'fcm_token_xxx...',
  })
  @IsOptional()
  @IsString()
  fcmToken?: string;
}
