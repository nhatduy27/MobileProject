import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  ForbiddenException,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../interfaces';
import { CartService } from '../../cart/services';
import { IProductsRepository } from '../../products/interfaces';
import { IShopsRepository } from '../../shops/interfaces';
import { IShippersRepository } from '../../shippers/repositories/shippers-repository.interface';
import { ShipperStatus } from '../../shippers/entities/shipper.entity';
import { IAddressesRepository, ADDRESSES_REPOSITORY } from '../../users/interfaces';
import { IUsersRepository, USERS_REPOSITORY } from '../../users/interfaces';
import { VouchersService } from '../../vouchers/vouchers.service';
import { ConfigService } from '../../../core/config/config.service';
import { FirebaseService } from '../../../core/firebase/firebase.service';
import {
  OrderEntity,
  OrderStatus,
  PaymentStatus,
  DeliveryAddress,
} from '../entities';
import {
  CreateOrderDto,
  OrderFilterDto,
  OrderListItemDto,
  PaginatedOrdersDto,
  OwnerOrderDetailDto,
} from '../dto';
import { OrderStateMachineService } from './order-state-machine.service';
import { normalizeDeliveryAddress } from '../utils/address.normalizer';
import { toIsoString } from '../utils/timestamp.serializer';


@Injectable()
export class OrdersService {
  private readonly logger = new Logger(OrdersService.name);

  constructor(
    @Inject(ORDERS_REPOSITORY)
    private readonly ordersRepo: IOrdersRepository,
    private readonly cartService: CartService,
    @Inject('PRODUCTS_REPOSITORY')
    private readonly productsRepo: IProductsRepository,
    @Inject('SHOPS_REPOSITORY')
    private readonly shopsRepo: IShopsRepository,
    @Inject('IShippersRepository')
    private readonly shippersRepo: IShippersRepository,
    @Inject(ADDRESSES_REPOSITORY)
    private readonly addressesRepo: IAddressesRepository,
    @Inject(USERS_REPOSITORY)
    private readonly usersRepo: IUsersRepository,
    private readonly vouchersService: VouchersService,
    private readonly stateMachine: OrderStateMachineService,
    private readonly configService: ConfigService,
    private readonly firebaseService: FirebaseService,
  ) {}

  /**
   * Resolve delivery address from addressId or direct snapshot
   * @param customerId - User ID
   * @param dto - Create order DTO
   * @returns Resolved delivery address snapshot
   */
  private async resolveDeliveryAddress(
    customerId: string,
    dto: CreateOrderDto,
  ): Promise<DeliveryAddress> {
    // Validation: require EITHER deliveryAddressId OR deliveryAddress
    if (!dto.deliveryAddressId && !dto.deliveryAddress?.fullAddress) {
      throw new BadRequestException({
        code: 'ORDER_INVALID_ADDRESS',
        message: 'Either deliveryAddressId or deliveryAddress.fullAddress is required',
        statusCode: 400,
      });
    }

    // Prefer deliveryAddressId if both provided
    if (dto.deliveryAddressId) {
      const savedAddress = await this.addressesRepo.findById(dto.deliveryAddressId);

      if (!savedAddress) {
        throw new NotFoundException({
          code: 'ADDRESS_NOT_FOUND',
          message: `Address ${dto.deliveryAddressId} not found`,
          statusCode: 404,
        });
      }

      // Verify ownership
      if (savedAddress.userId !== customerId) {
        throw new ForbiddenException({
          code: 'ADDRESS_ACCESS_DENIED',
          message: 'Cannot use address that belongs to another user',
          statusCode: 403,
        });
      }

      // Return snapshot (exclude isDefault, createdAt, updatedAt)
      return {
        id: savedAddress.id,
        label: savedAddress.label,
        fullAddress: savedAddress.fullAddress,
        building: savedAddress.building || undefined,
        room: savedAddress.room || undefined,
        note: dto.deliveryNote || savedAddress.note || undefined, // Allow override
      };
    }

    // Use direct snapshot from dto.deliveryAddress
    if (dto.deliveryAddress) {
      return {
        label: dto.deliveryAddress.label,
        fullAddress: dto.deliveryAddress.fullAddress!,
        building: dto.deliveryAddress.building,
        room: dto.deliveryAddress.room,
        note: dto.deliveryNote || dto.deliveryAddress.note,
        // Include legacy fields if provided (backward compatibility)
        street: dto.deliveryAddress.street,
        ward: dto.deliveryAddress.ward,
        district: dto.deliveryAddress.district,
        city: dto.deliveryAddress.city,
        coordinates: dto.deliveryAddress.coordinates,
      };
    }

    // Should never reach here due to validation above
    throw new BadRequestException({
      code: 'ORDER_INVALID_ADDRESS',
      message: 'Invalid delivery address configuration',
      statusCode: 400,
    });
  }

  /**
   * Helper: Resolve shipper's shop ID from users collection
   * This matches the data source used by owner list endpoint (GET /owner/shippers)
   * Reads from users/{uid}.shipperInfo.shopId (authoritative source)
   * 
   * SHIPPER-DATA-BUG-FIX: Validates role and provides helpful error messages
   * if shipperInfo is missing or in wrong document
   * 
   * @param shipperId - Shipper user ID
   * @returns shopId - The shop ID the shipper is assigned to
   * @throws NotFoundException - Shipper not found
   * @throws BadRequestException - Role mismatch or not assigned to shop
   */
  private async resolveShipperShopId(shipperId: string): Promise<string> {
    // Read from users collection (same as owner list endpoint)
    const shipper = await this.usersRepo.findById(shipperId);
    if (!shipper) {
      throw new NotFoundException({
        code: 'SHIPPER_NOT_FOUND',
        message: 'Shipper not found',
        statusCode: 404,
      });
    }

    // SHIPPER-DATA-BUG-FIX: Validate role is SHIPPER (if available)
    // Catches cases where shipperInfo might be in owner doc or role is wrong
    // Only validate if role field exists (some mocks/legacy data may not have it)
    if (shipper.role && shipper.role !== 'SHIPPER') {
      throw new BadRequestException({
        code: 'SHIPPER_ROLE_MISMATCH',
        message: `Account has role '${shipper.role}' but SHIPPER role is required for order endpoints`,
        statusCode: 400,
        details: {
          receivedRole: shipper.role,
          expectedRole: 'SHIPPER',
        },
      });
    }

    // Get shopId from shipperInfo (authoritative source)
    // shipperInfo MUST be in the SHIPPER user's document, not owner's document
    const shopId = shipper.shipperInfo?.shopId;
    if (!shopId) {
      throw new BadRequestException({
        code: 'SHIPPER_NOT_ASSIGNED',
        message: 'Shipper has not been assigned to a shop. Wait for owner to approve your application or check if assignment failed.',
        statusCode: 400,
        details: {
          shipperId,
          hasShipperInfo: !!shipper.shipperInfo,
          shipperInfoKeys: shipper.shipperInfo ? Object.keys(shipper.shipperInfo) : [],
        },
      });
    }

    return shopId;
  }

