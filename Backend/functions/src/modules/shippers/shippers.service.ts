import {
  Injectable,
  ConflictException,
  NotFoundException,
  ForbiddenException,
  Inject,
  BadRequestException,
} from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { IShippersRepository } from './repositories/shippers-repository.interface';
import { UsersService } from '../users/users.service';
import { ShopsService } from '../shops/services/shops.service';
import { StorageService } from '../../shared/services/storage.service';
import { ApplyShipperDto } from './dto/apply-shipper.dto';
import { RejectApplicationDto } from './dto/reject-application.dto';
import { ShipperApplicationEntity, ApplicationStatus } from './entities/shipper-application.entity';
import { ShipperEntity } from './entities/shipper.entity';

@Injectable()
export class ShippersService {
  constructor(
    @Inject('IShippersRepository')
    private readonly shippersRepository: IShippersRepository,
    private readonly usersService: UsersService,
    private readonly shopsService: ShopsService,
    private readonly storageService: StorageService,
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  // SHIP-002: Apply to be Shipper with Files
  async applyShipper(
    userId: string,
    dto: ApplyShipperDto,
    idCardFrontFile: Express.Multer.File,
    idCardBackFile: Express.Multer.File,
    driverLicenseFile: Express.Multer.File,
  ): Promise<ShipperApplicationEntity> {
    const user = await this.usersService.getProfile(userId);

    // Check if already shipper
    if (user.role === 'SHIPPER') {
      throw new ConflictException('SHIPPER_001: Bạn đã là shipper rồi');
    }

    // Validate shop exists first
    const shop = await this.shopsService.getShopById(dto.shopId);

    // Check if already applied (PENDING)
    const existingApp = await this.shippersRepository.findPendingApplication(userId, dto.shopId);
    if (existingApp) {
      throw new ConflictException('SHIPPER_005: Bạn đã nộp đơn cho shop này rồi');
    }

    // Validate image types
    const validMimeTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (
      !validMimeTypes.includes(idCardFrontFile.mimetype) ||
      !validMimeTypes.includes(idCardBackFile.mimetype) ||
      !validMimeTypes.includes(driverLicenseFile.mimetype)
    ) {
      throw new BadRequestException('Chỉ chấp nhận file ảnh định dạng JPG, JPEG, PNG');
    }

    // Validate image sizes (max 5MB each)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (
      idCardFrontFile.size > maxSize ||
      idCardBackFile.size > maxSize ||
      driverLicenseFile.size > maxSize
    ) {
      throw new BadRequestException('Kích thước mỗi ảnh không được vượt quá 5MB');
    }

    // Upload images to Firebase Storage
    let idCardFrontUrl: string;
    let idCardBackUrl: string;
    let driverLicenseUrl: string;

    try {
      // Upload all 3 documents in parallel
      [idCardFrontUrl, idCardBackUrl, driverLicenseUrl] = await Promise.all([
        this.storageService.uploadShipperDocument(
          userId,
          'idCardFront',
          idCardFrontFile.buffer,
          idCardFrontFile.mimetype,
        ),
        this.storageService.uploadShipperDocument(
          userId,
          'idCardBack',
          idCardBackFile.buffer,
          idCardBackFile.mimetype,
        ),
        this.storageService.uploadShipperDocument(
          userId,
          'driverLicense',
          driverLicenseFile.buffer,
          driverLicenseFile.mimetype,
        ),
      ]);
    } catch (error) {
      throw new BadRequestException('Upload ảnh thất bại. Vui lòng thử lại');
    }

    // Create application
    const application = await this.shippersRepository.createApplication({
      userId,
      userName: user.displayName,
      userPhone: user.phone || '',
      userAvatar: user.avatarUrl || '',
      shopId: dto.shopId,
      shopName: shop.name,
      vehicleType: dto.vehicleType,
      vehicleNumber: dto.vehicleNumber,
      idCardNumber: dto.idCardNumber,
      idCardFrontUrl,
      idCardBackUrl,
      driverLicenseUrl,
      message: dto.message,
      status: ApplicationStatus.PENDING,
    });

    // TODO: Notify owner
    // await this.notificationService.sendToOwner(dto.shopId, { ... });

    return application;
  }

  // SHIP-003: Get My Applications
  async getMyApplications(userId: string): Promise<ShipperApplicationEntity[]> {
    return this.shippersRepository.findUserApplications(userId);
  }

  // SHIP-004: Cancel Application
  async cancelApplication(userId: string, applicationId: string): Promise<void> {
    const application = await this.shippersRepository.findApplicationById(applicationId);

    if (!application) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Check ownership
    if (application.userId !== userId) {
      throw new ForbiddenException('Bạn không có quyền hủy đơn này');
    }

    // Only PENDING can be cancelled
    if (application.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Chỉ có thể hủy đơn đang chờ duyệt');
    }

    await this.shippersRepository.deleteApplication(applicationId);
  }

