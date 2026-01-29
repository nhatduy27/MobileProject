import {
  Injectable,
  Logger,
  BadRequestException,
  NotFoundException,
  ForbiddenException,
  ConflictException,
  Inject,
} from '@nestjs/common';
import { Firestore, Timestamp } from 'firebase-admin/firestore';
import { GoogleRoutesService } from './google-routes.service';
import { DeliveryPointsRepository } from '../repositories/delivery-points.repository';
import { ShipperTripsRepository } from '../repositories/shipper-trips.repository';
import { OrdersService } from '../../orders/services/orders.service';
import { DeliveryPointsService } from '../../delivery-points/services/delivery-points.service';
import {
  ShipperTrip,
  TripStatus,
  TripDeliveryStatus,
  TripWaypoint,
  TripOrder,
} from '../entities/shipper-trip.entity';
import { CreateOptimizedTripDto } from '../dto/create-optimized-trip.dto';
import { PaginatedTripsDto } from '../dto/paginated-trips.dto';
import { OrderStatus } from '../../orders/entities/order.entity';

/**
 * GPS Service
 *
 * Core business logic for route optimization and trip management.
 */
@Injectable()
export class GpsService {
  private readonly logger = new Logger(GpsService.name);

  constructor(
    private readonly googleRoutesService: GoogleRoutesService,
    private readonly deliveryPointsRepository: DeliveryPointsRepository,
    private readonly shipperTripsRepository: ShipperTripsRepository,
    private readonly ordersService: OrdersService,
    private readonly deliveryPointsService: DeliveryPointsService,
    @Inject('FIRESTORE') private readonly firestore: Firestore,
  ) {}

  /**
   * Serialize Firestore Timestamp to ISO string
   * Handles both Firestore Timestamp objects and Date objects
   */
  private serializeTimestamp(
    timestamp: FirebaseFirestore.Timestamp | Date | undefined,
  ): string | undefined {
    if (!timestamp) return undefined;
    if (timestamp instanceof Date) return timestamp.toISOString();
    if (typeof (timestamp as any).toDate === 'function') {
      return (timestamp as FirebaseFirestore.Timestamp).toDate().toISOString();
    }
    return undefined;
  }

  /**
   * Serialize trip timestamps to ISO strings for API response consistency
   */
  private serializeTripTimestamps(trip: ShipperTrip): ShipperTrip {
    return {
      ...trip,
      createdAt: this.serializeTimestamp(trip.createdAt) as any,
      updatedAt: this.serializeTimestamp(trip.updatedAt) as any,
      startedAt: this.serializeTimestamp(trip.startedAt) as any,
      finishedAt: this.serializeTimestamp(trip.finishedAt) as any,
      cancelledAt: this.serializeTimestamp(trip.cancelledAt) as any,
    };
  }

