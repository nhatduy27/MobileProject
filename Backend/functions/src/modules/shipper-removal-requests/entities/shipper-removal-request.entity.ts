import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Timestamp } from '@google-cloud/firestore';

export enum RemovalRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

/**
 * Type of removal request
 * - QUIT: Shipper wants to quit completely (become CUSTOMER)
 * - TRANSFER: Shipper wants to leave this shop to work at another shop (stay SHIPPER)
 */
export enum RemovalRequestType {
  QUIT = 'QUIT',
  TRANSFER = 'TRANSFER',
}

/**
 * Shipper Removal Request Entity
 * Collection: shipper_removal_requests
 * 
 * Created when a shipper wants to leave a shop they are working for
 */
export class ShipperRemovalRequestEntity {
  @ApiProperty({ example: 'srr_abc123' })
  id: string;

  @ApiProperty({ example: 'uid_shipper_123' })
  shipperId: string;

  @ApiProperty({ example: 'Nguyễn Văn Shipper' })
  shipperName: string;

  @ApiProperty({ example: '0901234567' })
  shipperPhone?: string;

  @ApiProperty({ example: 'shop_abc456' })
  shopId: string;

  @ApiProperty({ example: 'Quán A Mập' })
  shopName: string;

  @ApiProperty({ example: 'uid_owner_789' })
  ownerId: string;

  @ApiProperty({ 
    enum: RemovalRequestType, 
    example: RemovalRequestType.TRANSFER,
    description: 'QUIT = nghỉ hẳn (trở thành CUSTOMER), TRANSFER = chuyển shop (giữ role SHIPPER)'
  })
  type: RemovalRequestType;

  @ApiPropertyOptional({ 
    example: 'Tôi bận việc riêng, không thể tiếp tục làm việc',
    description: 'Reason from shipper (optional)'
  })
  reason?: string;

  @ApiProperty({ enum: RemovalRequestStatus, example: RemovalRequestStatus.PENDING })
  status: RemovalRequestStatus;

  @ApiPropertyOptional({
    example: 'Cần shipper trong thời gian cao điểm',
    description: 'Rejection reason from owner'
  })
  rejectionReason?: string;

  @ApiPropertyOptional({ description: 'When the request was processed' })
  processedAt?: Timestamp | Date | string;

  @ApiPropertyOptional({ description: 'Owner who processed the request' })
  processedBy?: string;

  @ApiProperty({ description: 'When the request was created' })
  createdAt: Timestamp | Date | string;

  constructor(data: Partial<ShipperRemovalRequestEntity>) {
    Object.assign(this, data);
  }
}
