import {
  Injectable,
  Inject,
  NotFoundException,
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Logger,
} from '@nestjs/common';
import { Firestore, FieldValue } from '@google-cloud/firestore';
import { FirestoreShipperRemovalRequestsRepository } from '../repositories/firestore-shipper-removal-requests.repository';
import {
  ShipperRemovalRequestEntity,
  RemovalRequestStatus,
  RemovalRequestType,
} from '../entities/shipper-removal-request.entity';
import { CreateRemovalRequestDto, ProcessRemovalRequestDto } from '../dto';
import { UsersService } from '../../users/users.service';
import { NotificationsService } from '../../notifications/services/notifications.service';
import { NotificationType } from '../../notifications/entities/notification.entity';
import { FirebaseService } from '../../../core/firebase/firebase.service';

export const SHIPPER_REMOVAL_REQUESTS_REPOSITORY = 'SHIPPER_REMOVAL_REQUESTS_REPOSITORY';

interface ShopData {
  id: string;
  name: string;
  ownerId: string;
}

@Injectable()
export class ShipperRemovalRequestsService {
  private readonly logger = new Logger(ShipperRemovalRequestsService.name);

  constructor(
    @Inject(SHIPPER_REMOVAL_REQUESTS_REPOSITORY)
    private readonly repository: FirestoreShipperRemovalRequestsRepository,
    private readonly usersService: UsersService,
    private readonly notificationsService: NotificationsService,
    private readonly firebaseService: FirebaseService,
    @Inject('FIRESTORE')
    private readonly firestore: Firestore,
  ) {}

  /**
   * Get shop data including ownerId from Firestore
   */
  private async getShopWithOwner(shopId: string): Promise<ShopData | null> {
    const shopDoc = await this.firestore.collection('shops').doc(shopId).get();
    if (!shopDoc.exists) {
      return null;
    }
    const data = shopDoc.data()!;
    return {
      id: shopDoc.id,
      name: data.name || '',
      ownerId: data.ownerId || '',
    };
  }