  // SHIP-005: List Applications (Owner)
  async listApplications(
    ownerId: string,
    status?: ApplicationStatus,
  ): Promise<ShipperApplicationEntity[]> {
    const shop = await this.shopsService.getMyShop(ownerId);
    return this.shippersRepository.findShopApplications(shop.id, status);
  }

  // SHIP-006: Approve Application ⭐
  async approveApplication(ownerId: string, applicationId: string): Promise<void> {
    const shop = await this.shopsService.getMyShop(ownerId);

    const app = await this.shippersRepository.findApplicationById(applicationId);

    if (!app) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Validate ownership
    if (app.shopId !== shop.id) {
      throw new ForbiddenException('Bạn không có quyền duyệt đơn này');
    }

    // Check status
    if (app.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Đơn đã được xử lý rồi');
    }

    // Transaction: Update application + Update user
    await this.firestore.runTransaction(async (transaction) => {
      const appRef = this.firestore.collection('shipperApplications').doc(applicationId);
      const userRef = this.firestore.collection('users').doc(app.userId);

      // Update application
      transaction.update(appRef, {
        status: ApplicationStatus.APPROVED,
        reviewedBy: ownerId,
        reviewedAt: FieldValue.serverTimestamp(),
      });

      // Update user: change role to SHIPPER + shipperInfo
      transaction.update(userRef, {
        role: 'SHIPPER',
        shipperInfo: {
          shopId: app.shopId,
          shopName: app.shopName,
          vehicleType: app.vehicleType,
          vehicleNumber: app.vehicleNumber,
          status: 'ACTIVE',
          isOnline: false,
          rating: 5.0,
          totalDeliveries: 0,
          currentOrders: [],
          joinedAt: FieldValue.serverTimestamp(),
        },
        updatedAt: FieldValue.serverTimestamp(),
      });
    });

    // TODO: Notify user
    // await this.notificationService.sendToUser(app.userId, { ... });
  }

  // SHIP-007: Reject Application
  async rejectApplication(
    ownerId: string,
    applicationId: string,
    dto: RejectApplicationDto,
  ): Promise<void> {
    const shop = await this.shopsService.getMyShop(ownerId);

    const app = await this.shippersRepository.findApplicationById(applicationId);

    if (!app) {
      throw new NotFoundException('Không tìm thấy đơn xin làm shipper');
    }

    // Validate ownership
    if (app.shopId !== shop.id) {
      throw new ForbiddenException('Bạn không có quyền từ chối đơn này');
    }

    // Check status
    if (app.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Đơn đã được xử lý rồi');
    }

    await this.shippersRepository.updateApplicationStatus(
      applicationId,
      ApplicationStatus.REJECTED,
      ownerId,
      dto.reason,
    );

    // TODO: Notify user
    // await this.notificationService.sendToUser(app.userId, { ... });
  }

  // SHIP-008: List Shop Shippers
  async listShopShippers(ownerId: string): Promise<ShipperEntity[]> {
    const shop = await this.shopsService.getMyShop(ownerId);

    const shippers = await this.shippersRepository.findShippersByShop(shop.id);

    return shippers.map(
      (shipper) =>
        new ShipperEntity({
          id: shipper.id as string,
          name: (shipper.displayName || shipper.name) as string,
          phone: shipper.phone as string,
          avatar: (shipper.avatarUrl || shipper.avatar) as string,
          shipperInfo: shipper.shipperInfo
            ? ({
                ...shipper.shipperInfo,
                joinedAt:
                  (shipper.shipperInfo as any).joinedAt?.toDate?.() ||
                  (shipper.shipperInfo as any).joinedAt,
              } as any)
            : undefined,
        }),
    );
  }

  // SHIP-009: Remove Shipper
  async removeShipper(ownerId: string, shipperId: string): Promise<void> {
    const shop = await this.shopsService.getMyShop(ownerId);

    const shipper = await this.usersService.getProfile(shipperId);

    // Validate ownership
    if (shipper.shipperInfo?.shopId !== shop.id) {
      throw new ForbiddenException('Shipper này không thuộc shop của bạn');
    }

    // Check if shipper has active orders
    // Note: currentOrders will be populated by Orders module
    // const shipperInfo = shipper.shipperInfo as any;
    // if (shipperInfo?.currentOrders && Array.isArray(shipperInfo.currentOrders) && shipperInfo.currentOrders.length > 0) {
    //   throw new ConflictException('SHIPPER_003: Shipper đang có đơn chưa hoàn thành');
    // }

    // Remove SHIPPER role and clear shipperInfo
    const userRef = this.firestore.collection('users').doc(shipperId);
    await userRef.update({
      role: 'CUSTOMER', // Change back to CUSTOMER
      shipperInfo: FieldValue.delete(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
}