  /**
   * Create optimized trip for shipper
   *
   * @param shipperId Shipper user ID
   * @param dto Trip creation data
   * @returns Created trip with optimized route
   */
  async createOptimizedTrip(shipperId: string, dto: CreateOptimizedTripDto): Promise<ShipperTrip> {
    this.logger.log(
      `Creating optimized trip for shipper ${shipperId} with ${dto.orderIds.length} orders`,
    );

    // 1. Validate and fetch orders
    const orders = await this.validateOrders(dto.orderIds, shipperId);
    this.logger.log(`Validated ${orders.length} orders`);

    // 2. Extract building codes from orders
    const buildingCodes = this.extractBuildingCodes(orders);

    if (buildingCodes.length === 0) {
      throw new BadRequestException(
        'No valid buildings found in orders. Ensure orders have building field.',
      );
    }

    this.logger.log(
      `Extracted ${buildingCodes.length} unique buildings: ${buildingCodes.join(', ')}`,
    );

    // 3. Fetch delivery points for buildings
    const deliveryPointsMap = await this.deliveryPointsRepository.getByBuildingCodes(buildingCodes);
    this.logger.log(`Fetched ${deliveryPointsMap.size} delivery points from repository`);

    // Validate all buildings have delivery points
    const missingBuildings = buildingCodes.filter((code) => !deliveryPointsMap.has(code));
    if (missingBuildings.length > 0) {
      throw new BadRequestException(
        `Delivery points not found for buildings: ${missingBuildings.join(', ')}. ` +
          'Please ensure deliveryPoints collection is seeded.',
      );
    }

    // 4. Prepare waypoints for route optimization
    const waypoints = buildingCodes.map((code) => {
      const point = deliveryPointsMap.get(code);
      if (!point) {
        this.logger.error(`Delivery point not found for building code: ${code}`);
        throw new BadRequestException(`Delivery point not found for building: ${code}`);
      }
      if (!point.location) {
        this.logger.error(
          `Delivery point ${code} missing location field: ${JSON.stringify(point)}`,
        );
        throw new BadRequestException(`Delivery point ${code} missing location coordinates`);
      }
      return {
        lat: point.location.lat,
        lng: point.location.lng,
      };
    });

    // 5. Determine return destination (use origin if not specified)
    const returnTo = dto.returnTo || dto.origin;

    // 6. Call Google Routes API for route optimization
    this.logger.log('Calling Google Routes API for optimization...');
    const routeResult = await this.googleRoutesService.computeOptimizedRoute(
      dto.origin,
      waypoints,
      returnTo,
    );

    // 7. Build optimized waypoints with correct order
    // Note: Google Routes API may return [-1] for single waypoint (no optimization needed)
    // Filter out invalid indices and handle edge cases
    let validWaypointOrder = routeResult.waypointOrder.filter(
      (idx) => idx >= 0 && idx < buildingCodes.length,
    );

    // If no valid indices (e.g., all -1), use original order
    // Note: Google Routes API returns [-1] for single waypoint (no optimization needed) - this is expected behavior
    if (validWaypointOrder.length === 0) {
      this.logger.log(
        `Waypoint order from Google Routes API: [${routeResult.waypointOrder.join(', ')}]. Using original order (normal for single waypoint).`,
      );
      validWaypointOrder = buildingCodes.map((_, idx) => idx);
    }

    // IMPORTANT: Reorder waypoints array to match optimized visiting order
    // This ensures waypoints[i].order matches the actual visiting sequence
    // After this transformation:
    // - waypoints[0] = first stop (order: 1)
    // - waypoints[1] = second stop (order: 2)
    // - etc.
    const optimizedWaypoints: TripWaypoint[] = validWaypointOrder.map(
      (originalIndex, visitingIndex) => {
        const buildingCode = buildingCodes[originalIndex];
        const point = deliveryPointsMap.get(buildingCode);

        if (!point) {
          this.logger.error(
            `Delivery point not found for building code: ${buildingCode} at index ${originalIndex}`,
          );
          throw new BadRequestException(`Delivery point not found for building: ${buildingCode}`);
        }

        return {
          buildingCode,
          location: {
            lat: point.location.lat,
            lng: point.location.lng,
            name: point.name,
          },
          order: visitingIndex + 1, // 1-based visiting order (1st, 2nd, 3rd...)
        };
      },
    );

    // 8. Map orders to trip orders with delivery status and stop index
    // stopIndex maps each order to its corresponding waypoint (1-based)
    // Multiple orders can share the same stopIndex if they go to the same building
    const buildingToStopIndex = new Map<string, number>();
    optimizedWaypoints.forEach((waypoint) => {
      buildingToStopIndex.set(waypoint.buildingCode, waypoint.order);
    });

    const tripOrders: TripOrder[] = orders.map((order) => {
      const buildingCode = order.deliveryAddress?.building || '';
      const stopIndex = buildingToStopIndex.get(buildingCode) || 0;

      return {
        orderId: order.id,
        buildingCode,
        tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
        stopIndex, // Maps order to waypoint (1st stop, 2nd stop, etc.)
      };
    });

    // Sort orders by stopIndex (ascending), then by orderId for stability
    tripOrders.sort((a, b) => {
      if (a.stopIndex !== b.stopIndex) {
        return a.stopIndex - b.stopIndex;
      }
      return a.orderId.localeCompare(b.orderId);
    });

    // 9. Create trip document
    // Convert DTO objects to plain objects for Firestore compatibility
    // CRITICAL: Must spread all properties explicitly to avoid custom prototypes
    const tripData: Omit<ShipperTrip, 'id' | 'createdAt' | 'updatedAt'> = {
      shipperId,
      status: TripStatus.PENDING,
      origin: {
        lat: Number(dto.origin.lat),
        lng: Number(dto.origin.lng),
        ...(dto.origin.name && { name: String(dto.origin.name) }),
      },
      returnTo: {
        lat: Number(returnTo.lat),
        lng: Number(returnTo.lng),
        ...(returnTo.name && { name: String(returnTo.name) }),
      },
      waypoints: optimizedWaypoints,
      orders: tripOrders,
      route: {
        distance: routeResult.distance,
        duration: routeResult.duration,
        polyline: routeResult.polyline,
        // waypointOrder is now sequential [0,1,2,...] since waypoints array is already in optimized order
        // This eliminates confusion: waypoints[i] corresponds to visiting order i+1
        waypointOrder: optimizedWaypoints.map((_, idx) => idx),
      },
      totalDistance: routeResult.distance,
      totalDuration: routeResult.duration,
      totalOrders: orders.length,
      totalBuildings: buildingCodes.length,
    };

    // 10. Save to Firestore
    const trip = await this.shipperTripsRepository.create(tripData);

    this.logger.log(
      `Trip created: ${trip.id}, distance: ${trip.totalDistance}m, ` +
        `duration: ${trip.totalDuration}s, buildings: ${trip.totalBuildings}`,
    );

    return trip;
  }