  /**
   * ORDER-002: Create a new order from cart
   * CRITICAL: Uses Firestore transaction to atomically create order and clear cart
   * All validation happens in service layer BEFORE transaction
   */
  async createOrder(
    customerId: string,
    dto: CreateOrderDto,
  ): Promise<OrderEntity> {
    // SERVICE LAYER VALIDATION (all pre-transaction checks)
    // This ensures cart state is confirmed before atomic transaction

    // 1. Get cart grouped by shop
    const { groups } = await this.cartService.getCartGrouped(customerId);
    if (!groups || groups.length === 0) {
      throw new NotFoundException({
        code: 'ORDER_001',
        message: 'Cart is empty',
        statusCode: 404,
      });
    }

    // 2. Get the specific shop group
    const shopGroup = groups.find((g) => g.shopId === dto.shopId);
    if (!shopGroup || shopGroup.items.length === 0) {
      throw new NotFoundException({
        code: 'ORDER_002',
        message: `No items found for shop ${dto.shopId} in cart`,
        statusCode: 404,
      });
    }

    // 3. Validate shop exists and is open
    const shop = await this.shopsRepo.findById(dto.shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'ORDER_003',
        message: 'Shop not found',
        statusCode: 404,
      });
    }
    if (!shop.isOpen || shop.status !== 'OPEN') {
      throw new ConflictException({
        code: 'ORDER_004',
        message: 'Shop is currently closed',
        statusCode: 409,
      });
    }

    // 4. Validate all products still available (for business logic only, not price)
    for (const item of shopGroup.items) {
      const product = await this.productsRepo.findById(item.productId);
      if (!product || !product.isAvailable || product.isDeleted) {
        throw new ConflictException({
          code: 'ORDER_005',
          message: `Product ${item.productName} is no longer available`,
          statusCode: 409,
        });
      }
      // NOTE: Do NOT re-fetch product.price here.
      // Prices are LOCKED from cart snapshot (price field).
    }

    // 5. Calculate totals using cart's subtotal (already calculated)
    const subtotal = shopGroup.subtotal;
    
    // FREE_SHIP MODEL: Customer pays 0, shipper gets shop fee internally
    const shipFee = 0;                           // Customer pays nothing for shipping
    const shipperPayout = shop.shipFeePerOrder || 0;  // Internal amount to pay shipper

    // 6. Validate and preview voucher (if provided)
    let discount = 0;
    let voucherId: string | undefined;
    let voucherCode: string | undefined;
    if (dto.voucherCode) {
      const validationResult = await this.vouchersService.validateVoucher(customerId, {
        code: dto.voucherCode,
        shopId: shop.id,
        subtotal,
        shipFee: shipperPayout,  // Pass internal shipFee for validation logic
      });

      if (!validationResult.valid) {
        throw new BadRequestException({
          code: validationResult.errorCode,
          message: validationResult.errorMessage,
          statusCode: 400,
        });
      }

      discount = validationResult.discountAmount;
      voucherId = validationResult.voucherId;
      voucherCode = dto.voucherCode;
    }

    // Total calculation: shipFee (0) does NOT affect total in free-ship model
    const total = subtotal - discount;

    // 6.5. Resolve delivery address (from addressId or snapshot)
    const resolvedAddress = await this.resolveDeliveryAddress(customerId, dto);

    // 6.6. Normalize delivery address (remove undefined values)
    // This ensures Firestore doesn't try to store undefined fields
    // (e.g., street/ward/district/city when using new KTX format)
    const normalizedAddress = normalizeDeliveryAddress(resolvedAddress);

    // 6.7. Fetch customer snapshot for OWNER list display (avoid N+1 queries later)
    let customerSnapshot;
    try {
      const customer = await this.usersRepo.findById(customerId);
      if (customer) {
        customerSnapshot = {
          id: customer.id,
          displayName: customer.displayName,
          phone: customer.phone,
        };
      }
    } catch (error) {
      this.logger.warn(`Failed to fetch customer snapshot for order: ${error}`);
      // Non-critical: proceed without snapshot
    }

    // 7. Create order entity
    // PRICING LOCK: All item prices are taken from cart snapshot (price field).
    // Product price changes after add-to-cart do NOT affect the order.
    const orderNumber = this.generateOrderNumber();
    const orderEntity: OrderEntity = {
      orderNumber,
      customerId,
      customerSnapshot, // Add customer snapshot for OWNER list
      shopId: dto.shopId,
      shopName: shop.name,
      items: shopGroup.items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        price: item.price, // LOCKED: Use cart snapshot price
        subtotal: item.subtotal,
      })),
      subtotal,
      shipFee,              // Customer pays 0 (free-ship model)
      shipperPayout,        // Internal: amount to pay shipper
      discount,
      voucherCode,
      voucherId,
      total,
      status: OrderStatus.PENDING,
      paymentStatus: PaymentStatus.UNPAID,
      paymentMethod: dto.paymentMethod,
      deliveryAddress: normalizedAddress, // Use normalized address (no undefined values)
      deliveryNote: dto.deliveryNote,
      shipperId: null, // FIX-001: Explicitly set to null (unassigned). Required for shipper available orders query.
    };

    // 8. CRITICAL: Use Firestore transaction
    // Apply voucher (if used) + Create order + Clear cart group atomically
    const order = await this.ordersRepo.createOrderAndClearCartGroup(
      customerId,
      dto.shopId,
      orderEntity,
      // Apply voucher atomically if voucherId present
      voucherId ? async () => {
        await this.vouchersService.applyVoucherAtomic(
          voucherId!,
          customerId,
          orderEntity.orderNumber, // Use orderNumber as orderId
          discount,
        );
      } : undefined,
    );

    return order;
  }

  /**
   * ORDER-003: Get customer's orders with cursor-based pagination
   */
  async getMyOrders(
    customerId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    const { status, page = 1, limit = 10 } = filter;

    // Validate pagination params
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 10, 50);

    // Get total count using count() - efficient Firestore operation
    const total = await this.ordersRepo.count({
      customerId,
      ...(status && { status }),
    });
    const totalPages = Math.ceil(total / validLimit);

    // Build query for data (no fetch-all, use offset properly)
    let query = this.ordersRepo
      .query()
      .where('customerId', '==', customerId)
      .orderBy('createdAt', 'desc');

    if (status) {
      query = query.where('status', '==', status);
    }

    // Use offset + limit correctly (no over-fetch, no slice)
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // Execute query
    const orders = await this.ordersRepo.findMany(query);

    // Customer endpoint doesn't need shipper resolution (customer doesn't need to see shipper details)
    // But we keep the signature consistent - pass empty map
    const emptyShipperMap = new Map();

    // Resolve customers for legacy orders missing customerSnapshot
    const customerMap = await this.resolveCustomersForOrders(orders);

    return {
      orders: orders.map(order => this.mapToListDto(order, emptyShipperMap, customerMap)),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
    };
  }

  /**
   * ORDER-004: Get order detail with ownership check
   */
  async getOrderDetail(
    customerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);

    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Security: Verify order belongs to customer
    if (order.customerId !== customerId) {
      throw new ForbiddenException({
        code: 'ORDER_007',
        message: 'You do not have permission to view this order',
        statusCode: 403,
      });
    }

    return order;
  }

  /**
   * ORDER-005: Cancel order (customer only, with state validation)
   */
  async cancelOrder(
    customerId: string,
    orderId: string,
    reason?: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Security: Verify ownership
    if (order.customerId !== customerId) {
      throw new ForbiddenException({
        code: 'ORDER_007',
        message: 'You do not have permission to cancel this order',
        statusCode: 403,
      });
    }

    // 3. Validate cancellation is allowed for current status
    if (!this.stateMachine.canCancelCustomer(order.status)) {
      throw new ConflictException({
        code: 'ORDER_012',
        message: `Cannot cancel order - order is ${order.status}`,
        statusCode: 409,
      });
    }

    // 4. Update order status
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CANCELLED,
      cancelledAt: Timestamp.now(),
      cancelReason: reason || 'Cancelled by customer',
      cancelledBy: 'CUSTOMER',
    });

    // 5. Trigger refund if already paid (stub for MVP)
    if (order.paymentStatus === PaymentStatus.PAID) {
      // TODO: Call paymentService.initiateRefund(orderId) when available
    }

    // 6. Notify shop owner (stub for MVP)
    // TODO: Call notificationService.notifyOwner(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-006: Owner confirms order
   */
  async confirmOrder(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify owner owns this shop
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'You do not have permission to confirm this order',
        statusCode: 403,
      });
    }

    // 3. Validate payment (COD can be confirmed immediately)
    if (
      order.paymentMethod !== 'COD' &&
      order.paymentStatus !== PaymentStatus.PAID
    ) {
      throw new ConflictException({
        code: 'ORDER_009',
        message: 'Cannot confirm order - payment not completed',
        statusCode: 409,
      });
    }

    // 4. Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.CONFIRMED,
    );

    // 5. Update order
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CONFIRMED,
      confirmedAt: Timestamp.now(),
    });

    // 6. Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-007: Owner marks order as preparing
   */
  async markPreparing(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'Permission denied',
        statusCode: 403,
      });
    }

    // Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.PREPARING,
    );

    // Update
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.PREPARING,
      preparingAt: Timestamp.now(),
    });

    // Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-008: Owner marks order as ready
   */
  async markReady(
    ownerId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'Permission denied',
        statusCode: 403,
      });
    }

    // Validate state transition
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.READY,
    );

    // Update
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.READY,
      readyAt: Timestamp.now(),
    });

    // Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    // Notify available shippers (broadcast) - stub for MVP
    // TODO: Call notificationService.notifyAvailableShippers(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-009: Owner cancels order
   * Can only cancel from CONFIRMED or PREPARING status
   */
  async ownerCancelOrder(
    ownerId: string,
    orderId: string,
    reason?: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify ownership
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop || shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_008',
        message: 'You do not have permission to cancel this order',
        statusCode: 403,
      });
    }

    // 3. Validate cancellation is allowed for current status
    // Owner can only cancel from CONFIRMED or PREPARING
    const cancelableStatuses = [OrderStatus.CONFIRMED, OrderStatus.PREPARING];
    if (!cancelableStatuses.includes(order.status)) {
      throw new ConflictException({
        code: 'ORDER_013',
        message: `Cannot cancel order - order is ${order.status}. Owner can only cancel CONFIRMED or PREPARING orders.`,
        statusCode: 409,
      });
    }

    // 4. Update order status
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.CANCELLED,
      cancelledAt: Timestamp.now(),
      cancelReason: reason || 'Cancelled by owner',
      cancelledBy: 'OWNER',
    });

    // 5. Trigger refund if already paid (stub for MVP)
    if (order.paymentStatus === PaymentStatus.PAID) {
      // TODO: Call paymentService.initiateRefund(orderId) when available
    }

    // 6. Notify customer (stub for MVP)
    // TODO: Call notificationService.notifyCustomer(...) when available

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-010: Get shop orders with page-based pagination
   * 
   * ⚠️ FIRESTORE INDEX REQUIREMENTS:
   * - Composite index: shopId ASC + createdAt DESC
   * - Composite index (with filter): shopId ASC + status ASC + createdAt DESC
   * 
   * DEV FALLBACK MODE:
   * Set ENABLE_FIRESTORE_PAGINATION_FALLBACK=true in .env to use in-memory pagination
   * when indexes are building. Not recommended for production.
   */
  async getShopOrders(
    ownerId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    // 1. Get shop owned by this owner
    const shop = await this.shopsRepo.findByOwnerId(ownerId);
    if (!shop) {
      throw new NotFoundException({
        code: 'ORDER_010',
        message: 'Shop not found for this owner',
        statusCode: 404,
      });
    }

    const shopId = (shop as any).id || (shop as any)._id;

    // 2. Validate pagination params
    const { status, page = 1, limit = 20 } = filter;
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 20, 50);

    try {
      // Get total count using count() - efficient Firestore operation
      const total = await this.ordersRepo.count({
        shopId,
        ...(status && { status }),
      });
      const totalPages = Math.ceil(total / validLimit);

      // 3. Build query for data (no fetch-all, use offset properly)
      let query = this.ordersRepo
        .query()
        .where('shopId', '==', shopId)
        .orderBy('createdAt', 'desc');

      if (status) {
        query = query.where('status', '==', status);
      }

      // Use offset + limit correctly (no over-fetch, no slice)
      const skipCount = (validPage - 1) * validLimit;
      query = query.offset(skipCount).limit(validLimit);

      // 4. Execute query
      const orders = await this.ordersRepo.findMany(query);

      // DEBUG: Log order structure for verification (only if DEBUG_ORDERS=true)
      if (process.env.DEBUG_ORDERS === 'true' && orders.length > 0) {
        this.logger.debug('[OWNER LIST] First order doc keys:', Object.keys(orders[0]));
        this.logger.debug('[OWNER LIST] customerSnapshot exists?', !!orders[0].customerSnapshot);
        if (orders[0].customerSnapshot) {
          this.logger.debug('[OWNER LIST] customerSnapshot:', orders[0].customerSnapshot);
        }
      }

      // 5. Resolve shippers for orders missing shipperSnapshot
      const shipperMap = await this.resolveShippersForOrders(orders);

      // 6. Resolve customers for legacy orders missing customerSnapshot
      const customerMap = await this.resolveCustomersForOrders(orders);

      return {
        orders: orders.map(order => this.mapToListDto(order, shipperMap, customerMap)),
        page: validPage,
        limit: validLimit,
        total,
        totalPages,
      };
    } catch (error: any) {
      // Check if this is a FAILED_PRECONDITION error (missing/building index)
      const isIndexError = 
        error?.code === 'FAILED_PRECONDITION' || 
        error?.code === 9 || // Firestore numeric code
        (error?.message && error.message.includes('requires an index'));

      // If fallback mode is enabled, use in-memory pagination
      if (isIndexError && this.configService.enableFirestorePaginationFallback) {
        this.logger.warn(
          `[FALLBACK MODE] Using in-memory pagination for shop ${shopId} due to missing/building Firestore index. ` +
          `This is a development workaround and NOT recommended for production.`
        );

        return this.getShopOrdersFallback(shopId, status, validPage, validLimit);
      }

      // Otherwise, let the error propagate to FirestoreErrorHandler
      throw error;
    }
  }

  /**
   * FALLBACK: Get shop orders using in-memory pagination
   * 
   * WARNING: This method is only for development when Firestore indexes are building.
   * - Fetches up to 200 orders without orderBy (avoids composite index requirement)
   * - Sorts in memory by createdAt
   * - Applies pagination in memory
   * - Does NOT scale well with large datasets
   * - Total count is approximate (limited to fetched subset)
   * 
   * DO NOT USE IN PRODUCTION
   */
  private async getShopOrdersFallback(
    shopId: string,
    status: string | undefined,
    page: number,
    limit: number,
  ): Promise<PaginatedOrdersDto> {
    // Fetch orders by shopId only (no composite index required)
    // Use a safe upper bound to avoid fetching too much data
    const FETCH_LIMIT = 200;

    let query = this.ordersRepo
      .query()
      .where('shopId', '==', shopId)
      .limit(FETCH_LIMIT);

    const allOrders = await this.ordersRepo.findMany(query);

    // Filter by status in memory if provided
    let filteredOrders = allOrders;
    if (status) {
      filteredOrders = allOrders.filter(order => order.status === status);
    }

    // Sort by createdAt DESC in memory
    // Handle Firestore Timestamp safely
    filteredOrders.sort((a, b) => {
      const timeA = this.getTimestampMillis(a.createdAt);
      const timeB = this.getTimestampMillis(b.createdAt);
      return timeB - timeA; // DESC
    });

    // Apply pagination in memory
    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + limit;
    const paginatedOrders = filteredOrders.slice(startIndex, endIndex);

    // Compute totals (approximate if we hit fetch limit)
    const total = filteredOrders.length;
    const totalPages = Math.ceil(total / limit);

    // Log warning if we might be missing data
    if (allOrders.length >= FETCH_LIMIT) {
      this.logger.warn(
        `[FALLBACK MODE] Fetched ${FETCH_LIMIT} orders. Actual total may be higher. ` +
        `Pagination totals are approximate.`
      );
    }

    // DEBUG: Log order structure for verification (only if DEBUG_ORDERS=true)
    if (process.env.DEBUG_ORDERS === 'true' && paginatedOrders.length > 0) {
      this.logger.debug('[OWNER LIST FALLBACK] First order doc keys:', Object.keys(paginatedOrders[0]));
      this.logger.debug('[OWNER LIST FALLBACK] customerSnapshot exists?', !!paginatedOrders[0].customerSnapshot);
      if (paginatedOrders[0].customerSnapshot) {
        this.logger.debug('[OWNER LIST FALLBACK] customerSnapshot:', paginatedOrders[0].customerSnapshot);
      }
    }

    // Resolve shippers for orders missing shipperSnapshot
    const shipperMap = await this.resolveShippersForOrders(paginatedOrders);

    // Resolve customers for legacy orders missing customerSnapshot
    const customerMap = await this.resolveCustomersForOrders(paginatedOrders);

    return {
      orders: paginatedOrders.map(order => this.mapToListDto(order, shipperMap, customerMap)),
      page,
      limit,
      total,
      totalPages,
    };
  }

  /**
   * Helper: Get timestamp in milliseconds for sorting
   * Handles Firestore Timestamp, Date, and fallback to 0
   */
  private getTimestampMillis(timestamp: any): number {
    if (!timestamp) return 0;
    
    // Firestore Timestamp
    if (timestamp instanceof Timestamp) {
      return timestamp.toMillis();
    }
    
    // Date object
    if (timestamp instanceof Date) {
      return timestamp.getTime();
    }
    
    // Timestamp-like object with _seconds
    if (typeof timestamp === 'object' && typeof timestamp._seconds === 'number') {
      return timestamp._seconds * 1000 + Math.floor((timestamp._nanoseconds || 0) / 1000000);
    }
    
    // String (ISO format)
    if (typeof timestamp === 'string') {
      return new Date(timestamp).getTime();
    }
    
    return 0;
  }

  /**
   * OWNER ORDER DETAIL: Get full order detail for shop owner
   * GET /api/orders/shop/:id
   * 
   * Authorization: OWNER role required
   * Validates that order belongs to owner's shop
   */
  async getShopOrderDetail(
    ownerId: string,
    orderId: string,
  ): Promise<OwnerOrderDetailDto> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_NOT_FOUND',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Get shop owned by this owner
    const shop = await this.shopsRepo.findById(order.shopId);
    if (!shop) {
      throw new NotFoundException({
        code: 'SHOP_NOT_FOUND',
        message: 'Shop not found',
        statusCode: 404,
      });
    }

    // 3. Verify ownership
    if (shop.ownerId !== ownerId) {
      throw new ForbiddenException({
        code: 'ORDER_ACCESS_DENIED',
        message: 'You do not have permission to view this order',
        statusCode: 403,
      });
    }

    // 4. Map to detail DTO for consistent response format
    return this.mapToOwnerDetailDto(order);
  }

  /**
   * ORDER-013: Accept order for delivery (shipper)
   * PHASE 2
   * 
   * OPTION-1-FIX: Accept order chỉ set shipperId, KHÔNG thay đổi status
   * Status vẫn là READY sau accept, sẽ chuyển READY → SHIPPING ở markShipping()
   * 
   * CRITICAL: Uses Firestore transaction to atomically:
   * 1. Verify order is still READY with shipperId=null (prevents race condition: two shippers accepting same order)
   * 2. Set shipperId only (do NOT change status)
   */
  async acceptOrder(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // SERVICE LAYER VALIDATION (all pre-transaction checks)
    // This ensures state is confirmed before atomic transaction

    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Validate order is READY
    if (order.status !== OrderStatus.READY) {
      throw new ConflictException({
        code: 'ORDER_014',
        message: 'Order must be READY before accepting',
        statusCode: 409,
      });
    }

    // 3. Validate no shipper assigned yet (first-to-accept wins)
    if (order.shipperId) {
      throw new ConflictException({
        code: 'ORDER_013',
        message: 'Order not available for pickup',
        statusCode: 409,
      });
    }

    // 4. Check shipper availability - must be AVAILABLE
    const shipper = await this.shippersRepo.findById(shipperId);
    if (!shipper) {
      throw new NotFoundException({
        code: 'ORDER_017',
        message: 'Shipper not found',
        statusCode: 404,
      });
    }

    // ===== CONDITION 5: Check shipper has shipperInfo (defensive check) =====
    // If shipper profile is missing, return 400 (not 500)
    if (!shipper.shipperInfo) {
      throw new BadRequestException({
        code: 'ORDER_019',
        message: `Shipper profile incomplete. Shipper account exists but missing required shipperInfo fields. Contact shop owner to ensure shipper was properly approved.`,
        statusCode: 400,
        details: {
          shipperId,
          hasShipperInfo: false,
          guidance: 'Ensure shop owner approved this shipper application before attempting to accept orders.',
        },
      });
    }

    // DEBUG: Log shipper data for troubleshooting shipper availability issues
    console.log('[AcceptOrder-DEBUG] Shipper availability check:', {
      shipperId,
      hasShipperInfo: !!shipper.shipperInfo,
      shipperStatus: shipper.shipperInfo?.status,
      expectedStatus: ShipperStatus.AVAILABLE,
      shipperInfoKeys: shipper.shipperInfo ? Object.keys(shipper.shipperInfo) : 'N/A',
      timestamp: new Date().toISOString(),
    });

    // ===== CONDITION 6: Check shipper status is AVAILABLE =====
    // Shipper must be in AVAILABLE status to accept orders
    // Status Semantics (FINAL):
    //   - ACTIVE = Employed, but offline (legacy, should transition to AVAILABLE before shift)
    //   - AVAILABLE = Online and ready to accept orders
    //   - BUSY = Currently on delivery
    //   - OFFLINE = Not working
    // For now, accept both ACTIVE and AVAILABLE to support transition period
    const validStatuses = [ShipperStatus.AVAILABLE, 'ACTIVE']; // Support both during transition
    if (!shipper.shipperInfo.status || !validStatuses.includes(shipper.shipperInfo.status)) {
      throw new ConflictException({
        code: 'ORDER_018',
        message: `Shipper is not available (status: ${shipper.shipperInfo.status}). Shipper must be in AVAILABLE or ACTIVE status to accept orders.`,
        statusCode: 409,
        details: {
          shipperId,
          currentStatus: shipper.shipperInfo.status,
          validStatuses,
          guidance: 'Ensure shipper goes online (status AVAILABLE or ACTIVE) before trying to accept orders.',
        },
      });
    }

    // 5. Do NOT validate state transition here anymore
    // OPTION-1-FIX: Status validation removed (accept doesn't change status)
    // Status check happens in markShipping() when actually shipping

    // 6. CRITICAL: Use Firestore transaction to atomically accept order
    // This prevents race condition where two shippers accept same order
    const updatedOrder = await this.ordersRepo.acceptOrderAtomically(
      orderId,
      shipperId,
    );

    console.log(`✓ Order ${order.orderNumber} accepted by shipper: ${shipperId}`);

    return updatedOrder;
  }

  /**
   * ORDER-014: Mark order as shipping (shipper picked up and started delivery)
   * PHASE 2
   * 
   * OPTION-1-FIX: Accept chỉ set shipperId (status vẫn READY)
   * markShipping sẽ thực hiện chuyển READY → SHIPPING
   */
  async markShipping(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify shipper is assigned to this order
    if (order.shipperId !== shipperId) {
      throw new ForbiddenException({
        code: 'ORDER_016',
        message: 'Shipper not assigned to this order',
        statusCode: 403,
      });
    }

    // 3. Validate state (should be READY after accept, not yet SHIPPING)
    // OPTION-1-FIX: After accept, order stays READY. Shipping will change it to SHIPPING.
    if (order.status !== OrderStatus.READY) {
      throw new ConflictException({
        code: 'ORDER_011',
        message: `Order must be READY (accepted but not yet shipped), current status: ${order.status}`,
        statusCode: 409,
      });
    }

    // 4. Validate state transition (READY -> SHIPPING)
    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.SHIPPING,
    );

    // 5. Update order: transition to SHIPPING and set shippingAt timestamp
    // OPTION-1-FIX: This is where READY → SHIPPING happens (not in accept)
    const now = Timestamp.now();
    await this.ordersRepo.update(orderId, {
      status: OrderStatus.SHIPPING,
      shippingAt: now,
      updatedAt: now,
    });

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-015: Mark order as delivered
   * PHASE 2
   */
  async markDelivered(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify shipper is assigned to this order
    if (order.shipperId !== shipperId) {
      throw new ForbiddenException({
        code: 'ORDER_016',
        message: 'Shipper not assigned to this order',
        statusCode: 403,
      });
    }

    // 3. Validate state transition (SHIPPING -> DELIVERED)
    if (order.status !== OrderStatus.SHIPPING) {
      throw new ConflictException({
        code: 'ORDER_015',
        message: 'Order must be SHIPPING before delivering',
        statusCode: 409,
      });
    }

    await this.stateMachine.validateTransition(
      order.status,
      OrderStatus.DELIVERED,
    );

    // 4. Update order status
    const updateData: any = {
      status: OrderStatus.DELIVERED,
      deliveredAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    };

    // 5. For COD orders, mark as PAID upon delivery
    if (order.paymentMethod === 'COD' && order.paymentStatus === PaymentStatus.UNPAID) {
      updateData.paymentStatus = PaymentStatus.PAID;
    }

    await this.ordersRepo.update(orderId, updateData);

    // 6. Update shipper status back to AVAILABLE
    const shipper = await this.shippersRepo.findById(shipperId);
    if (shipper?.shipperInfo) {
      await this.shippersRepo.update(shipperId, {
        shipperInfo: {
          ...shipper.shipperInfo,
          status: ShipperStatus.AVAILABLE,
        },
      });
    }

    console.log(`✓ Order ${order.orderNumber} delivered by shipper: ${shipperId}`);

    // TODO: Trigger payout to shop owner (Phase 2 enhancement)
    // await this.walletService.processOrderPayout(orderId);

    return this.ordersRepo.findById(orderId) as Promise<OrderEntity>;
  }

  /**
   * ORDER-016: Get shipper's orders with page-based pagination
   * PHASE 2
   */
  async getShipperOrders(
    shipperId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    const { status, page = 1, limit = 10 } = filter;

    // Validate shipper has a shop (authorization check)
    // Uses same data source as owner list endpoint for consistency
    await this.resolveShipperShopId(shipperId);

    // Validate pagination params
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 10, 50);

    // Get total count using count() - efficient Firestore operation
    const total = await this.ordersRepo.count({
      shipperId,
      ...(status && { status }),
    });
    const totalPages = Math.ceil(total / validLimit);

    // Build query for data (no fetch-all, use offset properly)
    let query = this.ordersRepo
      .query()
      .where('shipperId', '==', shipperId)
      .orderBy('createdAt', 'desc');

    if (status) {
      query = query.where('status', '==', status);
    }

    // Use offset + limit correctly (no over-fetch, no slice)
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // Execute query
    const orders = await this.ordersRepo.findMany(query);

    // Resolve shippers and customers for shipper list (both might need resolution for consistent DTO)
    const shipperMap = await this.resolveShippersForOrders(orders);
    const customerMap = await this.resolveCustomersForOrders(orders);

    return {
      orders: orders.map(order => this.mapToListDto(order, shipperMap, customerMap)),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
    };
  }

  /**
   * GET AVAILABLE ORDERS FOR SHIPPER
   * GET /api/orders/shipper/available
   * 
   * Returns READY orders that are unassigned (shipperId=null) and belong to the shipper's shop
   * This allows shippers to discover available orders to accept/pickup
   * 
   * Authorization: SHIPPER role required
   * Query scope: Only READY orders with shipperId=null within shipper's shop
   */
  async getShipperOrdersAvailable(
    shipperId: string,
    filter: OrderFilterDto,
  ): Promise<PaginatedOrdersDto> {
    const { page = 1, limit = 10 } = filter;

    // Resolve shopId using same data source as owner list endpoint
    const shopId = await this.resolveShipperShopId(shipperId);

    // Validate pagination params
    const validPage = Math.max(page || 1, 1);
    const validLimit = Math.min(limit || 10, 50);

    // Get total count of available orders (where shipperId === null)
    const total = await this.ordersRepo.count({
      status: OrderStatus.READY,
      shipperId: null,
      shopId,
    });
    const totalPages = Math.ceil(total / validLimit);

    // DEFENSIVE: If zero results but READY orders exist, log warning
    // This indicates some orders may lack shipperId field (pre-fix orders)
    if (total === 0) {
      try {
        const readyCount = await this.ordersRepo.count({
          status: OrderStatus.READY,
          shopId,
        });

        if (readyCount > 0) {
          console.warn(
            `[ShipperAvailable] Mismatch: shopId=${shopId} has ${readyCount} READY ` +
            `orders but 0 visible to shipper. Some orders may lack shipperId field. ` +
            `Consider running OrdersBackfillService.backfillShipperIdNull() to fix.`,
          );
        }
      } catch (err) {
        // Defensive check failed; continue without logging
        console.warn('[ShipperAvailable] Could not validate order visibility:', err);
      }
    }

    // Build query for available orders
    let query = this.ordersRepo
      .query()
      .where('status', '==', OrderStatus.READY)
      .where('shipperId', '==', null)
      .where('shopId', '==', shopId)
      .orderBy('createdAt', 'desc');

    // Use offset + limit for pagination
    const skipCount = (validPage - 1) * validLimit;
    query = query.offset(skipCount).limit(validLimit);

    // Execute query
    const orders = await this.ordersRepo.findMany(query);

    // Resolve shippers and customers for list DTO
    const shipperMap = await this.resolveShippersForOrders(orders);
    const customerMap = await this.resolveCustomersForOrders(orders);

    return {
      orders: orders.map(order => this.mapToListDto(order, shipperMap, customerMap)),
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
    };
  }

  /**
   * SHIPPER ORDER DETAIL: Get full order detail for shipper
   * GET /api/orders/shipper/:id
   * 
   * Authorization: SHIPPER role required
   * Allows access if:
   * - Order is assigned to this shipper (order.shipperId === shipperId), OR
   * - Order is READY and unassigned (shipperId is null) - allows preview before accepting
   */
  async getShipperOrderDetail(
    shipperId: string,
    orderId: string,
  ): Promise<OrderEntity> {
    // 1. Get order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'ORDER_NOT_FOUND',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Check authorization
    const isAssignedToShipper = order.shipperId === shipperId;
    const isReadyAndUnassigned = order.status === OrderStatus.READY && !order.shipperId;

    if (!isAssignedToShipper && !isReadyAndUnassigned) {
      throw new ForbiddenException({
        code: 'ORDER_ACCESS_DENIED',
        message: 'You do not have permission to view this order',
        statusCode: 403,
      });
    }

    return order;
  }

  /**
   * Batch resolve customers for legacy orders that have customerId but missing customerSnapshot
   * Prevents N+1 queries by fetching all unique customer users in one batch
   */
  private async resolveCustomersForOrders(
    orders: OrderEntity[],
  ): Promise<Map<string, { id: string; displayName?: string; phone?: string }>> {
    const customerMap = new Map<string, { id: string; displayName?: string; phone?: string }>();

    // Collect unique customer IDs that need resolution
    const customerIdsToResolve = new Set<string>();
    for (const order of orders) {
      if (order.customerId && !order.customerSnapshot) {
        customerIdsToResolve.add(order.customerId);
      }
    }

    // If no customers to resolve, return empty map
    if (customerIdsToResolve.size === 0) {
      return customerMap;
    }

    // Batch fetch customer users
    try {
      const customerIds = Array.from(customerIdsToResolve);
      const customerUsers = await Promise.all(
        customerIds.map(id => this.usersRepo.findById(id).catch(() => null))
      );

      // Build map of customer data
      customerUsers.forEach((user, index) => {
        if (user) {
          customerMap.set(customerIds[index], {
            id: user.id || customerIds[index],
            displayName: user.displayName,
            phone: user.phone,
          });
        }
      });
    } catch (error: any) {
      this.logger.warn(`Failed to resolve customers: ${error?.message || 'Unknown error'}`);
      // Return empty map on error - orders will still have customer data from snapshot if available
    }

    return customerMap;
  }

  /**
   * Batch resolve shippers for orders that have shipperId but missing shipperSnapshot
   * Prevents N+1 queries by fetching all unique shipper users in one batch
   */
  private async resolveShippersForOrders(
    orders: OrderEntity[],
  ): Promise<Map<string, { id: string; displayName?: string; phone?: string }>> {
    const shipperMap = new Map<string, { id: string; displayName?: string; phone?: string }>();

    // Collect unique shipper IDs that need resolution
    const shipperIdsToResolve = new Set<string>();
    for (const order of orders) {
      if (order.shipperId && !order.shipperSnapshot) {
        shipperIdsToResolve.add(order.shipperId);
      }
    }

    // If no shippers to resolve, return empty map
    if (shipperIdsToResolve.size === 0) {
      return shipperMap;
    }

    // Batch fetch shipper users
    try {
      const shipperIds = Array.from(shipperIdsToResolve);
      const shipperUsers = await Promise.all(
        shipperIds.map(id => this.usersRepo.findById(id).catch(() => null))
      );

      // Build map of shipper data
      shipperUsers.forEach((user, index) => {
        if (user) {
          shipperMap.set(shipperIds[index], {
            id: user.id || shipperIds[index],
            displayName: user.displayName,
            phone: user.phone,
          });
        }
      });
    } catch (error: any) {
      this.logger.warn(`Failed to resolve shippers: ${error?.message || 'Unknown error'}`);
      // Return empty map on error - orders will still have shipperId but no shipper object
    }

    return shipperMap;
  }

  /**
   * Map OrderEntity to OwnerOrderDetailDto
   * Used for OWNER detail endpoint GET /api/orders/shop/{id}
   * 
   * Ensures consistency with list response:
   * - customer field (not customerSnapshot) with phone always present
   * - shipperId field included (null or string)
   * - shipper object only when shipperId != null
   */
  private async mapToOwnerDetailDto(order: OrderEntity): Promise<OwnerOrderDetailDto> {
    // Build customer object from snapshot (always present at order creation)
    let customer: any = null;
    if (order.customerSnapshot) {
      customer = {
        id: order.customerSnapshot.id,
        displayName: order.customerSnapshot.displayName,
        phone: order.customerSnapshot.phone ?? null, // Ensure phone always present
      };
    } else {
      // Fallback: shouldn't happen as customerSnapshot is set at creation
      customer = {
        id: order.customerId,
        displayName: undefined,
        phone: null,
      };
    }

    // Build shipper object if shipper is assigned
    let shipper: any = undefined;
    if (order.shipperId) {
      if (order.shipperSnapshot) {
        shipper = {
          id: order.shipperSnapshot.id,
          displayName: order.shipperSnapshot.displayName,
          phone: order.shipperSnapshot.phone ?? null,
        };
      } else {
        // Fetch shipper if snapshot missing (single read is acceptable for detail)
        try {
          const shipperUser = await this.usersRepo.findById(order.shipperId);
          if (shipperUser) {
            shipper = {
              id: shipperUser.id || order.shipperId,
              displayName: shipperUser.displayName,
              phone: shipperUser.phone ?? null,
            };
          }
        } catch (error: any) {
          this.logger.warn(`Failed to fetch shipper ${order.shipperId}: ${error?.message}`);
          // Leave shipper undefined if fetch fails
        }
      }
    }

    return {
      id: order.id!,
      orderNumber: order.orderNumber,
      shopId: order.shopId,
      shopName: order.shopName,
      shipperId: order.shipperId ?? null,
      items: order.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        price: item.price,
        subtotal: item.subtotal,
      })),
      subtotal: order.subtotal,
      shipFee: order.shipFee,
      discount: order.discount,
      voucherCode: order.voucherCode ?? null,
      voucherId: order.voucherId ?? null,
      total: order.total,
      status: order.status,
      paymentStatus: order.paymentStatus,
      paymentMethod: order.paymentMethod,
      customer,
      shipper,
      deliveryAddress: order.deliveryAddress ? {
        label: order.deliveryAddress.label,
        fullAddress: order.deliveryAddress.fullAddress,
        building: order.deliveryAddress.building,
        room: order.deliveryAddress.room,
        note: order.deliveryAddress.note,
      } : {
        label: '',
        fullAddress: '',
      },
      deliveryNote: order.deliveryNote,
      createdAt: toIsoString(order.createdAt),
      updatedAt: toIsoString(order.updatedAt),
      confirmedAt: toIsoString(order.confirmedAt),
      preparingAt: toIsoString(order.preparingAt),
      readyAt: toIsoString(order.readyAt),
      shippingAt: toIsoString(order.shippingAt),
      deliveredAt: toIsoString(order.deliveredAt),
      cancelledAt: toIsoString(order.cancelledAt),
      cancelReason: order.cancelReason,
      cancelledBy: order.cancelledBy,
      reviewId: order.reviewId,
      reviewedAt: toIsoString(order.reviewedAt),
      paidOut: order.paidOut,
      paidOutAt: toIsoString(order.paidOutAt),
    };
  }

  private mapToListDto(
    order: OrderEntity,
    shipperMap?: Map<string, { id: string; displayName?: string; phone?: string }>,
    customerMap?: Map<string, { id: string; displayName?: string; phone?: string }>,
  ): OrderListItemDto {
    const MAX_PREVIEW_ITEMS = 3;
    
    // Create items preview (max 3 items)
    const itemsPreview = order.items.slice(0, MAX_PREVIEW_ITEMS).map(item => ({
      productId: item.productId,
      productName: item.productName,
      quantity: item.quantity,
      price: item.price,
      subtotal: item.subtotal,
    }));

    // Resolve customer: priority 1) snapshot, 2) resolved from map
    let customerData;
    if (order.customerSnapshot) {
      customerData = {
        id: order.customerSnapshot.id,
        displayName: order.customerSnapshot.displayName,
        phone: order.customerSnapshot.phone,
      };
    } else if (order.customerId && customerMap?.has(order.customerId)) {
      customerData = customerMap.get(order.customerId);
    }

    return {
      id: order.id!,
      orderNumber: order.orderNumber,
      shopId: order.shopId,
      shopName: order.shopName,
      status: order.status,
      paymentStatus: order.paymentStatus,
      total: order.total,
      discount: order.discount ?? 0,
      voucherCode: order.voucherCode ?? null,
      voucherId: order.voucherId ?? null,
      itemCount: order.items.length,
      createdAt: toIsoString(order.createdAt),
      itemsPreview,
      itemsPreviewCount: itemsPreview.length,
      customer: customerData,
      paymentMethod: order.paymentMethod,
      deliveryAddress: order.deliveryAddress ? {
        label: order.deliveryAddress.label,
        fullAddress: order.deliveryAddress.fullAddress,
        building: order.deliveryAddress.building,
        room: order.deliveryAddress.room,
      } : undefined,
      shipperId: order.shipperId ?? null,
      shipper: order.shipperSnapshot ? {
        id: order.shipperSnapshot.id,
        displayName: order.shipperSnapshot.displayName,
        phone: order.shipperSnapshot.phone,
      } : (order.shipperId && shipperMap?.has(order.shipperId) 
          ? shipperMap.get(order.shipperId)! 
          : undefined),
      updatedAt: toIsoString(order.updatedAt),
    };
  }

  private generateOrderNumber(): string {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substr(2, 6).toUpperCase();
    return `ORD-${timestamp}-${random}`;
  }

  /**
   * Backfill shipperId: null for orders missing this field
   *
   * Why needed:
   * - Orders created before this fix may lack shipperId field entirely
   * - Firestore query .where('shipperId', '==', null) doesn't match missing fields
   * - Only matches documents where field explicitly exists with value null
   *
   * Solution: Scans orders, identifies those without shipperId, updates to null
   * Safe: Batch operations, idempotent (can run multiple times)
   */
  async backfillShipperIdNull(): Promise<{
    scanned: number;
    updated: number;
    skipped: number;
    errors: Array<{ docId: string; error: string }>;
  }> {
    const db = this.firebaseService.firestore;
    const result = {
      scanned: 0,
      updated: 0,
      skipped: 0,
      errors: [] as Array<{ docId: string; error: string }>,
    };

    try {
      console.log('[BackfillShipperIdNull] Starting scan...');

      // Get all orders
      const snapshot = await db.collection('orders').get();

      console.log(`[BackfillShipperIdNull] Found ${snapshot.size} total orders`);

      const docsToUpdate: Array<{ id: string; data: any }> = [];

      // Identify orders missing shipperId field
      snapshot.forEach((doc: any) => {
        result.scanned++;
        const data = doc.data();

        // Check if order needs backfill
        // Only backfill READY, PENDING, CONFIRMED, PREPARING orders
        const statusesToBackfill = [
          OrderStatus.READY,
          OrderStatus.PENDING,
          OrderStatus.CONFIRMED,
          OrderStatus.PREPARING,
        ];

        if (
          statusesToBackfill.includes(data.status) &&
          !('shipperId' in data)
        ) {
          docsToUpdate.push({
            id: doc.id,
            data,
          });
        }
      });

      console.log(
        `[BackfillShipperIdNull] Found ${docsToUpdate.length} orders missing shipperId`,
      );

      // Batch update in groups of 100 (Firestore safe limit)
      const batchSize = 100;
      for (let i = 0; i < docsToUpdate.length; i += batchSize) {
        const batch = docsToUpdate.slice(i, i + batchSize);
        const writeBatch = db.batch();

        batch.forEach(({ id }) => {
          const docRef = db.collection('orders').doc(id);
          writeBatch.update(docRef, { shipperId: null });
          result.updated++;
        });

        try {
          await writeBatch.commit();
          console.log(
            `[BackfillShipperIdNull] Batch ${Math.floor(i / batchSize) + 1}: ` +
            `Updated ${batch.length} orders`,
          );
        } catch (err) {
          const error = err instanceof Error ? err.message : String(err);
          result.errors.push({
            docId: `batch_${Math.floor(i / batchSize) + 1}`,
            error,
          });
        }
      }

      result.skipped = result.scanned - result.updated;

      console.log(
        `[BackfillShipperIdNull] Complete! ` +
        `Scanned: ${result.scanned}, Updated: ${result.updated}, Skipped: ${result.skipped}`,
      );

      return result;
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : String(error);
      console.error('[BackfillShipperIdNull] Fatal error:', errorMsg);
      result.errors.push({
        docId: 'batch_operation',
        error: errorMsg,
      });
      throw error;
    }
  }
}
