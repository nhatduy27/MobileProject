import { ApiProperty } from '@nestjs/swagger';
import { IsString } from 'class-validator';

export class UpdateFcmTokenDto {
  @ApiProperty({ description: 'FCM token for push notifications' })
  @IsString()
  fcmToken: string;

  @ApiProperty({ description: 'Device ID' })
  @IsString()
  deviceId: string;
}
