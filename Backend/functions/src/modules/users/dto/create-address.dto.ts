import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsBoolean, MaxLength } from 'class-validator';

export class CreateAddressDto {
  @ApiProperty({ description: 'Address label', example: 'Nhà' })
  @IsString()
  @MaxLength(50)
  label: string;

  @ApiProperty({ description: 'Full address', example: 'Tòa A, Phòng 101, KTX Khu B' })
  @IsString()
  @MaxLength(200)
  fullAddress: string;

  @ApiPropertyOptional({ description: 'Building name/code', example: 'A' })
  @IsOptional()
  @IsString()
  @MaxLength(50)
  building?: string;

  @ApiPropertyOptional({ description: 'Room number', example: '101' })
  @IsOptional()
  @IsString()
  @MaxLength(20)
  room?: string;

  @ApiPropertyOptional({ description: 'Additional notes', example: 'Gọi trước khi đến' })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  note?: string;

  @ApiPropertyOptional({ description: 'Set as default address', default: false })
  @IsOptional()
  @IsBoolean()
  isDefault?: boolean;
}
