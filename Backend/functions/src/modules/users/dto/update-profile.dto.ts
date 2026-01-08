import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, Matches, MaxLength, MinLength } from 'class-validator';

export class UpdateProfileDto {
  @ApiPropertyOptional({ description: 'Display name', example: 'Nguyễn Văn B' })
  @IsOptional()
  @IsString()
  @MinLength(2)
  @MaxLength(100)
  displayName?: string;

  @ApiPropertyOptional({ description: 'Phone number', example: '0909876543' })
  @IsOptional()
  @IsString()
  @Matches(/^(0[3|5|7|8|9])+([0-9]{8})$/, {
    message: 'Phone number must be a valid Vietnamese phone number',
  })
  phone?: string;

  @ApiPropertyOptional({ description: 'Avatar URL' })
  @IsOptional()
  @IsString()
  avatarUrl?: string;
}
