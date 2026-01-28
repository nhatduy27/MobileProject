import { ApiProperty } from '@nestjs/swagger';
import { BuyerTier } from '../entities/shop-buyer.entity';

/**
 * DTO for buyer list item
 * Returned in paginated list
 */
export class BuyerListItemDto {
  @ApiProperty({ example: 'cust_123', description: 'Mã khách hàng' })
  customerId: string;

  @ApiProperty({ example: 'Nguyễn Văn A', description: 'Tên hiển thị' })
  displayName: string;

  @ApiProperty({ example: '0912345678', required: false, description: 'Số điện thoại' })
  phone?: string;

  @ApiProperty({ example: 'https://...', required: false, description: 'Avatar' })
  avatar?: string;

  @ApiProperty({
    enum: BuyerTier,
    enumName: 'BuyerTier', // Giúp Swagger hiển thị tên Enum rõ ràng
    example: BuyerTier.VIP,
    description: 'Hạng thành viên',
  })
  tier: BuyerTier;

  @ApiProperty({ example: 25, description: 'Tổng số đơn hàng' })
  totalOrders: number;

  @ApiProperty({ example: 2500000, description: 'Tổng chi tiêu' })
  totalSpent: number;

  @ApiProperty({ example: 100000, description: 'Giá trị trung bình đơn' })
  avgOrderValue: number;

  @ApiProperty({ example: '2024-06-15T10:00:00Z', description: 'Ngày tham gia (đơn đầu tiên)' })
  joinedDate: string;

  @ApiProperty({
    example: '2025-01-20T14:30:00Z',
    required: false,
    description: 'Đơn hàng gần nhất',
  })
  lastOrderDate?: string;

  // 👇 Constructor thần thánh: Giúp map 1 dòng là xong
  constructor(partial: Partial<BuyerListItemDto>) {
    Object.assign(this, partial);
  }
}

/**
 * Paginated buyer list response
 */
export class PaginatedBuyerListDto {
  @ApiProperty({ type: [BuyerListItemDto], description: 'Danh sách khách hàng' })
  items: BuyerListItemDto[];

  @ApiProperty({
    description: 'Thông tin phân trang',
    example: {
      page: 1,
      limit: 20,
      total: 150,
      totalPages: 8,
    },
  })
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };

  constructor(partial: Partial<PaginatedBuyerListDto>) {
    Object.assign(this, partial);
  }
}
