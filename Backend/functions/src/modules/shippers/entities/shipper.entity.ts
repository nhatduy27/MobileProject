import { ApiProperty } from '@nestjs/swagger';
import { Timestamp } from '@google-cloud/firestore';

export enum ShipperStatus {
  AVAILABLE = 'AVAILABLE',
  BUSY = 'BUSY',
  OFFLINE = 'OFFLINE',
}

export enum VehicleType {
  MOTORBIKE = 'MOTORBIKE',
  CAR = 'CAR',
  BICYCLE = 'BICYCLE',
}

export class ShipperInfoEntity {
  @ApiProperty({ example: 'shop_abc' })
  shopId: string;

  @ApiProperty({ example: 'Quán A Mập' })
  shopName: string;

  @ApiProperty({ enum: VehicleType, example: VehicleType.MOTORBIKE })
  vehicleType: VehicleType;

  @ApiProperty({ example: '59X1-12345' })
  vehicleNumber: string;

  @ApiProperty({ enum: ShipperStatus, example: ShipperStatus.AVAILABLE })
  status: ShipperStatus;

  @ApiProperty({ example: true, description: 'Whether shipper is online to receive order notifications' })
  isOnline?: boolean;

  @ApiProperty({ example: 4.8 })
  rating: number;

  @ApiProperty({ example: 150 })
  totalDeliveries: number;

  @ApiProperty({ example: ['order_1', 'order_2'], type: [String] })
  currentOrders: string[];

  @ApiProperty()
  joinedAt: Timestamp;

  @ApiProperty({ required: false, description: 'Last time shipper went online' })
  lastOnlineAt?: Timestamp;

  constructor(data: Partial<ShipperInfoEntity>) {
    Object.assign(this, data);
  }
}

export class ShipperEntity {
  @ApiProperty({ example: 'uid_123' })
  id: string;

  @ApiProperty({ example: 'Nguyễn Văn A' })
  name: string;

  @ApiProperty({ example: '0901234567' })
  phone: string;

  @ApiProperty({ example: 'https://...' })
  avatar: string;

  @ApiProperty({ type: ShipperInfoEntity })
  shipperInfo: ShipperInfoEntity;

  constructor(data: Partial<ShipperEntity>) {
    Object.assign(this, data);
    if (data.shipperInfo) {
      this.shipperInfo = new ShipperInfoEntity(data.shipperInfo);
    }
  }
}