  /**
   * Validate orders exist and are eligible for trip
   *
   * Orders must:
   * - Exist
   * - Not be cancelled or already delivered
   * - Have delivery address with building field
   *
   * Note: Using getShipperOrderDetail for validation
   */
  private async validateOrders(orderIds: string[], shipperId: string): Promise<any[]> {
    const orders = [];

    for (const orderId of orderIds) {
      try {
        // Fetch order detail (shipper view - checks if order is assigned to shipper)
        const order = await this.ordersService.getShipperOrderDetail(shipperId, orderId);

        // Validate order status
        if (order.status === 'CANCELLED') {
          throw new BadRequestException(
            `Order ${orderId} is cancelled and cannot be included in trip`,
          );
        }

        if (order.status === 'DELIVERED') {
          throw new BadRequestException(`Order ${orderId} is already delivered`);
        }

        // Validate has building field
        if (!order.deliveryAddress?.building) {
          throw new BadRequestException(
            `Order ${orderId} does not have building field in delivery address. ` +
              'Building is required for route optimization.',
          );
        }

        orders.push(order);
      } catch (error) {
        // If order not found or other error, re-throw with context
        if (error instanceof NotFoundException) {
          throw new NotFoundException(`Order ${orderId} not found or not assigned to you`);
        }
        throw error;
      }
    }

    return orders;
  }

  /**
   * Extract unique building codes from orders
   */
  private extractBuildingCodes(orders: any[]): string[] {
    const buildingSet = new Set<string>();

    for (const order of orders) {
      const building = order.deliveryAddress?.building;
      if (building && typeof building === 'string') {
        buildingSet.add(building.trim().toUpperCase());
      }
    }

    return Array.from(buildingSet);
  }

  /**
   * Get single trip by ID
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Trip details with serialized timestamps
   */
  async getMyTrip(shipperId: string, tripId: string): Promise<ShipperTrip> {
    const trip = await this.shipperTripsRepository.getById(tripId);

    if (!trip) {
      throw new NotFoundException('Trip not found');
    }

    // Verify ownership
    if (trip.shipperId !== shipperId) {
      throw new ForbiddenException('You do not have permission to view this trip');
    }

    // Serialize timestamps for consistent API response
    return this.serializeTripTimestamps(trip);
  }

  /**
   * List trips for shipper with pagination
   *
   * @param shipperId Shipper user ID
   * @param status Optional status filter
   * @param page Page number (1-based)
   * @param limit Items per page
   * @returns Paginated trips
   */
  async listMyTrips(
    shipperId: string,
    status?: TripStatus,
    page: number = 1,
    limit: number = 20,
  ): Promise<PaginatedTripsDto> {
    // Validate pagination params
    const validPage = Math.max(1, page);
    const validLimit = Math.min(50, Math.max(1, limit));

    this.logger.log(
      `Listing trips for shipper ${shipperId}: page=${validPage}, limit=${validLimit}, status=${status || 'all'}`,
    );

    // Get paginated results and total count
    const { trips, total } = await this.shipperTripsRepository.getByShipperIdPaginated(
      shipperId,
      validPage,
      validLimit,
      status,
    );

    // Serialize timestamps for all trips
    const serializedTrips = trips.map((trip) => this.serializeTripTimestamps(trip));

    // Calculate pagination metadata
    const totalPages = Math.ceil(total / validLimit);
    const hasNext = validPage < totalPages;

    return {
      items: serializedTrips,
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
      hasNext,
    };
  }

