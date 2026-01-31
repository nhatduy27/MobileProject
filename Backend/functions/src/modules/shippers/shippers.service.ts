import {
  Injectable,
  ConflictException,
  NotFoundException,
  ForbiddenException,
  Inject,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { IShippersRepository } from './repositories/shippers-repository.interface';
import { UsersService } from '../users/users.service';
import { ShopsService } from '../shops/services/shops.service';
import { StorageService } from '../../shared/services/storage.service';
import { FirebaseService } from '../../core/firebase/firebase.service';
import { WalletsService } from '../wallets/wallets.service';
import { WalletType } from '../wallets/entities';
import { ApplyShipperDto } from './dto/apply-shipper.dto';
import { RejectApplicationDto } from './dto/reject-application.dto';
import { ShipperApplicationEntity, ApplicationStatus } from './entities/shipper-application.entity';
import { ShipperEntity } from './entities/shipper.entity';
import { NotificationsService } from '../notifications/services/notifications.service';
import { NotificationType } from '../notifications/entities/notification.entity';

@Injectable()
export class ShippersService {
  private readonly logger = new Logger(ShippersService.name);

  constructor(
    @Inject('IShippersRepository')
    private readonly shippersRepository: IShippersRepository,
    private readonly usersService: UsersService,
    private readonly shopsService: ShopsService,
    private readonly storageService: StorageService,
    private readonly firebaseService: FirebaseService,
    private readonly notificationsService: NotificationsService,
    private readonly walletsService: WalletsService,
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

    // Check if already assigned to a shop (has shipperInfo with shopId)
    // Note: User can have role SHIPPER but not yet assigned to any shop
    // They need to apply to a shop first, then get approved
    if (user.shipperInfo?.shopId) {
      throw new ConflictException('SHIPPER_001: Bạn đã là shipper của một shop rồi');
    }

    // Validate shop exists first - get full shop entity to get ownerId
    const shopRef = this.firestore.collection('shops').doc(dto.shopId);
    const shopDoc = await shopRef.get();
    if (!shopDoc.exists) {
      throw new NotFoundException('Không tìm thấy shop này');
    }
    const shop = shopDoc.data() as any;
    const shopName = shop.name || '';

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
      shopName,
      vehicleType: dto.vehicleType,
      vehicleNumber: dto.vehicleNumber,
      idCardNumber: dto.idCardNumber,
      idCardFrontUrl,
      idCardBackUrl,
      driverLicenseUrl,
      message: dto.message,
      status: ApplicationStatus.PENDING,
    });

    // Notify shop owner about shipper application
    try {
      await this.notificationsService.send({
        userId: shop.ownerId,
        title: `Đơn đăng ký shipper mới`,
        body: `${user.displayName} muốn làm shipper cho ${shop.name}`,
        type: NotificationType.SHIPPER_APPLIED,
        data: {
          applicationId: application.id,
          shipperId: userId,
          shopId: dto.shopId,
          vehicleType: dto.vehicleType,
        },
        shopId: dto.shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send SHIPPER_APPLIED notification:', error);
      // Non-blocking: do not throw
    }

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

    // SHIPPER-DATA-BUG-FIX: Validate app.userId !== ownerId
    // Sanity check to prevent shipperInfo being written to owner's document
    if (app.userId === ownerId) {
      throw new BadRequestException(
        'SHIPPER_BUG: Chủ shop không thể là shipper cho chính shop của mình. Vui lòng dùng tài khoản shipper khác.',
      );
    }

    // Validate ownership
    if (app.shopId !== shop.id) {
      throw new ForbiddenException('Bạn không có quyền duyệt đơn này');
    }

    // Check status
    if (app.status !== ApplicationStatus.PENDING) {
      throw new ConflictException('Đơn đã được xử lý rồi');
    }

    // Transaction: Update application + Update user (Firestore only)
    // SHIPPER-DATA-BUG-FIX: Verify we're writing to shipper's document (app.userId), not owner's
    await this.firestore.runTransaction(async (transaction) => {
      const appRef = this.firestore.collection('shipperApplications').doc(applicationId);
      const userRef = this.firestore.collection('users').doc(app.userId); // ✓ Correct: shipper's document

      // Check if already approved (idempotent)
      const appDoc = await transaction.get(appRef);
      const currentApp = appDoc.data() as any;

      if (currentApp.status === ApplicationStatus.APPROVED) {
        // Already approved, skip Firestore update (will still sync claims below)
        this.logger.log(`Application ${applicationId} already approved (idempotent)`);
        return;
      }

      // Update application
      transaction.update(appRef, {
        status: ApplicationStatus.APPROVED,
        reviewedBy: ownerId,
        reviewedAt: FieldValue.serverTimestamp(),
      });

      // Update user: change role to SHIPPER + shipperInfo
      // SHIPPER-DATA-BUG-FIX: Set status to AVAILABLE (not ACTIVE) to match accept-order expectations
      // Status Semantics (FINAL):
      //   - AVAILABLE = Online and ready to accept orders (default after approval)
      //   - BUSY = Currently on delivery
      //   - OFFLINE = Not working / offline
      transaction.update(userRef, {
        role: 'SHIPPER',
        shipperInfo: {
          shopId: app.shopId,
          shopName: app.shopName,
          vehicleType: app.vehicleType,
          vehicleNumber: app.vehicleNumber,
          status: 'AVAILABLE', // FIX: Changed from ACTIVE to AVAILABLE
          isOnline: false, // Default: offline until shipper goes online
          rating: 5.0,
          totalDeliveries: 0,
          currentOrders: [],
          joinedAt: FieldValue.serverTimestamp(),
        },
        claimsSyncStatus: 'PENDING', // Track claims sync state
        updatedAt: FieldValue.serverTimestamp(),
      });
    });

    // CRITICAL: Update Firebase Custom Claims to keep authorization in sync
    // P0-FIX: DO NOT THROW after transaction commit - log and mark status instead
    // AuthGuard and RolesGuard check custom claims, so claims must be updated
    // after Firestore transaction succeeds
    try {
      await this.firebaseService.auth.setCustomUserClaims(app.userId, {
        role: 'SHIPPER',
      });

      // Mark claims sync as successful
      await this.firestore.collection('users').doc(app.userId).update({
        claimsSyncStatus: 'OK',
        claimsSyncedAt: FieldValue.serverTimestamp(),
      });

      this.logger.log(`Claims synced successfully for shipper ${app.userId}`);
    } catch (error) {
      // P0-FIX: DO NOT THROW - this would fail the API even though data is committed
      // Instead: log error and mark status for manual/automatic retry
      const errorMsg =
        `Failed to sync Firebase custom claims for shipper ${app.userId} after approval. ` +
        `User has role SHIPPER in Firestore but claims not updated. ` +
        `User should re-login or admin should retry sync.`;

      this.logger.error(errorMsg, error);

      // Mark status as FAILED for backfill/retry
      await this.firestore
        .collection('users')
        .doc(app.userId)
        .update({
          claimsSyncStatus: 'FAILED',
          claimsSyncError: error instanceof Error ? error.message : String(error),
        })
        .catch((err) => {
          this.logger.error(`Failed to update claimsSyncStatus: ${err}`);
        });

      // DO NOT THROW - approval succeeded, just claims sync failed
      // API returns success, shipper can re-login to get fresh claims
    }

    // Initialize shipper wallet (non-blocking, best-effort)
    this.walletsService.initializeWallet(app.userId, WalletType.SHIPPER).catch((err) => {
      console.error(`Failed to initialize shipper wallet for ${app.userId}:`, err);
    });

    // Notify shipper about approval
    try {
      await this.notificationsService.send({
        userId: app.userId,
        title: `Đơn đăng ký được duyệt`,
        body: `Đơn đăng ký làm shipper cho ${app.shopName} đã được chấp nhận!`,
        type: NotificationType.SHIPPER_APPLICATION_APPROVED,
        data: {
          applicationId,
          shopId: app.shopId,
          shopName: app.shopName,
        },
        shopId: app.shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send SHIPPER_APPLICATION_APPROVED notification:', error);
      // Non-blocking: do not throw
    }
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

    // Notify shipper about rejection
    try {
      await this.notificationsService.send({
        userId: app.userId,
        title: `Đơn đăng ký bị từ chối`,
        body: `Đơn đăng ký làm shipper cho ${app.shopName} bị từ chối. Lý do: ${dto.reason || 'Không có lý do'}`,
        type: NotificationType.SHIPPER_APPLICATION_REJECTED,
        data: {
          applicationId,
          shopId: app.shopId,
          shopName: app.shopName,
          reason: dto.reason,
        },
        shopId: app.shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send SHIPPER_APPLICATION_REJECTED notification:', error);
      // Non-blocking: do not throw
    }
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

    // Remove SHIPPER role and clear shipperInfo (update both sources)
    const userRef = this.firestore.collection('users').doc(shipperId);
    await userRef.update({
      role: 'CUSTOMER', // Change back to CUSTOMER
      shipperInfo: FieldValue.delete(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    // CRITICAL: Update Firebase Custom Claims to keep authorization in sync
    // Must update claims when role changes, even when removing shipper role
    try {
      await this.firebaseService.auth.setCustomUserClaims(shipperId, {
        role: 'CUSTOMER',
      });
    } catch (error) {
      // If claims update fails, log error and throw
      // Firestore updated but claims are out of sync
      const errorMsg = `Failed to sync Firebase custom claims for shipper ${shipperId} removal. Claims will be inconsistent until user re-login.`;
      console.error(errorMsg, error);
      throw new Error(errorMsg);
    }
  }

  /**
   * Get shipper profile by ID
   * Returns shipper entity with shipperInfo including shopId
   */
  async findById(shipperId: string): Promise<ShipperEntity | null> {
    const shipper = await this.shippersRepository.findById(shipperId);
    if (!shipper) {
      return null;
    }

    return new ShipperEntity({
      id: shipper.id,
      name: shipper.name,
      phone: shipper.phone,
      avatar: shipper.avatar,
      shipperInfo: shipper.shipperInfo,
    });
  }

  /**
   * Update shipper online status
   * Called when shipper goes online/offline via topic subscription
   */
  async updateOnlineStatus(shipperId: string, isOnline: boolean): Promise<void> {
    const userRef = this.firestore.collection('users').doc(shipperId);
    await userRef.update({
      'shipperInfo.isOnline': isOnline,
      'shipperInfo.lastOnlineAt': isOnline ? FieldValue.serverTimestamp() : null,
      updatedAt: FieldValue.serverTimestamp(),
    });
    this.logger.log(`Shipper ${shipperId} isOnline updated to: ${isOnline}`);
  }
}
