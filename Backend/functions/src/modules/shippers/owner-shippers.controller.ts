import { Controller, Post, Get, Delete, Body, Param, Query, UseGuards, Req } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse, ApiQuery } from '@nestjs/swagger';
import { ShippersService } from './shippers.service';
import { RejectApplicationDto } from './dto/reject-application.dto';
import { ApplicationFilterDto } from './dto/application-filter.dto';
import { ShipperApplicationEntity, ApplicationStatus } from './entities/shipper-application.entity';
import { ShipperEntity } from './entities/shipper.entity';
import { AuthGuard } from '../../core/guards/auth.guard';
import { RolesGuard } from '../../core/guards/roles.guard';
import { Roles } from '../../core/decorators/roles.decorator';
import { UserRole } from '../users/entities/user.entity';

@ApiTags('Owner - Shippers')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/shippers')
export class OwnerShippersController {
  constructor(private readonly shippersService: ShippersService) {}

  @Get('applications')
  @ApiOperation({ summary: 'List Applications' })
  @ApiQuery({
    name: 'status',
    enum: ApplicationStatus,
    required: false,
    description: 'Filter by status',
  })
  @ApiResponse({
    status: 200,
    description: 'List of applications',
    type: [ShipperApplicationEntity],
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'app_abc123',
            userId: 'uid_123',
            userName: 'Nguyễn Văn A',
            userPhone: '0901234567',
            userAvatar: 'https://...',
            shopId: 'shop_abc',
            shopName: 'Quán A Mập',
            vehicleType: 'MOTORBIKE',
            vehicleNumber: '59X1-12345',
            idCardNumber: '079202012345',
            idCardFrontUrl: 'https://...',
            idCardBackUrl: 'https://...',
            driverLicenseUrl: 'https://...',
            message: 'Tôi muốn làm shipper...',
            status: 'PENDING',
            createdAt: '2026-01-13T10:00:00Z',
          },
        ],
      },
    },
  })
  async listApplications(
    @Req() req: Express.Request & { user: { uid: string } },
    @Query() filter: ApplicationFilterDto,
  ): Promise<ShipperApplicationEntity[]> {
    return this.shippersService.listApplications(req.user.uid, filter.status);
  }

  @Post('applications/:id/approve')
  @ApiOperation({ summary: 'Approve Application' })
  @ApiResponse({
    status: 200,
    description: 'Application approved successfully',
    schema: {
      example: {
        success: true,
        message: 'Đã duyệt đơn xin làm shipper',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Application not found',
  })
  @ApiResponse({
    status: 409,
    description: 'Application already processed',
    schema: {
      example: {
        success: false,
        message: 'Đơn đã được xử lý rồi',
      },
    },
  })
  async approveApplication(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('id') id: string,
  ): Promise<{ message: string }> {
    await this.shippersService.approveApplication(req.user.uid, id);
    return { message: 'Đã duyệt đơn xin làm shipper' };
  }

  @Post('applications/:id/reject')
  @ApiOperation({ summary: 'Reject Application' })
  @ApiResponse({
    status: 200,
    description: 'Application rejected successfully',
    schema: {
      example: {
        success: true,
        message: 'Đã từ chối đơn xin làm shipper',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Application not found',
  })
  @ApiResponse({
    status: 409,
    description: 'Application already processed',
  })
  async rejectApplication(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('id') id: string,
    @Body() dto: RejectApplicationDto,
  ): Promise<{ message: string }> {
    await this.shippersService.rejectApplication(req.user.uid, id, dto);
    return { message: 'Đã từ chối đơn xin làm shipper' };
  }

  @Get()
  @ApiOperation({ summary: 'List Shop Shippers' })
  @ApiResponse({
    status: 200,
    description: 'List of shippers',
    type: [ShipperEntity],
    schema: {
      example: {
        success: true,
        data: [
          {
            id: 'uid_123',
            name: 'Nguyễn Văn A',
            phone: '0901234567',
            avatar: 'https://...',
            shipperInfo: {
              shopId: 'shop_abc',
              shopName: 'Quán A Mập',
              vehicleType: 'MOTORBIKE',
              vehicleNumber: '59X1-12345',
              status: 'AVAILABLE',
              rating: 4.8,
              totalDeliveries: 150,
              currentOrders: ['order_1'],
              joinedAt: '2026-01-01T00:00:00Z',
            },
          },
        ],
      },
    },
  })
  async listShopShippers(
    @Req() req: Express.Request & { user: { uid: string } },
  ): Promise<ShipperEntity[]> {
    return this.shippersService.listShopShippers(req.user.uid);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Remove Shipper' })
  @ApiResponse({
    status: 200,
    description: 'Shipper removed successfully',
    schema: {
      example: {
        success: true,
        message: 'Đã xóa shipper khỏi shop',
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Shipper not found',
  })
  @ApiResponse({
    status: 409,
    description: 'Shipper has active orders',
    schema: {
      example: {
        success: false,
        message: 'SHIPPER_003: Shipper đang có đơn chưa hoàn thành',
      },
    },
  })
  async removeShipper(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('id') id: string,
  ): Promise<{ message: string }> {
    await this.shippersService.removeShipper(req.user.uid, id);
    return { message: 'Đã xóa shipper khỏi shop' };
  }
}