  /**
   * Batch update orders to SHIPPING status
   *
   * Uses Firestore batch write for atomic updates.
   * Validates shipper ownership and order status before updating.
   *
   * @param shipperId Shipper user ID
   * @param tripOrders Array of trip orders
   * @throws ConflictException if any order is not in READY status
   * @throws ForbiddenException if shipper doesn't own any order
   */
  private async batchUpdateOrdersToShipping(
    shipperId: string,
    tripOrders: TripOrder[],
  ): Promise<void> {
    const batch = this.firestore.batch();
    const now = Timestamp.now();
    const orderIds = tripOrders.map((order) => order.orderId);

    this.logger.log(
      `Batch updating ${orderIds.length} orders to SHIPPING for shipper ${shipperId}`,
    );

    // Fetch all orders and validate
    for (const orderId of orderIds) {
      const orderRef = this.firestore.collection('orders').doc(orderId);
      const orderDoc = await orderRef.get();

      if (!orderDoc.exists) {
        throw new NotFoundException(`Order ${orderId} not found`);
      }

      const orderData = orderDoc.data();

      // Validate shipper ownership
      if (orderData?.shipperId !== shipperId) {
        throw new ForbiddenException(
          `Order ${orderId} is not assigned to shipper ${shipperId}. ` +
            `Current shipper: ${orderData?.shipperId || 'none'}`,
        );
      }

      // Validate order status
      if (orderData?.status !== OrderStatus.READY) {
        throw new ConflictException(
          `Order ${orderId} must be in READY status to start shipping. ` +
            `Current status: ${orderData?.status}`,
        );
      }

      // Add update to batch
      batch.update(orderRef, {
        status: OrderStatus.SHIPPING,
        updatedAt: now,
      });
    }

    // Commit all updates atomically
    await batch.commit();
    this.logger.log(`Successfully updated ${orderIds.length} orders to SHIPPING`);
  }

  /**
   * Batch update orders to DELIVERED status
   *
   * Uses Firestore batch write for atomic updates.
   * Validates shipper ownership and order status before updating.
   *
   * @param shipperId Shipper user ID
   * @param tripOrders Array of trip orders
   * @throws ConflictException if any order is not in SHIPPING status
   * @throws ForbiddenException if shipper doesn't own any order
   */
  private async batchUpdateOrdersToDelivered(
    shipperId: string,
    tripOrders: TripOrder[],
  ): Promise<void> {
    const batch = this.firestore.batch();
    const now = Timestamp.now();
    const orderIds = tripOrders.map((order) => order.orderId);

    this.logger.log(
      `Batch updating ${orderIds.length} orders to DELIVERED for shipper ${shipperId}`,
    );

    // Fetch all orders and validate
    for (const orderId of orderIds) {
      const orderRef = this.firestore.collection('orders').doc(orderId);
      const orderDoc = await orderRef.get();

      if (!orderDoc.exists) {
        throw new NotFoundException(`Order ${orderId} not found`);
      }

      const orderData = orderDoc.data();

      // Validate shipper ownership
      if (orderData?.shipperId !== shipperId) {
        throw new ForbiddenException(
          `Order ${orderId} is not assigned to shipper ${shipperId}. ` +
            `Current shipper: ${orderData?.shipperId || 'none'}`,
        );
      }

      // Validate order status
      if (orderData?.status !== OrderStatus.SHIPPING) {
        throw new ConflictException(
          `Order ${orderId} must be in SHIPPING status to mark as delivered. ` +
            `Current status: ${orderData?.status}`,
        );
      }

      // Add update to batch
      batch.update(orderRef, {
        status: OrderStatus.DELIVERED,
        deliveredAt: now,
        updatedAt: now,
      });
    }

    // Commit all updates atomically
    await batch.commit();
    this.logger.log(`Successfully updated ${orderIds.length} orders to DELIVERED`);
  }