  /**
   * Create a removal request (Shipper)
   * 
   * Validations:
   * - Shipper must be working for the shop
   * - Shipper must not have active orders for the shop
   * - No pending request should exist
   */
  async createRemovalRequest(
    shipperId: string,
    dto: CreateRemovalRequestDto,
  ): Promise<ShipperRemovalRequestEntity> {
    const { shopId, reason, type = RemovalRequestType.TRANSFER } = dto;

    // Get shipper profile
    const shipper = await this.usersService.getProfile(shipperId);
    if (!shipper) {
      throw new NotFoundException({
        code: 'SRR_001',
        message: 'Không tìm thấy thông tin shipper',
        statusCode: 404,
      });
    }

    // Check shipper is working for this shop
    if (shipper.shipperInfo?.shopId !== shopId) {
      throw new BadRequestException({
        code: 'SRR_002',
        message: 'Bạn không phải shipper của shop này',
        statusCode: 400,
      });
    }

    // Check for active orders
    const activeOrders = await this.checkActiveOrders(shipperId, shopId);
    if (activeOrders > 0) {
      throw new ConflictException({
        code: 'SRR_003',
        message: `Bạn đang có ${activeOrders} đơn hàng chưa hoàn thành. Vui lòng hoàn thành đơn trước khi rời khỏi shop.`,
        statusCode: 409,
      });
    }

    // Check for existing pending request
    const existingRequest = await this.repository.findPendingRequest(shipperId, shopId);
    if (existingRequest) {
      // Return existing request instead of creating new one
      return existingRequest;
    }

    // Get shop info (including ownerId)
    const shop = await this.getShopWithOwner(shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SRR_004',
        message: 'Không tìm thấy shop',
        statusCode: 404,
      });
    }

    // Create the removal request
    const request = await this.repository.create({
      shipperId,
      shipperName: shipper.displayName || 'Shipper',
      shipperPhone: shipper.phone,
      shopId,
      shopName: shop.name,
      ownerId: shop.ownerId,
      type: type as RemovalRequestType,
      reason,
      status: RemovalRequestStatus.PENDING,
    });

    // Notify shop owner
    const typeText = type === RemovalRequestType.QUIT ? 'nghỉ làm shipper' : 'chuyển sang shop khác';
    try {
      await this.notificationsService.send({
        userId: shop.ownerId,
        title: 'Yêu cầu rời shop',
        body: `${shipper.displayName || 'Shipper'} muốn ${typeText}`,
        type: NotificationType.SHIPPER_APPLIED, // Reuse existing type, or create new one
        data: {
          requestId: request.id,
          shipperId,
          shipperName: shipper.displayName,
          shopId,
          reason,
          removalType: type,
        },
        shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send removal request notification to owner:', error);
      // Non-blocking: do not throw
    }

    return request;
  }

  /**
   * List removal requests for shipper
   */
  async listMyRequests(
    shipperId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]> {
    return this.repository.findByShipperId(
      shipperId,
      status ? (status as RemovalRequestStatus) : undefined,
    );
  }

  /**
   * List removal requests for shop (Owner)
   */
  async listShopRequests(
    ownerId: string,
    shopId: string,
    status?: RemovalRequestStatus,
  ): Promise<ShipperRemovalRequestEntity[]> {
    // Verify ownership
    const shop = await this.getShopWithOwner(shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'SRR_005',
        message: 'Bạn không phải chủ shop này',
        statusCode: 403,
      });
    }

    return this.repository.findByShopId(
      shopId,
      status ? (status as RemovalRequestStatus) : undefined,
    );
  }

  /**
   * Process removal request (Owner - Approve/Reject)
   */
  async processRequest(
    ownerId: string,
    requestId: string,
    dto: ProcessRemovalRequestDto,
  ): Promise<ShipperRemovalRequestEntity> {
    const { action, rejectionReason } = dto;

    // Get request
    const request = await this.repository.findById(requestId);
    if (!request) {
      throw new NotFoundException({
        code: 'SRR_006',
        message: 'Không tìm thấy yêu cầu',
        statusCode: 404,
      });
    }

    // Verify ownership
    if (request.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'SRR_007',
        message: 'Bạn không có quyền xử lý yêu cầu này',
        statusCode: 403,
      });
    }

    // Check status is PENDING
    if (request.status !== RemovalRequestStatus.PENDING) {
      throw new ConflictException({
        code: 'SRR_008',
        message: `Yêu cầu đã được xử lý (status: ${request.status})`,
        statusCode: 409,
      });
    }

    if (action === 'APPROVE') {
      return this.approveRequest(request, ownerId);
    } else {
      return this.rejectRequest(request, ownerId, rejectionReason!);
    }
  }

  /**
   * Approve the removal request
   * - Update request status
   * - Based on type:
   *   - QUIT: Remove shipper role entirely (become CUSTOMER)
   *   - TRANSFER: Keep SHIPPER role, just clear shopId (can apply to another shop)
   * - Update Firebase custom claims
   * - Notify shipper
   */
  private async approveRequest(
    request: ShipperRemovalRequestEntity,
    ownerId: string,
  ): Promise<ShipperRemovalRequestEntity> {
    const { shipperId, shopId, shopName, type } = request;
    const isQuit = type === RemovalRequestType.QUIT;

    // Transaction: Update request + Update shipper status
    await this.firestore.runTransaction(async (transaction) => {
      const requestRef = this.firestore
        .collection('shipper_removal_requests')
        .doc(request.id);
      const userRef = this.firestore.collection('users').doc(shipperId);

      // Update request status
      transaction.update(requestRef, {
        status: RemovalRequestStatus.APPROVED,
        processedBy: ownerId,
        processedAt: FieldValue.serverTimestamp(),
      });

      if (isQuit) {
        // QUIT: Remove shipper role entirely, become CUSTOMER
        transaction.update(userRef, {
          role: 'CUSTOMER',
          shipperInfo: FieldValue.delete(),
          updatedAt: FieldValue.serverTimestamp(),
        });
      } else {
        // TRANSFER: Keep SHIPPER role, just clear shopId
        // Shipper can apply to another shop
        transaction.update(userRef, {
          'shipperInfo.shopId': FieldValue.delete(),
          'shipperInfo.shopName': FieldValue.delete(),
          updatedAt: FieldValue.serverTimestamp(),
        });
      }
    });

    // Update Firebase Custom Claims
    try {
      if (isQuit) {
        await this.firebaseService.auth.setCustomUserClaims(shipperId, {
          role: 'CUSTOMER',
        });
      }
      // For TRANSFER, keep role as SHIPPER - no need to update claims
    } catch (error) {
      this.logger.error(
        `Failed to update Firebase claims for shipper ${shipperId}:`,
        error,
      );
      // Non-blocking - user can re-login to get correct claims
    }

    // Notify shipper
    const messageBody = isQuit
      ? `Yêu cầu nghỉ việc tại ${shopName} đã được chấp nhận. Bạn giờ là CUSTOMER.`
      : `Yêu cầu rời ${shopName} đã được chấp nhận. Bạn có thể apply vào shop khác.`;

    try {
      await this.notificationsService.send({
        userId: shipperId,
        title: 'Yêu cầu được chấp nhận',
        body: messageBody,
        type: NotificationType.SHIPPER_APPLICATION_APPROVED, // Reuse
        data: {
          requestId: request.id,
          shopId,
          shopName,
          status: 'APPROVED',
          removalType: type,
        },
        shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send approval notification to shipper:', error);
    }

    // Return updated request
    return new ShipperRemovalRequestEntity({
      ...request,
      status: RemovalRequestStatus.APPROVED,
      processedBy: ownerId,
      processedAt: new Date(),
    });
  }

  /**
   * Reject the removal request
   * - Update request status with rejection reason
   * - Notify shipper
   */
  private async rejectRequest(
    request: ShipperRemovalRequestEntity,
    ownerId: string,
    rejectionReason: string,
  ): Promise<ShipperRemovalRequestEntity> {
    const { shipperId, shopId, shopName } = request;

    // Update request status
    await this.repository.updateStatus(
      request.id,
      RemovalRequestStatus.REJECTED,
      ownerId,
      rejectionReason,
    );

    // Notify shipper
    try {
      await this.notificationsService.send({
        userId: shipperId,
        title: 'Yêu cầu bị từ chối',
        body: `Yêu cầu rời khỏi ${shopName} bị từ chối: ${rejectionReason}`,
        type: NotificationType.SHIPPER_APPLICATION_REJECTED, // Reuse
        data: {
          requestId: request.id,
          shopId,
          shopName,
          status: 'REJECTED',
          rejectionReason,
        },
        shopId,
      });
    } catch (error) {
      this.logger.error('Failed to send rejection notification to shipper:', error);
    }

    // Return updated request
    return new ShipperRemovalRequestEntity({
      ...request,
      status: RemovalRequestStatus.REJECTED,
      processedBy: ownerId,
      processedAt: new Date(),
      rejectionReason,
    });
  }

  /**
   * Check if shipper has active orders for a shop
   */
  private async checkActiveOrders(
    shipperId: string,
    shopId: string,
  ): Promise<number> {
    const activeStatuses = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SHIPPING'];

    const snapshot = await this.firestore
      .collection('orders')
      .where('shipperId', '==', shipperId)
      .where('shopId', '==', shopId)
      .where('status', 'in', activeStatuses)
      .get();

    return snapshot.size;
  }
}
