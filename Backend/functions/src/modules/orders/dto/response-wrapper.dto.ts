import { ApiProperty } from '@nestjs/swagger';

/**
 * Generic response wrapper for success responses
 */
export class ResponseWrapperDto<T> {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Response data',
  })
  data: T;

  @ApiProperty({
    description: 'Response timestamp (ISO-8601)',
    example: '2026-01-18T15:12:20.059Z',
  })
  timestamp: string;
}

/**
 * Wrapper specifically for OrderEntity responses (detail/cancel)
 */
export class OrderResponseDto {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Order detail data (all timestamps as ISO-8601 strings)',
    example: {
      id: 'order_abc123def456',
      orderNumber: 'ORD-1705591320000-A2B3C4',
      customerId: 'user_cust_001',
      shopId: 'shop_123',
      shopName: 'Cơm Tấm Sườn',
      shipperId: null,
      items: [
        {
          productId: 'prod_123',
          productName: 'Cơm sườn bì chả',
          quantity: 2,
          price: 25000,
          subtotal: 50000,
        },
      ],
      subtotal: 50000,
      shipFee: 15000,
      discount: 10000,
      voucherCode: 'SUMMER20',
      total: 55000,
      status: 'CANCELLED',
      paymentStatus: 'UNPAID',
      paymentMethod: 'COD',
      deliveryAddress: {
        label: 'KTX B5',
        fullAddress: 'KTX Khu B - Tòa B5',
        building: 'B5',
        room: '101',
        note: 'Gọi trước 5 phút',
      },
      deliveryNote: 'Call before delivery',
      cancelReason: 'Changed my mind',
      cancelledBy: 'CUSTOMER',
      cancelledAt: '2026-01-18T15:12:20.059Z',
      createdAt: '2026-01-18T14:00:00.000Z',
      updatedAt: '2026-01-18T15:12:20.059Z',
      confirmedAt: null,
      preparingAt: null,
      readyAt: null,
      shippingAt: null,
      deliveredAt: null,
      reviewId: null,
      reviewedAt: null,
      paidOut: false,
      paidOutAt: null,
    },
  })
  data: any;

  @ApiProperty({
    description: 'Response timestamp (ISO-8601)',
    example: '2026-01-18T15:12:21.000Z',
  })
  timestamp: string;
}

/**
 * Wrapper for paginated orders responses (list)
 */
export class PaginatedOrdersResponseDto {
  @ApiProperty({
    description: 'Success status',
    example: true,
  })
  success: boolean;

  @ApiProperty({
    description: 'Paginated orders data (list items with ISO-8601 timestamps)',
    example: {
      orders: [
        {
          id: 'order_abc123def456',
          orderNumber: 'ORD-1705591320000-A2B3C4',
          shopId: 'shop_123',
          shopName: 'Cơm Tấm Sườn',
          status: 'PENDING',
          paymentStatus: 'UNPAID',
          total: 85000,
          discount: 0,
          voucherCode: null,
          itemCount: 2,
          createdAt: '2026-01-19T10:00:00.000Z',
          updatedAt: '2026-01-19T10:30:00.000Z',
          itemsPreview: [
            {
              productId: 'prod_123',
              productName: 'Cơm sườn bì chả',
              quantity: 2,
              price: 35000,
              subtotal: 70000,
            },
          ],
          itemsPreviewCount: 1,
          customer: {
            id: 'user_cust_001',
            displayName: 'Nguyễn Văn A',
            phone: '0901234567',
          },
          paymentMethod: 'COD',
          deliveryAddress: {
            label: 'KTX B5',
            fullAddress: 'KTX Khu B - Tòa B5',
            building: 'B5',
            room: '101',
          },
          shipperId: null,
        },
        {
          id: 'order_def456ghi789',
          orderNumber: 'ORD-1705590600000-X1Y2Z3',
          shopId: 'shop_123',
          shopName: 'Cơm Tấm Sườn',
          status: 'SHIPPING',
          paymentStatus: 'PAID',
          total: 125000,
          discount: 15000,
          voucherCode: 'FREESHIP50',
          itemCount: 3,
          createdAt: '2026-01-19T09:00:00.000Z',
          updatedAt: '2026-01-19T11:00:00.000Z',
          itemsPreview: [
            {
              productId: 'prod_002',
              productName: 'Cơm tấm sườn',
              quantity: 1,
              price: 45000,
              subtotal: 45000,
            },
            {
              productId: 'prod_003',
              productName: 'Trà đá',
              quantity: 2,
              price: 5000,
              subtotal: 10000,
            },
          ],
          itemsPreviewCount: 2,
          customer: {
            id: 'user_cust_002',
            displayName: 'Trần Thị B',
            phone: null,
          },
          paymentMethod: 'ZALOPAY',
          deliveryAddress: {
            label: 'KTX B8',
            fullAddress: 'KTX Khu B - Tòa B8',
          },
          shipperId: 'shipper_456',
          shipper: {
            id: 'shipper_456',
            displayName: 'Nguyễn Văn Shipper',
            phone: '0909999999',
          },
        },
      ],
      page: 1,
      limit: 20,
      total: 50,
      totalPages: 3,
    },
  })
  data: any;

  @ApiProperty({
    description: 'Response timestamp (ISO-8601)',
    example: '2026-01-19T07:30:00.000Z',
  })
  timestamp: string;
}
