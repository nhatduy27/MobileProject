import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * DTO for recent order in buyer detail
 */
export class RecentOrderDto {
  @ApiProperty({ example: 'order_456', description: 'Mã định danh đơn hàng' })
  orderId: string;

  @ApiProperty({ example: 'ORD-0001', description: 'Mã đơn hàng hiển thị' })
  orderNumber: string;

  @ApiProperty({ example: 125000, description: 'Tổng tiền đơn hàng' })
  total: number;

  @ApiProperty({ example: 'DELIVERED', description: 'Trạng thái đơn hàng' })
  status: string;

  @ApiProperty({ example: '2025-01-20T14:30:00Z', description: 'Ngày tạo đơn (ISO String)' })
  createdAt: string;

  // 👇 Constructor tiện lợi
  constructor(partial: Partial<RecentOrderDto>) {
    Object.assign(this, partial);
  }
}

/**
 * DTO for buyer detail view
 * Includes all stats + recent orders
 */
export class BuyerDetailDto {
  @ApiProperty({ example: 'cust_123', description: 'Mã khách hàng' })
  customerId: string;

  @ApiProperty({ example: 'Nguyễn Văn A', description: 'Tên hiển thị' })
  displayName: string;

  @ApiProperty({ example: '0912345678', required: false, description: 'Số điện thoại' })
  phone?: string;

  @ApiProperty({ example: 'https://...', required: false, description: 'Ảnh đại diện' })
  avatar?: string;

  @ApiProperty({ example: 'a@example.com', required: false, description: 'Email' })
  email?: string;

  @ApiProperty({
    enum: BuyerTier,
    enumName: 'BuyerTier',
    example: BuyerTier.VIP,
    description: 'Hạng thành viên (NEW/NORMAL/VIP)',
  })
  tier: BuyerTier;

  @ApiProperty({ example: 25, description: 'Tổng số đơn hàng thành công' })
  totalOrders: number;

  @ApiProperty({ example: 2500000, description: 'Tổng tiền đã chi tiêu' })
  totalSpent: number;

  @ApiProperty({ example: 100000, description: 'Giá trị trung bình mỗi đơn' })
  avgOrderValue: number;

  @ApiProperty({ example: '2024-06-15T10:00:00Z', description: 'Ngày tham gia (đơn đầu tiên)' })
  joinedDate: string;

  @ApiProperty({
    example: '2024-06-15T10:00:00Z',
    required: false,
    description: 'Ngày mua đơn đầu tiên',
  })
  firstOrderDate?: string;

  @ApiProperty({
    example: '2025-01-20T14:30:00Z',
    required: false,
    description: 'Ngày mua đơn gần nhất',
  })
  lastOrderDate?: string;

  @ApiProperty({ type: [RecentOrderDto], description: 'Danh sách 5 đơn hàng gần nhất' })
  recentOrders: RecentOrderDto[];

  constructor(partial: Partial<BuyerDetailDto>) {
    Object.assign(this, partial);
  }
}
