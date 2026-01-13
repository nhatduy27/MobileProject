import { Controller, Post, Get, Delete, Body, Param, UseGuards, Req, UseInterceptors, UploadedFiles, BadRequestException } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse, ApiConsumes } from '@nestjs/swagger';
import { FileFieldsInterceptor } from '@nestjs/platform-express';
import { ShippersService } from './shippers.service';
import { ApplyShipperWithFilesDto } from './dto/apply-shipper-with-files.dto';
import { ShipperApplicationEntity } from './entities/shipper-application.entity';
import { AuthGuard } from '../../core/guards/auth.guard';

@ApiTags('Shipper Applications')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard)
@Controller('shipper-applications')
export class ShipperApplicationsController {
  constructor(private readonly shippersService: ShippersService) {}

  @Post()
  @ApiOperation({ summary: 'Apply to be Shipper' })
  @ApiResponse({
    status: 201,
    description: 'Application created successfully',
    type: ShipperApplicationEntity,
    schema: {
      example: {
        success: true,
        data: {
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
      },
    },
  })
  @ApiResponse({
    status: 409,
    description: 'Already a shipper or already applied',
    schema: {
      example: {
        success: false,
        message: 'SHIPPER_001: Bạn đã là shipper rồi',
      },
    },
  })
  @UseInterceptors(
    FileFieldsInterceptor([
      { name: 'idCardFront', maxCount: 1 },
      { name: 'idCardBack', maxCount: 1 },
      { name: 'driverLicense', maxCount: 1 },
    ]),
  )
  @ApiConsumes('multipart/form-data')
  async applyShipper(
    @Req() req: Express.Request & { user: { uid: string } },
    @Body() dto: ApplyShipperWithFilesDto,
    @UploadedFiles()
    files: {
      idCardFront?: Express.Multer.File[];
      idCardBack?: Express.Multer.File[];
      driverLicense?: Express.Multer.File[];
    },
  ): Promise<{ success: boolean; data: ShipperApplicationEntity }> {
    // Validate files
    if (!files?.idCardFront?.[0] || !files?.idCardBack?.[0] || !files?.driverLicense?.[0]) {
      throw new BadRequestException('Vui lòng upload đầy đủ 3 ảnh: CMND/CCCD mặt trước, mặt sau và bằng lái xe');
    }

    const application = await this.shippersService.applyShipperWithFiles(
      req.user.uid,
      dto,
      files.idCardFront[0],
      files.idCardBack[0],
      files.driverLicense[0],
    );
    return { success: true, data: application };
  }

  @Get('me')
  @ApiOperation({ summary: 'Get My Applications' })
  @ApiResponse({
    status: 200,
    description: 'List of my applications',
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
            status: 'PENDING',
            createdAt: '2026-01-13T10:00:00Z',
          },
        ],
      },
    },
  })
  async getMyApplications(
    @Req() req: Express.Request & { user: { uid: string } },
  ): Promise<{ success: boolean; data: ShipperApplicationEntity[] }> {
    const applications = await this.shippersService.getMyApplications(req.user.uid);
    return { success: true, data: applications };
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Cancel Application' })
  @ApiResponse({
    status: 200,
    description: 'Application cancelled successfully',
    schema: {
      example: {
        success: true,
        message: 'Đã hủy đơn xin làm shipper',
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
        message: 'Chỉ có thể hủy đơn đang chờ duyệt',
      },
    },
  })
  async cancelApplication(
    @Req() req: Express.Request & { user: { uid: string } },
    @Param('id') id: string,
  ): Promise<{ success: boolean; message: string }> {
    await this.shippersService.cancelApplication(req.user.uid, id);
    return { success: true, message: 'Đã hủy đơn xin làm shipper' };
  }
}