  /**
   * Start a trip (PENDING -> STARTED)
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Updated trip
   */
  async startTrip(shipperId: string, tripId: string): Promise<ShipperTrip> {
    const trip = await this.getMyTrip(shipperId, tripId);

    // Validate state transition
    if (trip.status !== TripStatus.PENDING) {
      throw new ConflictException(
        `Cannot start trip. Trip is already ${trip.status}. Only PENDING trips can be started.`,
      );
    }

    // Batch update all orders to SHIPPING status (atomic operation)
    await this.batchUpdateOrdersToShipping(shipperId, trip.orders);

    // Update trip status and set all tripDeliveryStatus to NOT_VISITED (reset for trip start)
    const updatedOrders = trip.orders.map((order) => ({
      ...order,
      tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
    }));

    await this.shipperTripsRepository.update(tripId, {
      status: TripStatus.STARTED,
      startedAt: new Date() as any, // Will be replaced by serverTimestamp in repo
      orders: updatedOrders,
    });

    this.logger.log(
      `Trip ${tripId} started by shipper ${shipperId}. ` +
        `${trip.orders.length} orders updated to SHIPPING status.`,
    );

    // Return updated trip
    return this.getMyTrip(shipperId, tripId);
  }

  /**
   * Finish a trip (STARTED -> FINISHED)
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Updated trip with delivery stats
   */
  async finishTrip(
    shipperId: string,
    tripId: string,
  ): Promise<{ trip: ShipperTrip; ordersDelivered: number }> {
    const trip = await this.getMyTrip(shipperId, tripId);

    // Validate state transition
    if (trip.status !== TripStatus.STARTED) {
      throw new ConflictException(
        `Cannot finish trip. Trip is ${trip.status}. Only STARTED trips can be finished.`,
      );
    }

    // Batch update all orders to DELIVERED status (atomic operation)
    await this.batchUpdateOrdersToDelivered(shipperId, trip.orders);

    // Update all tripDeliveryStatus to VISITED on finish
    const updatedOrders = trip.orders.map((order) => ({
      ...order,
      tripDeliveryStatus: TripDeliveryStatus.VISITED,
    }));

    // Update trip status
    await this.shipperTripsRepository.update(tripId, {
      status: TripStatus.FINISHED,
      finishedAt: new Date() as any, // Will be replaced by serverTimestamp in repo
      orders: updatedOrders,
    });

    this.logger.log(
      `Trip ${tripId} finished by shipper ${shipperId}. ` +
        `${trip.orders.length} orders updated to DELIVERED status.`,
    );

    // Return updated trip
    const updatedTrip = await this.getMyTrip(shipperId, tripId);
    return {
      trip: updatedTrip,
      ordersDelivered: updatedTrip.orders.length,
    };
  }

  /**
   * Cancel a trip (PENDING -> CANCELLED)
   *
   * Only PENDING trips can be cancelled.
   * Does NOT update Orders collection status.
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @param reason Optional cancellation reason
   * @returns Updated trip
   */
  async cancelTrip(shipperId: string, tripId: string, reason?: string): Promise<ShipperTrip> {
    const trip = await this.getMyTrip(shipperId, tripId);

    // Only allow cancellation of PENDING trips
    if (trip.status !== TripStatus.PENDING) {
      throw new ConflictException(
        `Cannot cancel trip. Trip is ${trip.status}. Only PENDING trips can be cancelled.`,
      );
    }

    // Update trip status to CANCELLED
    await this.shipperTripsRepository.update(tripId, {
      status: TripStatus.CANCELLED,
      cancelledAt: new Date() as any, // Will be replaced by serverTimestamp in repo
      ...(reason && { cancelReason: reason }),
    });

    this.logger.log(
      `Trip ${tripId} cancelled by shipper ${shipperId}.${reason ? ` Reason: ${reason}` : ''}`,
    );

    // Return updated trip
    return this.getMyTrip(shipperId, tripId);
  }

  /**
   * @deprecated Proxy to shared DeliveryPointsService
   * Use DeliveryPointsService directly instead
   */
  async listActiveDeliveryPoints() {
    return this.deliveryPointsService.listActiveDeliveryPoints();
  }
}
