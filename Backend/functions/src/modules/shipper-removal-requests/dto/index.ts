import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsString,
  IsOptional,
  MaxLength,
  IsNotEmpty,
  IsIn,
  ValidateIf,
} from 'class-validator';
import { RemovalRequestType } from '../entities/shipper-removal-request.entity';

/**
 * DTO for shipper to create a removal request
 */
export class CreateRemovalRequestDto {
  @ApiProperty({
    description: 'ID of the shop to leave',
    example: 'shop_abc456',
  })
  @IsString()
  @IsNotEmpty({ message: 'shopId không được để trống' })
  shopId: string;

  @ApiProperty({
    description: 'Type of removal request',
    enum: RemovalRequestType,
    example: 'TRANSFER',
    default: 'TRANSFER',
  })
  @IsOptional()
  @IsString()
  @IsIn(['QUIT', 'TRANSFER'], { message: 'Type phải là QUIT hoặc TRANSFER' })
  type?: RemovalRequestType = RemovalRequestType.TRANSFER;

  @ApiPropertyOptional({
    description: 'Optional reason for leaving',
    example: 'Tôi muốn chuyển sang làm cho shop khác',
    maxLength: 500,
  })
  @IsOptional()
  @IsString()
  @MaxLength(500, { message: 'Lý do không được quá 500 ký tự' })
  reason?: string;
}

/**
 * DTO for owner to process (approve/reject) a removal request
 */
export class ProcessRemovalRequestDto {
  @ApiProperty({
    description: 'Action to take',
    enum: ['APPROVE', 'REJECT'],
    example: 'APPROVE',
  })
  @IsString()
  @IsIn(['APPROVE', 'REJECT'], { message: 'Action phải là APPROVE hoặc REJECT' })
  action: 'APPROVE' | 'REJECT';

  @ApiPropertyOptional({
    description: 'Reason for rejection (required when action is REJECT)',
    example: 'Chúng tôi cần bạn trong thời gian cao điểm',
    maxLength: 500,
  })
  @ValidateIf((o) => o.action === 'REJECT')
  @IsString({ message: 'Lý do từ chối là bắt buộc khi reject' })
  @IsNotEmpty({ message: 'Lý do từ chối là bắt buộc khi reject' })
  @MaxLength(500, { message: 'Lý do từ chối không được quá 500 ký tự' })
  rejectionReason?: string;
}

/**
 * Query parameters for listing removal requests
 */
export class RemovalRequestFilterDto {
  @ApiPropertyOptional({
    description: 'Filter by status',
    enum: ['PENDING', 'APPROVED', 'REJECTED'],
  })
  @IsOptional()
  @IsString()
  @IsIn(['PENDING', 'APPROVED', 'REJECTED'])
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
}
