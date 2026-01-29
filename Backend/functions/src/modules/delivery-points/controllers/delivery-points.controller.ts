import { Controller, Get, UseGuards, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { DeliveryPointsService } from '../services/delivery-points.service';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * Delivery Points Controller
 *
 * Endpoints để quản lý điểm giao hàng (tòa nhà KTX).
 * Shared controller dùng cho tất cả roles: Customer, Shipper, Owner, Admin.
 *
 * IMPORTANT: Controller trả về RAW data.
 * Global TransformInterceptor sẽ tự động wrap response với {success, data, timestamp}.
 * KHÔNG tự wrap response để tránh double-wrapping.
 */
@ApiTags('Delivery Points')
@Controller('delivery-points')
@UseGuards(AuthGuard, RolesGuard)
@ApiBearerAuth('firebase-auth')
export class DeliveryPointsController {
  constructor(private readonly deliveryPointsService: DeliveryPointsService) {}

  /**
   * Lấy danh sách delivery points đang active
   *
   * Endpoint: GET /api/delivery-points
   * Auth: CUSTOMER, SHIPPER, OWNER, ADMIN
   */
  @Get()
  @Roles(UserRole.CUSTOMER, UserRole.SHIPPER, UserRole.OWNER, UserRole.ADMIN)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Lấy danh sách điểm giao hàng KTX',
    description:
      'Trả về tất cả các delivery points đang active (active=true), ' +
      'được sắp xếp theo block (A-E) và số (1-9). ' +
      'Endpoint read-only, chỉ đọc tọa độ từ location.lat/lng.',
  })
  @ApiResponse({
    status: 200,
    description: 'Lấy danh sách thành công',
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'A1',
            buildingCode: 'A1',
            name: 'Tòa A1',
            location: {
              lat: 10.881765,
              lng: 106.781719,
            },
            note: 'Khu A - Tòa A1',
            active: true,
            createdAt: '2024-01-28T12:00:00.000Z',
            updatedAt: '2024-01-28T12:00:00.000Z',
          },
          {
            id: 'A2',
            buildingCode: 'A2',
            name: 'Tòa A2',
            location: {
              lat: 10.882004,
              lng: 106.781518,
            },
            note: 'Khu A - Tòa A2',
            active: true,
            createdAt: '2024-01-28T12:00:00.000Z',
            updatedAt: '2024-01-28T12:00:00.000Z',
          },
        ],
        timestamp: '2026-01-28T12:00:00.000Z',
      },
    },
  })
  @ApiResponse({
    status: 401,
    description: 'Chưa xác thực - Thiếu hoặc token không hợp lệ',
  })
  async listActiveDeliveryPoints() {
    // Trả về RAW data - TransformInterceptor sẽ tự động wrap
    return this.deliveryPointsService.listActiveDeliveryPoints();
  }
}
