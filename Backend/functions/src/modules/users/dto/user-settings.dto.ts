import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsIn, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';

export class NotificationSettingsDto {
  @ApiProperty({ description: 'Receive order update notifications', default: true })
  @IsBoolean()
  orderUpdates: boolean;

  @ApiProperty({ description: 'Receive promotion notifications', default: true })
  @IsBoolean()
  promotions: boolean;

  @ApiProperty({ description: 'Receive email notifications', default: true })
  @IsBoolean()
  email: boolean;

  @ApiProperty({ description: 'Receive push notifications', default: true })
  @IsBoolean()
  push: boolean;
}

export class UserSettingsDto {
  @ApiPropertyOptional({ type: NotificationSettingsDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => NotificationSettingsDto)
  notifications?: NotificationSettingsDto;

  @ApiPropertyOptional({ description: 'Language preference', enum: ['vi', 'en'], default: 'vi' })
  @IsOptional()
  @IsIn(['vi', 'en'])
  language?: 'vi' | 'en';

  @ApiPropertyOptional({ description: 'Currency', default: 'VND' })
  @IsOptional()
  @IsIn(['VND'])
  currency?: string;
}
